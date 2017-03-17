package seng302;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by cjd137 on 17/03/2017.
 */

public class DisplayUtils {

    private static double width;
    private static double height;
    private static ArrayList<Double> getWidthHeight;
    private static double minLat;
    private static double minLon;
    private static double maxLat;
    private static double maxLon;

    /**
     * This function takes the given lat and lon and returns a x,y coordinate scaled to the screen size
     * and the change in multiplier to the screen size.
     * @param lat This is the required Latitude to find the Y coordinate.
     * @param lon This is the required Longitude to find the X coordinate.
     * @return Returns an x and a y coordinate for use in placement of boats and/or marks/gates.
     */
    public static ArrayList<Double> convertFromLatLon(double lat, double lon) {

        ArrayList<Double> xyCoords = new ArrayList<>();
        int xCoord;
        int yCoord;

        double changeInLat = maxLat - minLat;
        double changeInLon = maxLon - minLon;

        double lonPerPixel = width/changeInLon;
        double latPerPixel = height/changeInLat;

        xCoord = (int) Math.round(Math.abs((lon - minLon)*lonPerPixel));
        yCoord = (int) Math.round(Math.abs((lat - minLat)*latPerPixel));

        xyCoords.add((double) xCoord);
        xyCoords.add((double) yCoord);

        return xyCoords;
    }
    /**
     * This function sets and maximum and minimum latitudes and longitudes of the course.
     * @param maxMinLatLon This ArrayList holds the minimum and maximum Latitudes and Longitudes of the course.
     */
    public static void setMaxMinLatLon(ArrayList<Double> maxMinLatLon) {

        minLat = maxMinLatLon.get(0);
        minLon = maxMinLatLon.get(1);
        maxLat = maxMinLatLon.get(2);
        maxLon = maxMinLatLon.get(3);
    }

    /**
     * This function takes a multiplier and changes the size of the screen to the multiplied value.
     */
    public static void setScreenSize(double multiplier) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double newWidth = screenSize.getWidth();
        double newHeight = screenSize.getHeight();

        width = Math.round(newWidth*(multiplier));
        height = Math.round(newHeight*(multiplier));
    }

    public static void setWidthHeight(double newWidth, double newHeight) { width = newWidth; height = newHeight;}

    public static ArrayList<Double> getWidthHeight() {
        getWidthHeight = new ArrayList<>();
        getWidthHeight.add(width);
        getWidthHeight.add(height);
        return getWidthHeight;
    }


}
