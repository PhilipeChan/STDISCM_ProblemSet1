import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.*;

class Canvas extends JPanel {
    final int width = 1280;
    final int height = 720;
    private final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Wall> walls = new CopyOnWriteArrayList<>();
    // FPS tracking
    private final JLabel fpsLabel; // JLabel to display FPS
    private int framesCounted = 0;
    private long lastFpsUpdateTime = System.nanoTime(); // Time of the last FPS update
    private final BufferedImage offscreenImage;
    private final ForkJoinPool physicsThreadPool = new ForkJoinPool();
    private final ForkJoinPool renderingThreadPool = new ForkJoinPool();

    public Canvas(JLabel fpsLabel) {
        this.fpsLabel = fpsLabel; // Initialize the FPS label
        setPreferredSize(new Dimension(width, height));
        offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    public void addWall(Wall wall) {
        walls.add(wall);
    }

    public void startSimulation() {
        new Thread(() -> {
            while (true) {
                long startTime = System.nanoTime();
                updateParticles(Constants.TIME_STEP); // Update particle physics
                SwingUtilities.invokeLater(this::repaint); // Repaint the canvas

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
        physicsThreadPool.submit(() -> particles.parallelStream().forEach(particle -> {
            particle.updatePosition(deltaTime);
            particle.handleWallCollision(width, height, walls);
        })).join();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = offscreenImage.createGraphics();
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, width, height);

        // Load balancing for rendering particles
        renderingThreadPool.submit(() -> particles.parallelStream().forEach(particle -> {
            int drawY = height - particle.position.y - 5;
            g2d.setColor(Color.BLACK);
            g2d.fillOval(particle.position.x, drawY, 5, 5);
        })).join();

        for (Wall wall : walls) {
            g2d.setColor(Color.RED);
            g2d.drawLine(wall.start.x, height - wall.start.y, wall.end.x, height - wall.end.y);
        }

        g2d.dispose();
        g.drawImage(offscreenImage, 0, 0, this);

        // Draw the border for the designated area
        g.setColor(Color.BLACK); // Set border color
        g.drawRect(0, 0, width, height); // Draw border around the 1280x720 area

        updateFPS();
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
        if (n <= 0) return; // No particles to add
        if (n == 1) {
            // Add a single particle at the start point
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
}
