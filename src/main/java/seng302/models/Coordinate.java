package seng302.models;

/**
 * Coordinate class to encapsulates the latitude longitude coordinates.
 */
public class Coordinate {
    protected double lat, lon;

    private static final double EARTH_RADIUS_IN_NAUTICAL_MILES = 3437.74677;

    public Coordinate(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    /**
     * Simplifier for setting lat and lon in one call
     */
    public void update(double lat, double lon) {
        setLat(lat);
        setLon(lon);
    }

    /**
     * Calculates distance from this coordinate to another
     * @param other the other Coordinate to compare distance to
     * @return distance from this to other in nautical miles
     */
    public double greaterCircleDistance(Coordinate other){
        double lat1 = Math.toRadians(lat);
        double lat2 = Math.toRadians(other.getLat());
        double lon1 = Math.toRadians(lon);
        double lon2 = Math.toRadians(other.getLon());

        return EARTH_RADIUS_IN_NAUTICAL_MILES * Math.acos(Math.sin(lat1) * Math.sin(lat2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
    }

    /**
     * Calculates the heading from this coordinate to another
     * @param other the other Coordinate to calculate heading to
     * @return the heading from this to other
     */
    public double headingToCoordinate(Coordinate other){
        double Ldelta = Math.toRadians(other.getLon()) - Math.toRadians(getLon());
        double X = Math.cos(Math.toRadians(other.getLat())) * Math.sin(Ldelta);
        double Y = Math.cos(Math.toRadians(getLat())) * Math.sin(Math.toRadians(other.getLat()))
                - Math.sin(Math.toRadians(getLat())) * Math.cos(Math.toRadians(other.getLat())) * Math.cos(Ldelta);
        double heading = Math.toDegrees(Math.atan2(X, Y));
        if(heading < 0){
            heading += 360;
        }
        return heading;
    }
}
