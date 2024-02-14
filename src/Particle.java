import java.awt.*;
import java.util.List;

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

    public void handleWallCollision(int canvasWidth, int canvasHeight, List<Wall> walls) {
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

        for (Wall wall : walls) {
            if (checkCollisionWithWall(wall)) {
                reflectOffWall(wall);
            }
        }

        if (angle < 0) angle += 360;
        else if (angle > 360) angle -= 360;
    }

    private boolean checkCollisionWithWall(Wall wall) {
        double x1 = position.x;
        double y1 = position.y;

        double x2 = x1 + velocity * Math.cos(Math.toRadians(angle)) * (1 / 60.0);
        double y2 = y1 + velocity * Math.sin(Math.toRadians(angle)) * (1 / 60.0);

        double x3 = wall.start.x;
        double y3 = wall.start.y;
        double x4 = wall.end.x;
        double y4 = wall.end.y;

        double den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (den == 0) {
            return false;
        }

        double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / den;
        double u = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / den;

        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }


    private void reflectOffWall(Wall wall) {
        double incidentX = Math.cos(Math.toRadians(angle));
        double incidentY = Math.sin(Math.toRadians(angle));

        double wallDx = wall.end.x - wall.start.x;
        double wallDy = wall.end.y - wall.start.y;

        double normalX = wallDy;
        double normalY = -wallDx;

        double length = Math.sqrt(normalX * normalX + normalY * normalY);
        normalX /= length;
        normalY /= length;

        double dotProduct = incidentX * normalX + incidentY * normalY;

        double reflectX = incidentX - 2 * dotProduct * normalX;
        double reflectY = incidentY - 2 * dotProduct * normalY;

        angle = Math.toDegrees(Math.atan2(reflectY, reflectX));

        if (angle < 0) angle += 360;
        else if (angle >= 360) angle -= 360;
    }
}
