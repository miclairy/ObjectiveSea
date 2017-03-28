package seng302;
import javafx.scene.Node;

import javax.naming.ldap.Control;

/**
 * Created by cjd137 on 17/03/2017.
 */

public class DisplayUtils {

    private static double minLat;
    private static double minLon;
    private static double maxLat;
    private static double maxLon;

    /**
     * This function takes the given lat and lon and returns a x,y coordinate scaled to the canvas size
     * and the change in multiplier to the screen size.
     * @param lat This is the required Latitude to find the Y coordinate.
     * @param lon This is the required Longitude to find the X coordinate.
     * @return Returns CartesianPoint with an x and a y coordinate for use in placement of boats and/or marks/gates.
     */
    public static CartesianPoint convertFromLatLon(double lat, double lon) {
        double canvasY = Controller.getCanvasSize().getY();
        double canvasX = Controller.getCanvasSize().getX();

        double changeInLat = maxLat - minLat;
        double changeInLon = maxLon - minLon;

        double xPerLon = canvasX/changeInLon;
        double yPerLat = canvasY/changeInLat;

        if (yPerLat > xPerLon) {
            yPerLat = xPerLon;
        } else {
            xPerLon = yPerLat;
        }

        int xCoord = (int) ((canvasX - changeInLon * xPerLon) / 2 + (lon - minLon) * xPerLon);
        int yCoord = (int) (canvasY - ((canvasY - changeInLat * yPerLat) / 2 + (lat - minLat) * yPerLat));

        CartesianPoint point = new CartesianPoint(xCoord, yCoord);
        return point;
    }

    /**
     * This function sets and maximum and minimum latitudes and longitudes of the course.
     * @param minLat The minimum latitude of the course
     * @param minLon The minimum longitude of the course
     * @param maxLat The maximum latitude of the course
     * @param maxLon The maximum longitude of the course
     */
    public static void setMaxMinLatLon(double minLat, double minLon, double maxLat, double maxLon) {
        DisplayUtils.minLat = minLat;
        DisplayUtils.minLon = minLon;
        DisplayUtils.maxLat = maxLat;
        DisplayUtils.maxLon = maxLon;
    }

    public static boolean checkBounds(Node node){
        boolean outsideBound = false;
        if(node.getBoundsInParent().getMaxX() > Controller.getCanvasSize().getX()){
            outsideBound = true;
        }
        return outsideBound;
    }

}
