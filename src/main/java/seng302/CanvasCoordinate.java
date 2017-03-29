package seng302;

/**
 * Reprsents a point on the Cartesian coordinate system
 */
public class CanvasCoordinate {
    private double x, y;

    CanvasCoordinate(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
