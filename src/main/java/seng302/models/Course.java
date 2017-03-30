package seng302.models;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created on 7/03/17.
 * Class to figure out the mark locations and degrees.
 */

public class Course {

    private ArrayList<CompoundMark> courseOrder;
    private ArrayList<Coordinate> boundary;
    private double minLat, minLon, maxLat, maxLon;
    private HashMap<String, CompoundMark> marks;
    private double windDirection;
    private RaceLine startingLine;
    private String timeZone;

    public Course() {
        this.marks = new HashMap<>();
        this.courseOrder = new ArrayList<>();
        this.boundary = new ArrayList<>();
    }

    /**
     * Puts mark into the marks HashMap, with the name of the mark as the Key
     * @param mark - a defined Mark object
     */
    public void addNewMark(CompoundMark mark){
        marks.put(mark.getName(), mark);
    }

    /**
     * Appends a mark to the course order ArrayList. The mark must already exist in the marks HashMap
     * @param markName - the name of the mark to look up in the marks HashMap
     */
    public void addMarkInOrder(String markName){
        courseOrder.add(marks.get(markName));
    }

    /**
     * Appends a coordinate (lat/long) to the boundary list to make a polygon
     * @param coord The coordinate of a new boundary point
     */
    public void addToBoundary(Coordinate coord){
        boundary.add(coord);
    }
    /**
     * This function finds the distance between each mark on the course
     * @param markIndex1 - the index in the courseOrder array of the source mark
     * @param markIndex2 - the index in the courseOrder array of the destination mark
     * @return distance between source mark and destination mark in nautical miles
     */
    public double distanceBetweenMarks(int markIndex1, int markIndex2){
        CompoundMark mark1 = this.courseOrder.get(markIndex1);
        CompoundMark mark2 = this.courseOrder.get(markIndex2);
        double distance = mark1.getPosition().greaterCircleDistance(mark2.getPosition());
        return distance;
    }

    /**
     * This function uses heading calculations to find the headings between two marks.
     * @param markIndex1 - the index in the courseOrder array of the source mark
     * @param markIndex2 - the index in the courseOrder array of the destination mark
     * @return heading - in degrees, from source mark to destination mark
     */
    public double headingsBetweenMarks(int markIndex1, int markIndex2){
        CompoundMark mark1 = this.courseOrder.get(markIndex1);
        CompoundMark mark2 = this.courseOrder.get(markIndex2);
        return mark1.getPosition().headingToCoordinate(mark2.getPosition());
    }

    /**
     * This function updates the min/max latitude/longitude if the new corresponding coordinate is lower/greater than
     * before
     * @param newLat the new latitude
     * @param newLon the new longitude
     */
    private void updateMinMaxLatLon(double newLat, double newLon){
        if(newLat > maxLat) {
            maxLat = newLat;
        } else if(newLat < minLat) {
            minLat = newLat;
        }
        if(newLon > maxLon) {
            maxLon = newLon;
        } else if(newLon < minLon) {
            minLon = newLon;
        }
    }

    /**
     * This function grabs all the latitude and longitude points for each mark/gate
     * The furthest most points to the North and East are found and made the max latitude and longitude
     * The furthest most points to the South and West are also found and made the min latitude and longitude.
     * This is then added to an ArrayList for future use.
     */
    public void initCourseLatLon() {
        maxLat = minLat = this.courseOrder.get(0).getLat();
        maxLon = minLon = this.courseOrder.get(0).getLon();

        for(CompoundMark mark : courseOrder){
            updateMinMaxLatLon(mark.getLat(), mark.getLon());
        }
        for(Coordinate coord : boundary){
            updateMinMaxLatLon(coord.getLat(), coord.getLon());
        }
        //Adding padding of 0.004 to each coordinate to make sure the visual area is large enough
        minLat -= 0.004; minLon -= 0.004; maxLat += 0.004; maxLon += 0.004;
    }

    public ArrayList<CompoundMark> getCourseOrder(){
        return this.courseOrder;
    }

    public HashMap<String, CompoundMark> getMarks() {
        return marks;
    }

    public double getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(double windDirection) {
        this.windDirection = (windDirection + 360) % 360;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public double getMinLat() {
        return minLat;
    }

    public double getMinLon() {
        return minLon;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public double getMaxLon() {
        return maxLon;
    }

    public ArrayList<Coordinate> getBoundary() {
        return boundary;
    }

    public RaceLine getStartingLine() {
        return startingLine;
    }

    public void setStartingLine(RaceLine startingLine) {
        this.startingLine = startingLine;
    }
}
