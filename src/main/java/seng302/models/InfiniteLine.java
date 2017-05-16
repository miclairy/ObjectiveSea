package seng302.models;

/**
 * Defines a line with equation ax + by + c = 0
 *
 */
public class InfiniteLine {
    private static final double EPS = 1e-9;
    private double a, b, c;

    public InfiniteLine(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    InfiniteLine(Coordinate point1, Coordinate point2){
        if(Math.abs(point1.getLon() - point2.getLon()) < EPS){
            a = 1.0;
            b = 0.0;
            c = -point1.getLon();
        } else{
            a = -(point1.getLat() - point2.getLat()) / (point1.getLon() - point2.getLon());
            b = 1.0;
            c = -(a * point1.getLon()) - point1.getLat();
        }
    }

    public Coordinate closestPoint(Coordinate point){
        Coordinate closest;
        if(this.isVerticalLine()){
            closest = new Coordinate(point.getLat(), -c);
        } else if(this.isHorizontalLine()){
            closest = new Coordinate(-c, point.getLon());
        } else{
            InfiniteLine perpendicular = this.pointSlopeToLine(point, 1 / a);
            closest = InfiniteLine.intersectionPoint(this, perpendicular);
        }
        return closest;
    }

    private static Coordinate intersectionPoint(InfiniteLine infiniteLine1, InfiniteLine infiniteLine2) {
        double lon = (infiniteLine1.getB() * infiniteLine2.getC() - infiniteLine2.getB() * infiniteLine1.getC()) /
                     (infiniteLine1.getA() * infiniteLine2.getB() - infiniteLine2.getA() * infiniteLine1.getB());
        double lat;
        if(!infiniteLine1.isVerticalLine()){
            lat = -(infiniteLine1.getA() * lon + infiniteLine1.getC());
        } else{
            lat = -(infiniteLine2.getA() * lon + infiniteLine2.getC());
        }
        return new Coordinate(lat, lon);
    }

    private InfiniteLine pointSlopeToLine(Coordinate point, double slope) {
        double a = -slope;
        double b = 1;
        double c = -((a * point.getLon()) + (b * point.getLat()));
        return new InfiniteLine(a, b ,c);
    }

    private boolean isVerticalLine() {
        return Math.abs(b) < EPS;
    }

    private boolean isHorizontalLine() {
        return Math.abs(a) < EPS;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }
}
