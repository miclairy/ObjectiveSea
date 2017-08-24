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

        double Ldelta = Math.toRadians(canvasCoordinate.getX()) - Math.toRadians(this.x);
        double X = Math.cos(Math.toRadians(canvasCoordinate.getY()) * Math.sin(Ldelta));
        double Y = Math.cos(Math.toRadians(this.y)) * Math.sin(Math.toRadians(canvasCoordinate.getY()))
                - Math.sin(Math.toRadians(this.y)) * Math.cos(Math.toRadians(canvasCoordinate.getY())) * Math.cos(Ldelta);
        double angle = Math.toDegrees(Math.atan2(X, Y));
        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }
}
