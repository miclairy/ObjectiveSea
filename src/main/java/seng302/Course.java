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

    public double distanceBetweenMarks(int markIndex1, int markIndex2){
        Mark mark1 = this.courseOrder.get(markIndex1);
        Mark mark2 = this.courseOrder.get(markIndex2);
        double distance = greaterCircleDistance(mark1.getLat(), mark2.getLat(), mark1.getLon(), mark2.getLon());
        return distance;
    }

    public double greaterCircleDistance(double lat1, double lat2, double lon1, double lon2){
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        return this.EARTH_RADIUS_IN_NAUTICAL_MILES * Math.acos(Math.sin(lat1) * Math.sin(lat2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
    }

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

    public void courseSizePoints() {
        double latMax, latMin;
        latMax = latMin = this.courseOrder.get(0).getLat();
        double lonMax, lonMin;
        lonMax = lonMin = this.courseOrder.get(0).getLon();

        for(int i = 0; i < this.courseOrder.size(); i++) {

            if(this.courseOrder.get(i).getLat() > latMax) {
                latMax = this.courseOrder.get(i).getLat();
            }
            else if(this.courseOrder.get(i).getLat() < latMin) {
                latMin = this.courseOrder.get(i).getLat();
            }

            if(this.courseOrder.get(i).getLon() > lonMax) {
                lonMax = this.courseOrder.get(i).getLon();
            }
            else if(this.courseOrder.get(i).getLon() < lonMin) {
                lonMin = this.courseOrder.get(i).getLon();
            }


        }
        System.out.printf("Max X, Y = %f,%f   Min X, Y = %f,%f", latMax, lonMax, latMin, lonMin);
        //0.004 lat and lon changes for padding

    }

    public ArrayList<Mark> getCourseOrder(){
        return this.courseOrder;
    }



}
