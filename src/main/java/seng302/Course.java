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
        double latDist = Math.pow((mark2.getLat() - mark1.getLat()), 2);
        double lonDist = Math.pow((mark2.getLon() - mark1.getLon()), 2);
        double totalDist = Math.pow((latDist + lonDist), 0.5);
        return totalDist;
    }

    public double headingsBetweenMarks(int markIndex1, int markIndex2){
        Mark mark1 = this.courseOrder.get(markIndex1);
        Mark mark2 = this.courseOrder.get(markIndex2);
        double latitude = mark2.getLat() - mark1.getLat();
        double longitude = mark2.getLon() - mark1.getLon();
        double heading = Math.toDegrees(Math.atan2(latitude, longitude));
        if(heading < 0){
            heading += 360;
        }
        return heading;
    }

    public ArrayList<Mark> getCourseOrder(){
        return this.courseOrder;
    }



}
