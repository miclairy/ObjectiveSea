package seng302.models;

import java.util.*;

/**
 * Created on 7/03/17.
 * Class to figure out the mark locations and degrees.
 */

public class Course extends Observable {

    private ArrayList<CompoundMark> courseOrder;
    private ArrayList<Coordinate> boundary;
    private double minLat, minLon, maxLat, maxLon;
    private Map<Integer, CompoundMark> compoundMarks;
    private Map<Integer, Mark> allMarks;
    private double windDirection;
    private int trueWindAngle;
    private int trueWindDirection;
    private RaceLine startLine, finishLine;


    private double trueWindSpeed;

    public Course() {
        this.compoundMarks = new HashMap<>();
        this.courseOrder = new ArrayList<>();
        this.boundary = new ArrayList<>();
        allMarks = new HashMap<>();
    }

    /**
     * Puts mark into the compoundMarks HashMap, with the name of the mark as the Key
     * @param compoundMark - a defined CompoundMark object
     */
    public void addNewCompoundMark(CompoundMark compoundMark){
        compoundMarks.put(compoundMark.getCompoundMarkID(), compoundMark);
        Mark mark1 = compoundMark.getMark1();
        allMarks.put(mark1.getSourceID(), mark1);
        if(compoundMark.hasTwoMarks()){
            Mark mark2 = compoundMark.getMark2();
            allMarks.put(mark2.getSourceID(), mark2);
        }
    }

    /**
     * Appends a mark to the course order ArrayList. The mark must already exist in the compoundMarks HashMap
     * @param compoundMarkID - the name of the mark to look up in the compoundMarks HashMap
     */
    public void addMarkInOrder(Integer compoundMarkID){
        if(compoundMarks.containsKey(compoundMarkID)){
            courseOrder.add(compoundMarks.get(compoundMarkID));
        }
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
     * This function uses heading calculations to find the headings between two compoundMarks.
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
        maxLat = maxLon = -Double.MAX_VALUE;
        minLat = minLon = Double.MAX_VALUE;
        for(Integer markId : allMarks.keySet()){
            Mark mark = allMarks.get(markId);
            updateMinMaxLatLon(mark.getPosition().getLat(), mark.getPosition().getLon());
        }
        for(Coordinate coord : boundary){
            updateMinMaxLatLon(coord.getLat(), coord.getLon());
        }
        //Adding padding of 0.004 to each coordinate to make sure the visual area is large enough
        minLat -= 0.004; minLon -= 0.004; maxLat += 0.004; maxLon += 0.004;
    }

    /**
     * Updates a position of a mark
     * @param sourceID the source id of a mark
     * @param lat the new latitude of a mark
     * @param lon the new longitude of a mark
     */
    public void updateMark(Integer sourceID, Double lat, Double lon) {
        if(allMarks.containsKey(sourceID)){
            Coordinate markCoordinate = allMarks.get(sourceID).getPosition();
            markCoordinate.setLat(lat);
            markCoordinate.setLon(lon);
        }
    }

    public void updateTrueWindValues(int angle, int direction) {
        trueWindAngle = angle;
        trueWindDirection = direction;
    }

    public void updateCourseWindValues(int raceCourseWindDirection) {
        windDirection = raceCourseWindDirection;
    }

    public ArrayList<CompoundMark> getCourseOrder(){
        return this.courseOrder;
    }

    public Map<Integer, CompoundMark> getCompoundMarks() {
        return compoundMarks;
    }

    public double getWindDirection() { return windDirection; }

    public void setWindDirection(double windDirection) {
        this.windDirection = (windDirection + 360) % 360;
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

    public double getTrueWindSpeed() {return trueWindSpeed;}

    public void setTrueWindSpeed(double trueWindSpeed) {this.trueWindSpeed = trueWindSpeed;}

    public ArrayList<Coordinate> getBoundary() {
        return boundary;
    }

    public RaceLine getStartLine() {
        return startLine;
    }

    public void setStartLine(RaceLine startLine) {
        this.startLine = startLine;
    }

    public Map<Integer, Mark> getAllMarks() {
        return Collections.unmodifiableMap(allMarks);
    }

    public void setFinishLine(RaceLine finishLine) {
        this.finishLine = finishLine;
    }

    public RaceLine getFinishLine() {
        return finishLine;
    }

    /**
     * This method takes another Course and attempts to merge the differences in it into this one.
     * Assumes that the other course has the desired changes, and overwrites it's self with these changes.
     * The other course is not changed.
     * For now, all it does is merge the boundary (as this is currently the only changing thing in the race XML file
     * that we care about). In future we may look at merging changed marks as well.
     * @param otherCourse the course to merge into this one
     */
    public void mergeWithOtherCourse(Course otherCourse) {
        this.boundary = new ArrayList<>(otherCourse.getBoundary());
        setChanged();
        notifyObservers();
    }

    public void removeCompoundMark(CompoundMark mark) {
        if(compoundMarks.containsKey(mark.getCompoundMarkID())){
            compoundMarks.remove(mark.getCompoundMarkID());
        }
    }
}
