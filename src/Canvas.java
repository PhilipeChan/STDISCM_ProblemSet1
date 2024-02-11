import java.awt.*;
import javax.swing.*;
import java.util.concurrent.CopyOnWriteArrayList;

class Canvas extends JPanel {
    final int width = 1280;
    final int height = 720;
    private final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Wall> walls = new CopyOnWriteArrayList<>();
    // FPS tracking
    private final JLabel fpsLabel; // JLabel to display FPS
    private int framesCounted = 0;
    private long lastFpsUpdateTime = System.nanoTime(); // Time of the last FPS update

    public Canvas(JLabel fpsLabel) {
        this.fpsLabel = fpsLabel; // Initialize the FPS label
        setPreferredSize(new Dimension(width, height));
    }

    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    public void addWall(Wall wall) {
        walls.add(wall);
    }

    // Called by the timer in Simulator to update particles
    public void updateParticles() {
        for (Particle particle : particles) {
            particle.updatePosition(1 / 60.0); // Assuming 60 FPS for deltaTime
            particle.handleWallCollision(width, height, walls);
        }
        repaint(); // Repaint the canvas to reflect position updates
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Particle particle : particles) {
            // Invert the y-coordinate for drawing. Subtract y from canvas height and adjust for the particle's diameter.
            int drawY = height - particle.position.y - 5; // Adjust for particle size to align at the bottom.
            g.fillOval(particle.position.x, drawY, 5, 5);
        }
        g.setColor(Color.BLACK);
        for (Wall wall : walls) {
            g.drawLine(wall.start.x, height - wall.start.y, wall.end.x, height - wall.end.y);
        }

        // Draw the border for the designated area
        g.setColor(Color.BLACK); // Set border color
        g.drawRect(0, 0, width - 1, height - 1); // Draw border around the 1280x720 area

        long currentTime = System.nanoTime();
        framesCounted++;

        // Update FPS every 0.5 seconds
        if ((currentTime - lastFpsUpdateTime) >= 500_000_000L) { // 500,000,000 ns = 0.5 seconds
            double elapsedTimeInSeconds = (currentTime - lastFpsUpdateTime) / 1_000_000_000.0;
            double fps = framesCounted / elapsedTimeInSeconds;
            fpsLabel.setText(String.format("FPS: %.2f", fps));

            // Reset counters
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