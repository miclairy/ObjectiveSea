package seng302.controllers;

import seng302.data.BoatStatus;
import seng302.data.RaceStatus;
import seng302.data.RaceVisionFileReader;
import seng302.models.*;
import seng302.utilities.TimeUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static seng302.data.RaceStatus.*;

/**
 * Created by Michael Trotter on 4/29/2017.
 * Creates and runs a mock race to be sent out over the MockStream
 */

public class MockRaceRunner implements Runnable {

    private String raceId;
    private long startTime;

    private RaceStatus raceStatus = RaceStatus.NOT_ACTIVE;

    private List<Boat> boatsInRace;
    private Course course;
    private double HOURS_PASSED_PER_FRAME = TimeUtils.convertSecondsToHours(0.2 / 1000);

    public MockRaceRunner(){
        this.raceId = generateRaceId();

        initialize();
    }

    public void initialize(){
        boatsInRace = RaceVisionFileReader.importDefaultStarters();
        course = RaceVisionFileReader.importCourse(null);
        setStartingPositions();

        startTime = Instant.now().toEpochMilli() + 5000; //5 seconds from now
        raceStatus = raceStatus.PRESTART;
        boatsInRace.forEach(b -> b.setStatus(BoatStatus.PRERACE));
    }

    /**
     * Generates a race id from the current date and time
     * @return
     */
    private String generateRaceId() {
        DateFormat dateFormat = new SimpleDateFormat("yyMMddHH");
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    public void run() {
        while (!raceStatus.isRaceEndedStatus()) {
            boolean atLeastOneBoatNotFinished = false;
            for (Boat boat : boatsInRace) {

                if(raceStatus.equals(RaceStatus.STARTED)){
                    updateLocation(boat, HOURS_PASSED_PER_FRAME, course);
                    calculateTimeAtNextMark(boat);
                } else {
                    long millisBeforeStart = startTime - Instant.now().toEpochMilli();
                    if(millisBeforeStart < 3000 && millisBeforeStart > 1000){
                        raceStatus = WARNING;
                    }else if(millisBeforeStart < 1000  && millisBeforeStart >0){
                        raceStatus = RaceStatus.PREPARATORY;
                    }else if (millisBeforeStart < 0){
                        raceStatus = RaceStatus.STARTED;
                        boatsInRace.forEach(b -> b.setStatus(BoatStatus.RACING)); //set status to Racing
                    }
                }
                if (!boat.getStatus().equals(BoatStatus.FINISHED)) {
                    atLeastOneBoatNotFinished = true;
                }

            }
            if (!atLeastOneBoatNotFinished) {
                raceStatus = RaceStatus.TERMINATED;
            }
        }
    }

    /**
     * Updates the boat's coordinates by how much it moved in timePassed hours on the course
     * @param timePassed the amount of race hours since the last update
     * @param course the course the boat is racing on
     */
    private Coordinate updateLocation(Boat boat, double timePassed, Course course) {
        if(boat.isFinished()){
            return null;
        }
        int lastPassedMark = boat.getLastRoundedMarkIndex();
        boat.setSpeed(boat.getMaxSpeed());
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        CompoundMark nextMark = courseOrder.get(lastPassedMark+1);

        Coordinate currentPosition = boat.getCurrentPosition();
        double distanceGained = timePassed * boat.getSpeed();
        double distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());

        //If boat moves more than the remaining distance in the leg
        while(distanceGained > distanceLeftInLeg && lastPassedMark < courseOrder.size()-1){
            distanceGained -= distanceLeftInLeg;



            //Set boat position to next mark
            currentPosition.setLat(nextMark.getPosition().getLat());
            currentPosition.setLon(nextMark.getPosition().getLon());
            lastPassedMark++;
            boat.setLastRoundedMarkIndex(lastPassedMark);

            if(lastPassedMark < courseOrder.size()-1){
                boat.setHeading(course.headingsBetweenMarks(lastPassedMark, lastPassedMark + 1));
                nextMark = courseOrder.get(lastPassedMark+1);
                distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());
            }
        }

        //Check if boat has finished
        if(lastPassedMark == courseOrder.size()-1){
            boat.setStatus(BoatStatus.FINISHED);
            boat.setSpeed(0);
        } else{
            //Move the remaining distance in leg
            double percentGained = (distanceGained / distanceLeftInLeg);
            double newLat = currentPosition.getLat() + percentGained * (nextMark.getPosition().getLat() - currentPosition.getLat());
            double newLon = currentPosition.getLon() + percentGained * (nextMark.getPosition().getLon() - currentPosition.getLon());
            currentPosition.update(newLat, newLon);
        }
        return currentPosition;
    }

    /**
     * Spreads the starting positions of the boats over the start line
     */
    public void setStartingPositions(){
        RaceLine startingLine = course.getStartLine();
        Coordinate startingEnd1 = startingLine.getMark1().getPosition();
        Coordinate startingEnd2 = startingLine.getMark2().getPosition();
        Integer spaces = boatsInRace.size();
        Double dLat = (startingEnd2.getLat() - startingEnd1.getLat()) / spaces;
        Double dLon = (startingEnd2.getLon() - startingEnd1.getLon()) / spaces;

        Double curLat = startingEnd1.getLat() + dLat;
        Double curLon = startingEnd1.getLon() + dLon;
        for (Boat boat : boatsInRace){
            boat.setMaxSpeed(20);
            boat.maximiseSpeed();
            boat.setPosition(curLat, curLon);
            boat.setHeading(course.headingsBetweenMarks(0, 1));
            boat.getPathCoords().add(new Coordinate(curLat, curLon));
            boat.setLastRoundedMarkIndex(0);
            curLat += dLat;
            curLon += dLon;
        }
    }

    /**
     * Updates the boats time to the next mark
     * @param boat the current boat that is being updated.
     */
    private void calculateTimeAtNextMark(Boat boat){
        ArrayList<CompoundMark> order = course.getCourseOrder();
        if (boat.getLastRoundedMarkIndex() + 1 < order.size()) {
            CompoundMark nextMark = order.get(boat.getLastRoundedMarkIndex() + 1);
            Coordinate boatLocation = boat.getCurrentPosition();
            Coordinate markLocation = nextMark.getPosition();
            double dist = TimeUtils.calcDistance(boatLocation.getLat(), markLocation.getLat(), boatLocation.getLon(), markLocation.getLon());
            double testTime = dist / 10; // 10 is the VMG estimate of the boats

            double time = (TimeUtils.convertHoursToSeconds(testTime) * 1000) + Instant.now().toEpochMilli(); //time at next mark in milliseconds
            try {
                if (nextMark.isFinishLine()){
                    boat.setTimeTillFinish((long) time);
                }
                boat.setTimeTillMark((long) time);
            } catch (NumberFormatException ignored){ // Throws error at start when trying to convert ∞ to a double
            }
        }
    }

    public String getRaceId() {
        return raceId;
    }

    public RaceStatus getRaceStatus() {
        return raceStatus;
    }

    public List<Boat> getBoatsInRace() {
        return boatsInRace;
    }

    public Course getCourse() {
        return course;
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * @return whether or not the MockRaceRunner has finished generating new data for a race
     */
    public boolean raceHasEnded(){
        return raceStatus.isRaceEndedStatus();
    }
}
