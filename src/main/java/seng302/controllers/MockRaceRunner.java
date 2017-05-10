package seng302.controllers;

import seng302.data.BoatStatus;
import seng302.data.RaceStatus;
import seng302.data.RaceVisionXMLParser;
import seng302.models.*;
import seng302.utilities.DisplayUtils;
import seng302.utilities.MathUtils;
import seng302.utilities.TimeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static seng302.data.RaceStatus.*;

/**
 * Created by Michael Trotter on 4/29/2017.
 * Creates and runs a mock race to be sent out over the MockStream
 */

public class MockRaceRunner implements Runnable {

    private final double SECONDS_PER_UPDATE = 0.2;
    private double scaleFactor = 1;
    private final double WARNING_SIGNAL_TIME_IN_MS = (1000 * 60 * 3);
    private final double PREPATORY_SIGNAL_TIME_IN_MS = (1000 * 60 * 1);

    private String raceId;
    private Race race;

    public MockRaceRunner(){
        this.raceId = generateRaceId();

        initialize();
    }

    public void initialize(){
        List<Boat> boatsInRace = RaceVisionXMLParser.importDefaultStarters();
        Course course = RaceVisionXMLParser.importCourse();
        course.setTrueWindSpeed(20);
        course.setWindDirection(26.561799230287797);
        race = new Race("Mock Runner Race", course, boatsInRace);

        setStartingPositions();
        race.updateRaceStatus(RaceStatus.PRESTART);
        long currentTime = Instant.now().toEpochMilli();
        race.setCurrentTimeInEpochMs(currentTime);
        race.setStartTimeInEpochMs(currentTime + (1000 * 60 * 3)); //3 minutes from now
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
        while (!race.getRaceStatus().isRaceEndedStatus()) {
            boolean atLeastOneBoatNotFinished = false;
            double raceSecondsPassed = SECONDS_PER_UPDATE * scaleFactor;

            race.setCurrentTimeInEpochMs(race.getCurrentTimeInEpochMs() + (long)(raceSecondsPassed * 1000));
            for (Boat boat : race.getCompetitors()) {
                if(race.getRaceStatus().equals(RaceStatus.STARTED)){
                    updateLocation(TimeUtils.convertSecondsToHours(raceSecondsPassed), race.getCourse(), boat);
                    calculateTimeAtNextMark(boat);
                } else {
                    long millisBeforeStart = race.getStartTimeInEpochMs() - race.getCurrentTimeInEpochMs();
                    if(millisBeforeStart < WARNING_SIGNAL_TIME_IN_MS && millisBeforeStart > PREPATORY_SIGNAL_TIME_IN_MS) {
                        race.updateRaceStatus(WARNING);
                    }else if(millisBeforeStart < PREPATORY_SIGNAL_TIME_IN_MS && millisBeforeStart > 0){
                        race.updateRaceStatus(RaceStatus.PREPARATORY);
                    }else if (millisBeforeStart < 0){
                        race.updateRaceStatus(RaceStatus.STARTED);
                        race.getCompetitors().forEach(b -> b.setStatus(BoatStatus.RACING)); //set status to Racing
                    }
                }
                if (!boat.getStatus().equals(BoatStatus.FINISHED)) {
                    atLeastOneBoatNotFinished = true;
                }

            }
            if (!atLeastOneBoatNotFinished) {
                race.updateRaceStatus(RaceStatus.TERMINATED);
            }

            try{
                Thread.sleep((long) (SECONDS_PER_UPDATE * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Updates the boat's coordinates by how much it moved in timePassed hours on the course
     * @param timePassed the amount of race hours since the last update
     * @param course the course the boat is racing on
     */
    public void updateLocation(double timePassed, Course course, Boat boat) {
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        if(boat.isFinished() || courseOrder.get(boat.getLastRoundedMarkIndex()).isFinishLine()) {
            return;
        }
        double windDirection = course.getWindDirection();
        double bearing = course.headingsBetweenMarks(boat.getLastRoundedMarkIndex(),boat.getLastRoundedMarkIndex()+1);
        boolean onTack = false;
        boolean onGybe = false;
        boat.setCurrentVMGSpeed(boat.getMaxSpeed());
        if(MathUtils.pointBetweenTwoAngle(windDirection,boat.getTWAofBoat(),bearing)){
            onTack = true;
            boat.setCurrentVMGSpeed(boat.getVMGofBoat());
        } else if(MathUtils.pointBetweenTwoAngle((windDirection + 180)%360, 180 - boat.getGybeTWAofBoat(), bearing)){
            System.out.println("Hello");
            onGybe = true;
            boat.setCurrentVMGSpeed(boat.getGybeVMGofBoat() * (-1.0));
        }

        CompoundMark nextMark = courseOrder.get(boat.getLastRoundedMarkIndex()+1);
        double currentSpeed = boat.getSpeed();


        //The polars currently used aren't for our fancy catamaran's so it is super slow and boring
        // so I've commented them out for practicality of watching :)
        if(onTack) {
            boat.setCurrentSpeed(boat.getVMGofBoat() / Math.cos(Math.toRadians(boat.getTWAofBoat())));
        } else if(onGybe){
            boat.setCurrentSpeed(boat.getGybeVMGofBoat() / Math.cos(Math.toRadians(boat.getGybeTWAofBoat())));
        }

        Coordinate currentPosition = boat.getCurrentPosition();
        double distanceGained = timePassed * currentSpeed;
        double distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());


        //If boat moves more than the remaining distance in the leg
        while(distanceGained > distanceLeftInLeg && boat.getLastRoundedMarkIndex() < courseOrder.size()-1) {
            distanceGained -= distanceLeftInLeg;
            //Set boat position to next mark
            currentPosition.setLat(nextMark.getPosition().getLat());
            currentPosition.setLon(nextMark.getPosition().getLon());
            boat.setLastRoundedMarkIndex(boat.getLastRoundedMarkIndex() + 1);

            if(boat.getLastRoundedMarkIndex() < courseOrder.size()-1){
                boat.setHeading(course.headingsBetweenMarks(boat.getLastRoundedMarkIndex(), boat.getLastRoundedMarkIndex() + 1));
                nextMark = courseOrder.get(boat.getLastRoundedMarkIndex() +1);
                distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());
            }
        }

        //Check if boat has finished
        if(!boat.isFinished() && boat.getLastRoundedMarkIndex() != courseOrder.size()-1) {
            if(boat.getLastRoundedMarkIndex() == courseOrder.size()-1){
                boat.setStatus(BoatStatus.FINISHED);
                boat.setCurrentSpeed(0);
            } if(onTack) {
                double alphaAngle;
                if(bearing <= (windDirection + 90.0)){
                    alphaAngle = Math.abs(bearing - windDirection)%360;
                } else {
                    alphaAngle = (360 + windDirection - bearing)%360;
                }
                boat.setLastGybeMarkPassed(0);
                Coordinate tackingPosition = tackingUpdateLocation(distanceGained, courseOrder, true,alphaAngle, boat);
                //Move the remaining distance in leg
                currentPosition.update(tackingPosition.getLat(), tackingPosition.getLon());
            } else if(onGybe) {
                double alphaAngle;
                if(bearing <= (windDirection + 90.0)){
                    alphaAngle = 180 - Math.abs(bearing - windDirection)%360;
                } else {
                    alphaAngle = 180 - (360 + windDirection - bearing)%360;
                }
                boat.setLastTackMarkPassed(0);
                Coordinate tackingPosition = tackingUpdateLocation(distanceGained, courseOrder, false,alphaAngle, boat);
                //Move the remaining distance in leg
                currentPosition.update(tackingPosition.getLat(), tackingPosition.getLon());
            } else {
                boat.setLastTackMarkPassed(0);
                boat.setLastGybeMarkPassed(0);
                //Move the remaining distance in leg
                double percentGained = (distanceGained / distanceLeftInLeg);
                double newLat = boat.getCurrentLat() + percentGained * (nextMark.getPosition().getLat() - boat.getCurrentLat());
                double newLon = boat.getCurrentLon() + percentGained * (nextMark.getPosition().getLon() - boat.getCurrentLon());
                currentPosition.update(newLat, newLon);
            }
        } else {
            boat.setStatus(BoatStatus.FINISHED);
        }
    }

    /**
     * Updates the boat's coordinates by how much it moved in timePassed hours on the course
     * @param distanceGained the distance gained by the boat since last update
     * @param courseOrder the order of the set marks
     * @param onTack this decides whether to calculate a tack or a gybe
     */
    public Coordinate tackingUpdateLocation(double distanceGained, ArrayList<CompoundMark> courseOrder, Boolean onTack, double alphaAngle, Boat boat){
        double TrueWindAngle;
        if(onTack){
            TrueWindAngle = boat.getTWAofBoat();
        } else {
            TrueWindAngle = 180 - boat.getGybeTWAofBoat();}

        CompoundMark nextMark = courseOrder.get(boat.getLastRoundedMarkIndex()+1);
        double lengthOfLeg = courseOrder.get(boat.getLastRoundedMarkIndex()).getPosition().greaterCircleDistance(nextMark.getPosition());
        double betaAngle = (2*TrueWindAngle) - alphaAngle;
        double lengthOfTack = ((lengthOfLeg* Math.sin(Math.toRadians(betaAngle)))/Math.sin(Math.toRadians(180 - 2*TrueWindAngle)))/2.0;
        ArrayList<CompoundMark> tackingMarks = new ArrayList<>();
        tackingMarks.add(courseOrder.get(boat.getLastRoundedMarkIndex()));
        CompoundMark currentMark = courseOrder.get(boat.getLastRoundedMarkIndex());
        if(!onTack){
            alphaAngle += 180;
        }
        Coordinate tackingCoord = currentMark.getPosition().coordAt(lengthOfTack,alphaAngle);
        Mark tackingMark = new Mark(0, "tackingMark", tackingCoord);

        CompoundMark tackingMarkCM = new CompoundMark(0, "tack1", tackingMark);
        tackingMarks.add(tackingMarkCM);
        tackingMarks.add(nextMark);

        int lastMarkPassed;
        if(onTack){
            lastMarkPassed = boat.getLastTackMarkPassed();
        } else {
            lastMarkPassed = boat.getLastGybeMarkPassed();
        }
        CompoundMark nextTackMark = tackingMarks.get(lastMarkPassed+1);
        double distanceLeftinTack = boat.getCurrentPosition().greaterCircleDistance(nextTackMark.getPosition());
        if(lastMarkPassed == 0){
            double newHeading = tackingMarks.get(lastMarkPassed).getPosition().headingToCoordinate(tackingMarks.get(lastMarkPassed + 1).getPosition());
            boat.setHeading(newHeading);
        }
        //If boat moves more than the remaining distance in the leg
        while(distanceGained > distanceLeftinTack && lastMarkPassed < tackingMarks.size()-1){
            distanceGained -= distanceLeftinTack;
            //Set boat position to next mark
            boat.getCurrentPosition().setLat(nextTackMark.getPosition().getLat());
            boat.getCurrentPosition().setLon(nextTackMark.getPosition().getLon());
            if(onTack){
                boat.setLastTackMarkPassed(boat.getLastTackMarkPassed() + 1);
            } else {
                boat.setLastGybeMarkPassed(boat.getLastGybeMarkPassed() + 1);
            }
            lastMarkPassed++;

            if(lastMarkPassed < tackingMarks.size()-1){
                double newHeading = tackingMarks.get(lastMarkPassed).getPosition().headingToCoordinate(tackingMarks.get(lastMarkPassed + 1).getPosition());
                boat.setHeading(newHeading);
                nextTackMark = tackingMarks.get(lastMarkPassed+1);
                distanceLeftinTack = boat.getCurrentPosition().greaterCircleDistance(nextTackMark.getPosition());
            }
        }
        if(lastMarkPassed == tackingMarks.size()-1 && nextMark.isFinishLine()){
            boat.setStatus(BoatStatus.FINISHED);
            boat.setCurrentSpeed(0);
        }
        double percentGained = (distanceGained / distanceLeftinTack);
        double newLat = boat.getCurrentLat() + percentGained * (nextTackMark.getPosition().getLat() - boat.getCurrentLat());
        double newLon = boat.getCurrentLon() + percentGained * (nextTackMark.getPosition().getLon() - boat.getCurrentLon());
        return new Coordinate(newLat, newLon);
    }

//    /**
//     * Updates the boat's coordinates by how much it moved in timePassed hours on the course
//     * @param raceSecondsPassed the amount of race seconds since the last update
//     * @param course the course the boat is racing on
//     * @param boat the boat which location is updated for
//     */
//    //Move updateLocation method with tacking from Boat to here
//    private Coordinate updateLocation(Boat boat, double raceSecondsPassed, Course course) {
//        if(boat.isFinished()){
//            return null;
//        }
//        int lastPassedMark = boat.getLastRoundedMarkIndex();
//        boat.setSpeed(boat.getMaxSpeed());
//        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
//        CompoundMark nextMark = courseOrder.get(lastPassedMark+1);
//
//        Coordinate currentPosition = boat.getCurrentPosition();
//        double distanceGained = TimeUtils.convertSecondsToHours(raceSecondsPassed) * boat.getSpeed();
//        double distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());
//
//        //If boat moves more than the remaining distance in the leg
//        while(distanceGained > distanceLeftInLeg && lastPassedMark < courseOrder.size()-1){
//            distanceGained -= distanceLeftInLeg;
//
//            //Set boat position to next mark
//            currentPosition.setLat(nextMark.getPosition().getLat());
//            currentPosition.setLon(nextMark.getPosition().getLon());
//            lastPassedMark++;
//            boat.setLastRoundedMarkIndex(lastPassedMark);
//
//            if(lastPassedMark < courseOrder.size()-1){
//                boat.setHeading(course.headingsBetweenMarks(lastPassedMark, lastPassedMark + 1));
//                nextMark = courseOrder.get(lastPassedMark+1);
//                distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());
//            }
//        }
//
//        //Check if boat has finished
//        if(lastPassedMark == courseOrder.size()-1){
//            boat.setStatus(BoatStatus.FINISHED);
//            boat.setSpeed(0);
//        } else{
//            //Move the remaining distance in leg
//            double percentGained = (distanceGained / distanceLeftInLeg);
//            double newLat = currentPosition.getLat() + percentGained * (nextMark.getPosition().getLat() - currentPosition.getLat());
//            double newLon = currentPosition.getLon() + percentGained * (nextMark.getPosition().getLon() - currentPosition.getLon());
//            currentPosition.update(newLat, newLon);
//        }
//        return currentPosition;
//    }

    /**
     * Spreads the starting positions of the boats over the start line
     */
    public void setStartingPositions(){
        RaceLine startingLine = race.getCourse().getStartLine();
        Coordinate startingEnd1 = startingLine.getMark1().getPosition();
        Coordinate startingEnd2 = startingLine.getMark2().getPosition();
        Integer spaces = race.getCompetitors().size();
        Double dLat = (startingEnd2.getLat() - startingEnd1.getLat()) / spaces;
        Double dLon = (startingEnd2.getLon() - startingEnd1.getLon()) / spaces;

        Double curLat = startingEnd1.getLat() + dLat;
        Double curLon = startingEnd1.getLon() + dLon;
        for (Boat boat : race.getCompetitors()){
            Random random = new Random();
            double rangeMin = 15.0;
            double rangeMax = 25.0;
            double speed = rangeMin + (rangeMax - rangeMin) * random.nextDouble();
            boat.setMaxSpeed(speed);
            boat.maximiseSpeed();
            boat.setPosition(curLat, curLon);
            boat.setHeading(race.getCourse().headingsBetweenMarks(0, 1));
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
        ArrayList<CompoundMark> order = race.getCourse().getCourseOrder();
        if (boat.getLastRoundedMarkIndex() + 1 < order.size()) {
            CompoundMark nextMark = order.get(boat.getLastRoundedMarkIndex() + 1);
            Coordinate boatLocation = boat.getCurrentPosition();
            Coordinate markLocation = nextMark.getPosition();
            double dist = TimeUtils.calcDistance(boatLocation.getLat(), markLocation.getLat(), boatLocation.getLon(), markLocation.getLon());
            double testTime = dist / boat.getCurrentVMGSpeed(); // 10 is the VMG estimate of the boats
            double time = (TimeUtils.convertHoursToSeconds(testTime) * 1000) + race.getCurrentTimeInEpochMs(); //time at next mark in milliseconds
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

    public Race getRace() {
        return race;
    }

    /**
     * @return whether or not the MockRaceRunner has finished generating new data for a race
     */
    public boolean raceHasEnded(){
        return race.getRaceStatus().isRaceEndedStatus();
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void setRace(Race race) {
        this.race = race;
    }
}
