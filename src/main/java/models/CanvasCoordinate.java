package src.main.java.models;

/**
 * Represents a point in the Cartesian coordinate system within the bounds of the canvas
 */
public class CanvasCoordinate {
    private double x, y;

    public CanvasCoordinate(double x, double y){
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

    public static double distance(CanvasCoordinate a, CanvasCoordinate b){
        return Math.hypot(a.getX()-b.getX(), a.getY()-b.getY());
    }
}
