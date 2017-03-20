package seng302;

import java.awt.*;
import java.util.ArrayList;
import javafx.scene.Group;

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
     * @return Returns CartesianPoint with an x and a y coordinate for use in placement of boats and/or marks/gates.
     */
    public static CartesianPoint convertFromLatLon(double lat, double lon) {

        double changeInLat = maxLat - minLat;
        double changeInLon = maxLon - minLon;

        double lonPerPixel = width/changeInLon;
        double latPerPixel = height/changeInLat;

        int xCoord = (int) Math.round(Math.abs((lon - minLon)*lonPerPixel));
        int yCoord = (int) Math.round(Math.abs(height - ((lat - minLat)*latPerPixel)));

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

    public static boolean checkIntersection(Group root, Double x, Double y){
        boolean contains = false;
        for(int i = 0; i < (root.getChildren().size() -1); i++){
            if(root.getChildren().get(i).getBoundsInParent().contains(x,y)){
                contains = true;
            }
        }
        System.out.println(contains);
        return contains;
    }


}
