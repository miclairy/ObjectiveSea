package seng302.models;

import seng302.utilities.TimeUtils;

import java.util.*;


/**
 * Created on 7/03/17.
 * A Race encompasses the course and the competitors.
 */
public class Race {

    private String name;
    private Course course;
    private List<Boat> competitors;
    private List<Boat> raceOrder = new ArrayList<>();
    private Map<Integer, Boat> boatIdMap;

    private double totalRaceTime;
    private double secondsBeforeRace = 0; //extra time in seconds to allow the race to begin and end smoothly

    public Race(String name, Course course, List<Boat> competitors) {
        this.name = name;
        this.course = course;
        this.competitors = competitors;
        raceOrder.addAll(competitors);
        boatIdMap = new HashMap<>();
        for(Boat competitor : competitors){
            boatIdMap.put(competitor.getId(), competitor);
        }
    }

    /**
     * Spreads the starting positions of the boats over the start line
     */
    public void setStartingPositions(){
        RaceLine startingLine = course.getStartingLine();
        int spaces = competitors.size();
        double dLat = (startingLine.getEnd2Lat() - startingLine.getEnd1Lat()) / spaces;
        double dLon = (startingLine.getEnd2Lon() - startingLine.getEnd1Lon()) / spaces;
        double curLat = startingLine.getEnd1Lat() + dLat;
        double curLon = startingLine.getEnd1Lon() + dLon;
        for (Boat boat : competitors){
            boat.setPosition(curLat, curLon);
            boat.setHeading(course.headingsBetweenMarks(0, 1));
            boat.getPathCoords().add(new Coordinate(curLat, curLon));
            curLat += dLat;
            curLon += dLon;
        }
    }

    public void updateBoat(Integer sourceID, Double lat, Double lon, Double heading, Double speed){
        if(boatIdMap.containsKey(sourceID)){
            System.err.println("Boat found");
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

    public String getName() {
        return name;
    }

    public Course getCourse() {
        return course;
    }


    public List<Boat> getRaceOrder() {
        return raceOrder;
    }

    public void setName(String name) {
        this.name = name;
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

}

