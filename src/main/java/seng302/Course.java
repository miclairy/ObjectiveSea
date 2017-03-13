package seng302;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cjd137 on 7/03/17.
 * Class to figure out the mark locations and degrees.
 */

public class Course {

    private ArrayList<Mark> courseOrder;
    private HashMap<String, Mark> marks;

    private static final double EARTH_RADIUS_IN_NAUTICAL_MILES = 3437.74677;

    public Course() {
        this.marks = new HashMap<>();
        this.courseOrder = new ArrayList<>();
    }

    public void addNewMark(Mark mark){
        marks.put(mark.getName(), mark);
    }

    public void addMarkInOrder(String markName){
        courseOrder.add(marks.get(markName));
    }

    /**
     * This functio finds the distance between each mark on the course
     * Uses greaterCircleDistance function to calculate actual distance
     */
    public double distanceBetweenMarks(int markIndex1, int markIndex2){
        Mark mark1 = this.courseOrder.get(markIndex1);
        Mark mark2 = this.courseOrder.get(markIndex2);
        double distance = greaterCircleDistance(mark1.getLat(), mark2.getLat(), mark1.getLon(), mark2.getLon());
        return distance;
    }

    /**
     * Calculates distances using formula that uses the coordinate locations.
     */
    public double greaterCircleDistance(double lat1, double lat2, double lon1, double lon2){
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        return this.EARTH_RADIUS_IN_NAUTICAL_MILES * Math.acos(Math.sin(lat1) * Math.sin(lat2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
    }

    /**
     * This function uses heading calculations to find the headings between two marks.
     * Returns heading in degrees.
     */
    public double headingsBetweenMarks(int markIndex1, int markIndex2){
        Mark mark1 = this.courseOrder.get(markIndex1);
        Mark mark2 = this.courseOrder.get(markIndex2);
        double Ldelta = Math.toRadians(mark2.getLon()) - Math.toRadians(mark1.getLon());
        double X = Math.cos(Math.toRadians(mark2.getLat())) * Math.sin(Ldelta);
        double Y = Math.cos(Math.toRadians(mark1.getLat())) * Math.sin(Math.toRadians(mark2.getLat()))
                - Math.sin(Math.toRadians(mark1.getLat())) * Math.cos(Math.toRadians(mark2.getLat())) * Math.cos(Ldelta);
        double heading = Math.toDegrees(Math.atan2(X, Y));
        if(heading < 0){
            heading += 360;
        }
        return heading;
    }

    /**
     * Getter for the course order
     */
    public ArrayList<Mark> getCourseOrder(){
        return this.courseOrder;
    }



}
