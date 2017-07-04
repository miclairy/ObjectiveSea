package seng302.models;

import static seng302.data.RaceStatus.*;

import seng302.data.RaceStatus;
import seng302.utilities.TimeUtils;

import java.util.*;


/**
 * Created on 7/03/17.
 * A Race encompasses the course and the competitors.
 */
public class Race extends Observable{

    public static final int UPDATED_STATUS_SIGNAL = 1;
    private String id;
    private String regattaName;
    private Course course;
    private List<Boat> competitors = new ArrayList<>();
    private List<Boat> raceOrder = new ArrayList<>();
    private Map<Integer, Boat> boatIdMap;
    private double totalRaceTime;
    private RaceStatus raceStatus = NOT_ACTIVE;
    private long startTimeInEpochMs, currentTimeInEpochMs;
    private double UTCOffset;
    private boolean firstMessage = true;
    private Set<Integer> competitorIds = new HashSet<>();

    public Race(String name, Course course, List<Boat> competitors) {
        initialize(name, course, competitors);
    }

    public Race(){

    }

    /**
     * Used for tests
     * @param name
     * @param course
     * @param competitors
     */
    public void initialize(String name, Course course, List<Boat> competitors) {
        this.regattaName = name;
        this.course = course;
        this.competitors = competitors;
        raceOrder.addAll(competitors);
        boatIdMap = new HashMap<>();
        for(Boat competitor : competitors){
            boatIdMap.put(competitor.getId(), competitor);
        }
        updateRaceOrder();
        raceStatus = NOT_ACTIVE;
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
            boat.addPathCoord(new Coordinate(curLat, curLon));
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
    public void updateBoat(Integer sourceID, Double lat, Double lon, Double heading, Double speed, double twa){
        if(boatIdMap.containsKey(sourceID)){
            Boat boat = boatIdMap.get(sourceID);
            boat.setPosition(lat, lon);
            boat.setHeading(heading);
            boat.setCurrentSpeed(speed);
            boat.setTWAofBoat(twa);
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

        this.totalRaceTime = TimeUtils.convertHoursToSeconds(courseDistance / slowestBoatSpeed);
    }

    /**
     * Updates the race status and prints it if it is different than before (for debugging purposes)
     * @param newRaceStatus The new race status read in
     */
    public void updateRaceStatus(RaceStatus newRaceStatus) {
        if(raceStatus != newRaceStatus){
            raceStatus = newRaceStatus;
            setChanged();
            notifyObservers(UPDATED_STATUS_SIGNAL);
        }
    }

    /**
     * Updates a boat's last rounded mark based on the ids of boat and the rounded mark's id, and update the race
     * order accordingly.
     * If a mark occurs multiple times in the race order, the rounded mark index will be the one next occurrence
     * of the mark that the boat has not rounded yet.
     * @param sourceID the boat's id
     * @param roundedMarkIndex the mark's index in race order
     * @param time the time that the boat rounded the mark
     */
    public void updateMarkRounded(int sourceID, int roundedMarkIndex, long time) {
        Boat boat = boatIdMap.get(sourceID);
        if(boat != null){
            boat.setLastRoundedMarkIndex(roundedMarkIndex);
            boat.setLastRoundedMarkTime(time);
            updateRaceOrder();
        }
    }

    /**
     * Checks if race is terminated or not
     * @return true is race has terminated status, false otherwise
     */
    public boolean isTerminated(){
        return raceStatus == TERMINATED;
    }

    public long getStartTimeInEpochMs() {
        return startTimeInEpochMs;
    }

    public void setStartTimeInEpochMs(long startTimeInEpochMs) {
        this.startTimeInEpochMs = startTimeInEpochMs;
    }

    public long getCurrentTimeInEpochMs() {
        return currentTimeInEpochMs;
    }

    public void setCurrentTimeInEpochMs(long currentTimeInEpoch) {
        this.currentTimeInEpochMs = currentTimeInEpoch;
    }

    public void updateRaceOrder() {
        Collections.sort(raceOrder);
        for (int i = 0; i < raceOrder.size(); i++) {
            Boat boat = raceOrder.get(i);
            boat.setCurrPlacing(i + 1);
        }
    }

    public RaceStatus getRaceStatus() {
        return raceStatus;
    }

    public double getUTCOffset() { return UTCOffset; }

    public void setUTCOffset(double UTCOffset) { this.UTCOffset = UTCOffset; }

    public Boat getBoatById(Integer id){
        if(boatIdMap.containsKey(id)){
            return boatIdMap.get(id);
        } else{
            return null;
        }
    }

    public boolean isInitialized(){
        return course != null && competitors != null;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isFirstMessage() {
        return firstMessage;
    }

    public void setFirstMessage(boolean firstMessage) {
        this.firstMessage = firstMessage;
    }

    private List<Boat> filterNonCompetitors(List<Boat> possibleCompetitors){
        List<Boat> participantsInRace = new ArrayList<>();
        for(Boat boat : possibleCompetitors){
            if(competitorIds.contains(boat.getId())){
                participantsInRace.add(boat);
            }
        }
        return participantsInRace;
    }

    public void setCompetitors(List<Boat> competitors) {
        List<Boat> actualCompetitors = filterNonCompetitors(competitors);
        this.competitors = actualCompetitors;

        raceOrder.addAll(actualCompetitors);
        boatIdMap = new HashMap<>();
        for(Boat competitor : actualCompetitors){
            boatIdMap.put(competitor.getId(), competitor);
        }
    }

    public void setCompetitorIds(Set<Integer> competitorIds) {
        this.competitorIds = competitorIds;
    }

    public String getId() {
        return id;
    }
}

