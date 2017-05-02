package seng302.utilities;
import javafx.scene.Node;
import seng302.controllers.Controller;
import seng302.models.CanvasCoordinate;
import seng302.models.Coordinate;


/**
 * Utils class used to calculated x y from lat lon and check bounds of the canvas.
 */

public class DisplayUtils {

    public static Coordinate max, min;
    public static String GOOGLE_API_KEY = "AIzaSyAQ8WSXVS1gXdhy5v9IpjeQL842wsMU1VQ";

    /**
     * Takes the given lat and lon and returns a x,y coordinate scaled to the canvas size
     * and the change in multiplier to the screen size.
     * @param lat This is the required Latitude to find the Y coordinate.
     * @param lon This is the required Longitude to find the X coordinate.
     * @return Returns CanvasCoordinate with an x and a y coordinate for use in placement of boats and/or marks/gates.
     */
    public static CanvasCoordinate convertFromLatLon(double lat, double lon) {
        double canvasY = Controller.getCanvasHeight();
        double canvasX = Controller.getCanvasWidth();

        double changeInLat = max.getLat() - min.getLat();
        double changeInLon = max.getLon() - min.getLon();

        double xPerLon = canvasX/changeInLon;
        double yPerLat = canvasY/changeInLat;

        if (yPerLat > xPerLon) {
            yPerLat = xPerLon;
        } else {
            xPerLon = yPerLat;
        }

        int xCoord = (int) ((canvasX - changeInLon * xPerLon) / 2 + (lon - min.getLon()) * xPerLon);
        int yCoord = (int) (canvasY - ((canvasY - changeInLat * yPerLat) / 2 + (lat - min.getLat()) * yPerLat));

        CanvasCoordinate point = new CanvasCoordinate(xCoord, yCoord);
        return point;
    }

    /**
     * generates Static Google Maps image url withing the current bounds of the course on screen
     * @return the image url as a string
     */
    public static String getGoogleMapsURL(){
        double canvasY = Controller.getAnchorHeight();
        double canvasX = Controller.getAnchorWidth(); //halved to keep within google size guidelines
        System.out.println("width: " + canvasX +"    height: " +canvasY);
        Coordinate midPoint = midPoint(max.getLat(), max.getLon(), min.getLat(), min.getLon());

        return "https://maps.googleapis.com/maps/api/staticmap?" +
                "center=" +
                midPoint.getLat() + "," + midPoint.getLon() +
                "&size=" +
                (int)canvasX + "x" + (int)canvasY +                         //dimentions of image
                "&style=feature:water%7Ccolor:0xaae7df" +
                "&style=feature:all%7Celement:labels%7Cvisibility:off" +
                "&visible="+
                min.getLat() + "," + min.getLon()+
                "&visible=" +
                max.getLat() + "%2C" + max.getLon() +
                "&scale=2" +
                "&key=" + GOOGLE_API_KEY;
    }


    /**
     * finds the geographic midpoint between two lat/lng points
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return the coordingate of the midpoint
     */
    public static Coordinate midPoint(double lat1,double lon1,double lat2,double lon2){

        double dLon = Math.toRadians(lon2 - lon1);

        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        return new Coordinate(Math.toDegrees(lat3), Math.toDegrees(lon3));
    }

    /**
     * Overload for convertFromLatLon which takes a Coordinate as a param
     * @param coordinate a Coordinate with defined lat and lon
     * @return Returns CanvasCoordinate with an x and a y coordinate for use in placement of boats and/or marks/gates.
     */
    public static CanvasCoordinate convertFromLatLon(Coordinate coordinate) {
        return convertFromLatLon(coordinate.getLat(), coordinate.getLon());
    }

    /**
     * This function sets and maximum and minimum latitudes and longitudes of the course.
     * @param minLat The minimum latitude of the course
     * @param minLon The minimum longitude of the course
     * @param maxLat The maximum latitude of the course
     * @param maxLon The maximum longitude of the course
     */
    public static void setMaxMinLatLon(double minLat, double minLon, double maxLat, double maxLon) {
        DisplayUtils.max = new Coordinate(maxLat, maxLon);
        DisplayUtils.min = new Coordinate(minLat, minLon);
    }

    /**
     * Checks whether the node is within the bounds of the canvas.
     * @param node Node to check if inside canvas.
     * @return Boolean of whether it is outside the bounds.
     */
    public static boolean checkBounds(Node node){
        boolean outsideBound = false;
        if(node.getBoundsInParent().getMaxX() > Controller.getCanvasWidth()){
            outsideBound = true;
        }
        return outsideBound;
    }

}
