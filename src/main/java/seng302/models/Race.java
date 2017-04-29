package seng302.models;

import seng302.utilities.TimeUtils;

import java.util.*;


/**
 * Created on 7/03/17.
 * A Race encompasses the course and the competitors.
 */
public class Race extends Observable{

    public static final int WARNING_STATUS = 1;
    public static final int PREPARATORY_STATUS = 2;
    public static final int STARTED_STATUS = 3;
    public static final int TERMINATED_STATUS = 8;

    public static final int UPDATED_STATUS_SIGNAL = 1;
    public static final int UPDATED_START_TIME_SIGNAL = 2;

    private String regattaName;
    private Course course;
    private List<Boat> competitors;
    private List<Boat> raceOrder = new ArrayList<>();
    private Map<Integer, Boat> boatIdMap;

    private double totalRaceTime;
    private double secondsBeforeRace = 0; //extra time in seconds to allow the race to begin and end smoothly
    private int raceStatus;
    private long startTimeInEpochMs, currentTimeInEpochMs;
    private int UTCOffset;

    public Race(String name, Course course, List<Boat> competitors) {
        this.regattaName = name;
        this.course = course;
        this.competitors = competitors;
        raceOrder.addAll(competitors);
        boatIdMap = new HashMap<>();
        for(Boat competitor : competitors){
            boatIdMap.put(competitor.getId(), competitor);
        }
        raceStatus = -1;
    }

    /**
     * Spreads the starting positions of the boats over the start line
     */
    public void setStartingPositions(){
        RaceLine startingLine = course.getStartLine();
        Coordinate startingEnd1 = startingLine.getMark1().getPosition();
        Coordinate startingEnd2 = startingLine.getMark2().getPosition();
        Integer spaces = competitors.size();
        Double dLat = (startingEnd2.getLat() - startingEnd1.getLat()) / spaces;
        Double dLon = (startingEnd2.getLon() - startingEnd1.getLon()) / spaces;

        Double curLat = startingEnd1.getLat() + dLat;
        Double curLon = startingEnd1.getLon() + dLon;
        for (Boat boat : competitors){
            boat.setPosition(curLat, curLon);
            boat.setHeading(course.headingsBetweenMarks(0, 1));
            boat.getPathCoords().add(new Coordinate(curLat, curLon));
            curLat += dLat;
            curLon += dLon;
        }
    }

    /**
     * Updates the position, speed and heading of the a boat with a given source id
     * @param sourceID the source id of the boat
     * @param lat the new latitude of the boat
     * @param lon the new longitude of the boat
     * @param heading the new heading of the boat
     * @param speed the new speed of the boat
     */
    public void updateBoat(Integer sourceID, Double lat, Double lon, Double heading, Double speed){
        if(boatIdMap.containsKey(sourceID)){
            Boat boat = boatIdMap.get(sourceID);
            boat.setPosition(lat, lon);
            boat.setHeading(heading);
            boat.setSpeed(speed);
        } else{
            System.err.println("Boat source ID not found");
        }
    }

    public List<Boat> getCompetitors() {
        return competitors;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public void setRegattaName(String name) { this.regattaName = name; }

    public Course getCourse() {
        return course;
    }

    public List<Boat> getRaceOrder() {
        return raceOrder;
    }

    public double getTotalRaceTime() {
        return totalRaceTime;
    }

    /**
     * Calculates the total time it will take for the race to complete using the total course distance and the
     * slowest boat's speed.
     */
    public void setTotalRaceTime(){

        double slowestBoatSpeed = Double.MAX_VALUE;
        for (Boat competitor : competitors) {
            slowestBoatSpeed = Math.min(slowestBoatSpeed, competitor.getMaxSpeed());
        }
        double courseDistance = 0;
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        for (int i = 1; i < courseOrder.size(); i++){
            courseDistance += course.distanceBetweenMarks(i - 1, i);
        }

        double totalRaceTimeInSeconds = TimeUtils.convertHoursToSeconds(courseDistance / slowestBoatSpeed);
        this.totalRaceTime = totalRaceTimeInSeconds;
    }

    public void setSecondsBeforeRace(double bufferTime) {
        secondsBeforeRace = bufferTime;
    }

    public double getSecondsBeforeRace() {
        return secondsBeforeRace;
    }

    /**
     * Updates the race status and prints it if it is different than before (for debugging purposes)
     * @param newRaceStatus The new race status read in
     */
    public void updateRaceStatus(int newRaceStatus) {
        if(raceStatus != newRaceStatus){
            raceStatus = newRaceStatus;
            setChanged();
            notifyObservers(UPDATED_STATUS_SIGNAL);
            System.out.println("Race Status: " + raceStatus);
        }
    }

    /**
     * Updates a boat's last rounded mark based on the ids of boat and the rounded mark's id, and update the race
     * order accordingly.
     * If a mark occurs multiple times in the race order, the rounded mark index will be the one next occurrence
     * of the mark that the boat has not rounded yet.
     * @param sourceID the boat's id
     * @param roundedMarkID the mark's id
     * @param time the time that the boat rounded the mark
     */
    public void updateMarkRounded(int sourceID, int roundedMarkID, long time) {
        Boat boat = boatIdMap.get(sourceID);
        List<CompoundMark> courseOrder = course.getCourseOrder();
        for(int markIndex = boat.getLastRoundedMarkIndex()+1; markIndex < courseOrder.size(); markIndex++){
            CompoundMark mark = courseOrder.get(markIndex);
            if(mark.getCompoundMarkID() == roundedMarkID){
                boat.setLastRoundedMarkIndex(markIndex);
                boat.setLastRoundedMarkTime(time);
                updateRaceOrder();
                System.out.println(boat.getName() + " rounded " + course.getCompoundMarks().get(roundedMarkID).getName() + " at " + time);
                return;
            }
        }
    }

    /**
     * Checks if race is terminated or not
     * @return true is race has terminated status, false otherwise
     */
    public boolean isTerminated(){
        return raceStatus == TERMINATED_STATUS;
    }

    public long getStartTimeInEpochMs() {
        return startTimeInEpochMs;
    }

    public void setStartTimeInEpochMs(long startTimeInEpochMs) {
        if(this.startTimeInEpochMs != startTimeInEpochMs){
            this.startTimeInEpochMs = startTimeInEpochMs;
            setChanged();
            notifyObservers(UPDATED_START_TIME_SIGNAL);

        }
    }

    public long getCurrentTimeInEpochMs() {
        return currentTimeInEpochMs;
    }

    public void setCurrentTimeInEpochMs(long currentTimeInEpoch) {
        this.currentTimeInEpochMs = currentTimeInEpoch;
    }

    private void updateRaceOrder() {
        Collections.sort(raceOrder);
    }

    public int getRaceStatus() {
        return raceStatus;
    }

    public int getUTCOffset() { return UTCOffset; }

    public void setUTCOffset(int UTCOffset) { this.UTCOffset = UTCOffset; }

    public Boat getBoatById(Integer id){
        if(boatIdMap.containsKey(id)){
            return boatIdMap.get(id);
        } else{
            return null;
        }
    }
}

