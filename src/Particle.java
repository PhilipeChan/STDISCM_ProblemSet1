import java.awt.*;

class Particle {
    Point position;
    double angle;
    double velocity;
    double accumulatedX = 0.0;
    double accumulatedY = 0.0;

    public Particle(int x, int y, double angle, double velocity) {
        this.position = new Point(x, y);
        this.angle = angle;
        this.velocity = velocity;
    }

    public void updatePosition(double deltaTime) {
        double radians = Math.toRadians(this.angle);

        double deltaX = this.velocity * Math.cos(radians) * deltaTime;
        double deltaY = this.velocity * Math.sin(radians) * deltaTime;

        accumulatedX += deltaX;
        accumulatedY += deltaY;

        if (Math.abs(accumulatedX) >= 1.0 || Math.abs(accumulatedY) >= 1.0) {
            this.position.x += (int)Math.round(accumulatedX);
            this.position.y += (int)Math.round(accumulatedY);

            accumulatedX -= (int)Math.round(accumulatedX);
            accumulatedY -= (int)Math.round(accumulatedY);
        }
    }

    public void handleWallCollision(int canvasWidth, int canvasHeight) {
        int particleDiameter = 5;
        int buffer = 1;

        if (position.x <= 0) {
            angle = 180 - angle;
            position.x = buffer;
        } else if (position.x + particleDiameter >= canvasWidth) {
            angle = 180 - angle;
            position.x = canvasWidth - particleDiameter - buffer;
        }

        if (position.y + particleDiameter >= canvasHeight) {
            angle = -angle;
            position.y = canvasHeight - particleDiameter - buffer;
        } else if (position.y <= 0) {
            angle = -angle;
            position.y = buffer;
        }

        if (angle < 0) angle += 360;
        else if (angle > 360) angle -= 360;
    }
}
