package seng302;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by cjd137 on 17/03/2017.
 */

public class DisplayUtils {

    private double width;
    private double height;
    private double minLat;
    private double minLon;
    private double maxLat;
    private double maxLon;

    /**
     * This function takes the given lat and lon and returns a x,y coordinate scaled to the screen size
     * and the change in multiplier to the screen size.
     * @param lat This is the required Latitude to find the Y coordinate.
     * @param lon This is the required Longitude to find the X coordinate.
     * @return Returns an x and a y coordinate for use in placement of boats and/or marks/gates.
     */
    public ArrayList<Double> convertFromLatLon(double lat, double lon) {

        ArrayList<Double> xyCoords = new ArrayList<>();
        int xCoord;
        int yCoord;

        double changeInLat = this.maxLat - this.minLat;
        double changeInLon = this.maxLon - this.minLon;

        double lonPerPixel = this.width/changeInLon;
        double latPerPixel = this.height/changeInLat;

        xCoord = (int) Math.round(Math.abs((lon - this.minLon)*lonPerPixel));
        yCoord = (int) Math.round(Math.abs((lat - this.minLat)*latPerPixel));

        xyCoords.add((double) xCoord);
        xyCoords.add((double) yCoord);

     return xyCoords;
    }
    /**
     * This function sets and maximum and minimum latitudes and longitudes of the course.
     * @param maxMinLatLon This ArrayList holds the minimum and maximum Latitudes and Longitudes of the course.
     */
    public void setMaxMinLatLon(ArrayList<Double> maxMinLatLon) {

        this.minLat = maxMinLatLon.get(0);
        this.minLon = maxMinLatLon.get(1);
        this.maxLat = maxMinLatLon.get(2);
        this.maxLon = maxMinLatLon.get(3);
    }

    /**
     * This function takes a multiplier and changes the size of the screen to the multiplied value.
     */
    public void setScreenSize(double multiplier) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();

        this.width = Math.round(width*(multiplier));
        this.height = Math.round(height*(multiplier));
    }

    public void setWidthHeight(double newWidth, double newHeight) { this.width = newWidth; this.height = newHeight;}


}
