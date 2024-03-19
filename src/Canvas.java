import java.awt.*;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.*;
import java.util.List;

class Canvas extends JPanel {
    final int width = 1280;
    final int height = 720;
    private final List<Particle> particles = new ArrayList<>();
    private final Object particlesLock = new Object();
    private final JLabel fpsLabel;
    private final JLabel particlesLabel;
    private final JLabel spritePositionLabel;
    private int framesCounted = 0;
    private long lastFpsUpdateTime = System.nanoTime();
    private final BufferedImage offscreenImage;
    private final ForkJoinPool physicsThreadPool = new ForkJoinPool();
    private final ForkJoinPool renderingThreadPool = new ForkJoinPool();
    private boolean explorerMode = false;
    private Point spritePosition = null;
    private final Set<Integer> pressedKeys = ConcurrentHashMap.newKeySet();

    public Canvas(JLabel fpsLabel, JLabel particlesLabel, JLabel spritePositionLabel) {
        this.fpsLabel = fpsLabel;
        this.particlesLabel = particlesLabel;
        this.spritePositionLabel = spritePositionLabel;
        setPreferredSize(new Dimension(width, height));
        offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        setFocusable(true);
        requestFocusInWindow();
        setupKeyListeners();
    }

    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!explorerMode) return;
                pressedKeys.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!explorerMode) return;
                pressedKeys.remove(e.getKeyCode());
            }
        });
    }

    private void updateSpritePosition() {
        if (!explorerMode) return;

        int movementSpeed = 1;
        if (pressedKeys.contains(KeyEvent.VK_W)) spritePosition.y = Math.min(height, spritePosition.y + movementSpeed);
        if (pressedKeys.contains(KeyEvent.VK_S)) spritePosition.y = Math.max(0, spritePosition.y - movementSpeed);
        if (pressedKeys.contains(KeyEvent.VK_A)) spritePosition.x = Math.max(0, spritePosition.x - movementSpeed);
        if (pressedKeys.contains(KeyEvent.VK_D)) spritePosition.x = Math.min(width, spritePosition.x + movementSpeed);

        SwingUtilities.invokeLater(() -> spritePositionLabel.setText("Sprite Position: " + getSpritePositionAsString()));
    }

    public void addParticle(Particle particle) {
        synchronized (particlesLock) {
            particles.add(particle);
            SwingUtilities.invokeLater(() -> {
                if (particlesLabel != null) {
                    particlesLabel.setText("Particles: " + particles.size());
                }
            });
        }
    }

    public void startSimulation() {
        new Thread(() -> {
            while (true) {
                long startTime = System.nanoTime();
                updateParticles(Constants.TIME_STEP);
                SwingUtilities.invokeLater(this::repaint);

                try {
                    long sleepTime = (Constants.OPTIMAL_TIME - (System.nanoTime() - startTime)) / 1000000;
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void updateParticles(double deltaTime) {
        List<Particle> particlesSnapshot;
        synchronized (particlesLock) {
            particlesSnapshot = new ArrayList<>(particles);
        }
        physicsThreadPool.submit(() -> particlesSnapshot.parallelStream().forEach(particle -> {
            particle.updatePosition(deltaTime);
            particle.handleWallCollision(width, height);
        })).join();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateSpritePosition();

        List<Particle> particlesSnapshot;
        synchronized (particlesLock) {
            particlesSnapshot = new ArrayList<>(particles);
        }

        Graphics2D g2d = offscreenImage.createGraphics();
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, width, height);

        if (explorerMode) {
            renderExplorerMode(g2d, particlesSnapshot);
        } else {
            renderingThreadPool.submit(() -> particlesSnapshot.parallelStream().forEach(particle -> {
                int drawY = height - particle.position.y - 5;
                g2d.setColor(Color.BLACK);
                g2d.fillOval(particle.position.x, drawY, 5, 5);
            })).join();
        }

        g2d.dispose();
        g.drawImage(offscreenImage, 0, 0, this);

        g.setColor(Color.BLACK);
        g.drawRect(0, 0, width, height);

        updateFPS();
    }

    private void renderExplorerMode(Graphics2D g2d, List<Particle> particlesSnapshot) {
        int gridWidth = 33;
        int gridHeight = 19;
        int cellWidth = width / gridWidth;
        int cellHeight = height / gridHeight;

        int viewportX = spritePosition.x - gridWidth / 2;
        int viewportY = height - spritePosition.y - gridHeight / 2;

        if (viewportX <= 0) {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, Math.abs(viewportX) * cellWidth, height);
        }
        if (viewportY <= 0) {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, width, Math.abs(viewportY) * cellHeight);
        }
        if (viewportX + gridWidth >= width) {
            g2d.setColor(Color.BLACK);
            int overflowWidth = (viewportX + gridWidth - width) * cellWidth - 12;
            g2d.fillRect(width - overflowWidth, 0, overflowWidth, height);
        }
        if (viewportY + gridHeight >= height) {
            g2d.setColor(Color.BLACK);
            int overflowHeight = (viewportY + gridHeight - height) * cellHeight - 20;
            g2d.fillRect(0, height - overflowHeight, width, overflowHeight);
        }

        viewportX = Math.max(0, Math.min(viewportX, width - gridWidth));
        viewportY = Math.max(0, Math.min(viewportY, height - gridHeight));

        for (Particle particle : particlesSnapshot) {
            int relativeX = particle.position.x - viewportX;
            int relativeY = height - particle.position.y - viewportY;

            if (relativeX >= 0 && relativeX < gridWidth && relativeY >= 0 && relativeY < gridHeight) {
                int drawX = relativeX * cellWidth;
                int drawY = relativeY * cellHeight;
                g2d.setColor(Color.BLACK);
                g2d.fillRect(drawX, drawY, cellWidth, cellHeight);
            }
        }

        int spriteScreenX = (gridWidth / 2) * cellWidth;
        int spriteScreenY = (gridHeight / 2) * cellHeight;
        g2d.setColor(Color.RED);
        g2d.fillRect(spriteScreenX, spriteScreenY, cellWidth, cellHeight);
    }


    private void updateFPS() {
        long currentTime = System.nanoTime();
        framesCounted++;
        if ((currentTime - lastFpsUpdateTime) >= 500_000_000L) {
            double elapsedTimeInSeconds = (currentTime - lastFpsUpdateTime) / 1_000_000_000.0;
            double fps = framesCounted / elapsedTimeInSeconds;
            fpsLabel.setText(String.format("FPS: %.2f", fps));
            framesCounted = 0;
            lastFpsUpdateTime = currentTime;
        }
    }

    public void addParticlesBetweenPoints(int n, Point start, Point end, double angle, double velocity) {
        if (n <= 0) return;
        if (n == 1) {
            addParticle(new Particle(start.x, start.y, angle, velocity));
            return;
        }
        for (int i = 0; i < n; i++) {
            double ratio = (double) i / (n - 1);
            int x = start.x + (int) ((end.x - start.x) * ratio);
            int y = start.y + (int) ((end.y - start.y) * ratio);
            addParticle(new Particle(x, y, angle, velocity));
        }
    }

    public void addParticlesVaryingAngles(int n, Point start, double startAngle, double endAngle, double velocity) {
        if (n <= 1) {
            addParticle(new Particle(start.x, start.y, startAngle, velocity));
            return;
        }

        for (int i = 0; i < n; i++) {
            double angleIncrement = (endAngle - startAngle) / (n - 1);
            double angle = startAngle + (angleIncrement * i);
            addParticle(new Particle(start.x, start.y, angle, velocity));
        }
    }

    public void addParticlesVaryingVelocities(int n, Point start, double angle, double startVelocity, double endVelocity) {
        if (n <= 1) {
            addParticle(new Particle(start.x, start.y, angle, startVelocity));
            return;
        }

        for (int i = 0; i < n; i++) {
            double velocityIncrement = (endVelocity - startVelocity) / (n - 1);
            double velocity = startVelocity + (velocityIncrement * i);
            addParticle(new Particle(start.x, start.y, angle, velocity));
        }
    }

    public void enterExplorerMode(int x, int y) {
        explorerMode = true;
        spritePosition = new Point(x, y);
        requestFocus();
        repaint();
    }

    public void enterExplorerModeAtLastPosition() {
        if (spritePosition != null) {
            explorerMode = true;
            requestFocus();
            repaint();
        }
    }

    public void exitExplorerMode() {
        explorerMode = false;
        repaint();
    }

    public Point getSpritePosition() {
        return spritePosition;
    }

    public String getSpritePositionAsString() {
        if (spritePosition != null) {
            return String.format("(%d, %d)", spritePosition.x, spritePosition.y);
        }
        return "(Not in explorer mode)";
    }
}
