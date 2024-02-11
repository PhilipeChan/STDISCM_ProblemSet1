import java.awt.*;

class Wall {
    Point start;
    Point end;

    public Wall(int x1, int y1, int x2, int y2) {
        this.start = new Point(x1, y1);
        this.end = new Point(x2, y2);
    }
}