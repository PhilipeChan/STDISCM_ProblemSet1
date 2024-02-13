import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

class Particle {
    Point position; // Utilize Point for x and y coordinates
    double angle; // In degrees
    double velocity; // Pixels per second
    double accumulatedX = 0.0; // Accumulated movement in X
    double accumulatedY = 0.0; // Accumulated movement in Y

    public Particle(int x, int y, double angle, double velocity) {
        this.position = new Point(x, y);
        this.angle = angle;
        this.velocity = velocity;
    }

    // Method to update particle's position based on its velocity and angle
    public void updatePosition(double deltaTime) {
        // Convert angle to radians for trigonometric calculations
        double radians = Math.toRadians(this.angle);

        // Calculate movement based on velocity and angle
        double deltaX = this.velocity * Math.cos(radians) * deltaTime;
        double deltaY = this.velocity * Math.sin(radians) * deltaTime;

        // Accumulate sub-pixel movements
        accumulatedX += deltaX;
        accumulatedY += deltaY;

        // Update position when accumulated movement exceeds 1 pixel
        if (Math.abs(accumulatedX) >= 1.0 || Math.abs(accumulatedY) >= 1.0) {
            this.position.x += (int)Math.round(accumulatedX);
            this.position.y += (int)Math.round(accumulatedY);

            // Reset accumulated movement after applying position change
            accumulatedX -= (int)Math.round(accumulatedX);
            accumulatedY -= (int)Math.round(accumulatedY);
        }
    }

    public void handleWallCollision(int canvasWidth, int canvasHeight, CopyOnWriteArrayList<Wall> walls) {
        int particleDiameter = 5;
        int buffer = 1; // A small buffer to prevent sticking to the wall

        if (position.x <= 0) {
            angle = 180 - angle;
            position.x = buffer; // Move particle slightly inside to prevent sticking
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

        // Handle wall collisions
        for (Wall wall : walls) {
            if (checkCollisionWithWall(wall)) {
                reflectOffWall(wall);
            }
        }

        // Normalize the angle
        if (angle < 0) angle += 360;
        else if (angle > 360) angle -= 360;
    }

    private boolean checkCollisionWithWall(Wall wall) {
        // Current position
        double x1 = position.x;
        double y1 = position.y;

        // Predicted next position based on current velocity and angle
        double x2 = x1 + velocity * Math.cos(Math.toRadians(angle)) * (1 / 60.0); // Assuming frame rate of 60 FPS for deltaTime
        double y2 = y1 + velocity * Math.sin(Math.toRadians(angle)) * (1 / 60.0);

        // Wall start and end points
        double x3 = wall.start.x;
        double y3 = wall.start.y;
        double x4 = wall.end.x;
        double y4 = wall.end.y;

        // Calculate denominators for the line intersection formula
        double den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (den == 0) {
            return false; // Lines are parallel, no intersection
        }

        // Calculate the intersection point (u and t are the line scalar values)
        double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / den;
        double u = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / den;

        // Check if there is an intersection within the line segments
        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }


    private void reflectOffWall(Wall wall) {
        // Calculate the incident vector components
        double incidentX = Math.cos(Math.toRadians(angle));
        double incidentY = Math.sin(Math.toRadians(angle));

        // Calculate wall's normal vector
        double wallDx = wall.end.x - wall.start.x;
        double wallDy = wall.end.y - wall.start.y;
        // Rotate 90 degrees to get the normal: (dy, -dx)
        double normalX = wallDy;
        double normalY = -wallDx;
        // Normalize the normal vector
        double length = Math.sqrt(normalX * normalX + normalY * normalY);
        normalX /= length;
        normalY /= length;

        // Correctly calculate the dot product between the incident vector and the wall's normal vector
        double dotProduct = incidentX * normalX + incidentY * normalY;

        // Reflect the incident vector off the wall's normal vector using the correct reflection formula
        double reflectX = incidentX - 2 * dotProduct * normalX;
        double reflectY = incidentY - 2 * dotProduct * normalY;

        // Convert the reflected vector back to an angle
        angle = Math.toDegrees(Math.atan2(reflectY, reflectX));

        // Ensure the angle is normalized to the range [0, 360)
        if (angle < 0) angle += 360;
        else if (angle >= 360) angle -= 360;
    }
}
