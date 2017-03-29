package seng302;

/**
 * Represents a point in the Cartesian coordinate system within the bounds of the canvas
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
