package seng302;

import java.util.*;


/**
 * Created on 7/03/17.
 * A Race encompasses a course and some competitors, and the events that occur throughout the race.
 * For now, events are randomly pre-computed and stored in a queue ready to be read by other classes such as Display
 */
public class Race {

    private String name;
    private Course course;
    private ArrayList<Boat> competitors;
    private ArrayList<Boat> raceOrder = new ArrayList<>();

    private double totalRaceTime;
    private double secondsBeforeRace = 240; //extra time in seconds to allow the race to begin and end smoothly

    public Race(String name, Course course, ArrayList<Boat> competitors) {
        this.name = name;
        this.course = course;
        this.competitors = competitors;
        setStartingPositions();
        raceOrder.addAll(competitors);
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

    public ArrayList<Boat> getCompetitors() {
        return this.competitors;
    }

    public String getName() {
        return name;
    }

    public Course getCourse() {
        return course;
    }


    public ArrayList<Boat> getRaceOrder() {
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

