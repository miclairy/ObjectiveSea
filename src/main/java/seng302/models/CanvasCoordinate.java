package seng302.models;

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

    public double getAngleFromSceneCentre(CanvasCoordinate canvasCoordinate) {
        final double deltaY = (this.y - canvasCoordinate.getY());
        final double deltaX = (this.x - canvasCoordinate.getX());
        final double result = Math.toDegrees(Math.atan2(deltaY, deltaX));
        return (result < 0) ? (360d + result) : result;
    }
}
