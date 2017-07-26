package seng302.controllers;

import seng302.data.BoatStatus;
import seng302.data.RaceStatus;
import seng302.data.RaceVisionXMLParser;
import seng302.models.*;
import seng302.utilities.DisplayUtils;
import seng302.utilities.MathUtils;
import seng302.utilities.PolarReader;
import seng302.utilities.TimeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static seng302.data.RaceStatus.*;

/**
 * Created by Michael Trotter on 4/29/2017.
 * Creates and runs a mock race to be sent out over the MockStream
 */

public class RaceUpdater implements Runnable {

    private final double SECONDS_PER_UPDATE = 0.02;
    private double scaleFactor = 1;
    private final double WARNING_SIGNAL_TIME_IN_MS = (1000 * 60 * 3);
    private final double PREPATORY_SIGNAL_TIME_IN_MS = (1000 * 60 * 1);
    private final double MIN_WIND_SPEED = 6.0;
    private final double MAX_WIND_SPEED = 24.0;
    private double initialWindSpeed;

    private Race race;
    private PolarTable polarTable;
    private Collection<Boat> potentialCompetitors;

    public RaceUpdater(){
        //set race up with default files
        intialWindSpeedGenerator();
        List<Boat> boatsInRace = new ArrayList<>();
        potentialCompetitors = RaceVisionXMLParser.importDefaultStarters();
        Course course = RaceVisionXMLParser.importCourse();
        course.setTrueWindSpeed(initialWindSpeed);
        course.setWindDirection(course.getWindDirectionBasedOnGates());
        race = new Race("Mock Runner Race", course, boatsInRace);
        setRandomBoatSpeeds();
//        setInitialBoatSpeeds();
        initialize();
    }

    public RaceUpdater(Race race) {
        this.race = race;
        initialize();
    }

    public void initialize(){
        race.setId(generateRaceId());
        //for now we assume all boats racing are AC35 class yachts such that we can use the polars we have for them
        this.polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
        race.updateRaceStatus(RaceStatus.PRESTART);
        long currentTime = Instant.now().toEpochMilli();
        race.setCurrentTimeInEpochMs(currentTime);
        race.setStartTimeInEpochMs(currentTime + (1000 * 60 * 3)); //3 minutes from now
    }

    public int addCompetitor() {
        Boat newCompetitor = potentialCompetitors.iterator().next();
        potentialCompetitors.remove(newCompetitor);
        race.addCompetitor(newCompetitor);
        prepareBoatForRace(newCompetitor);
        return newCompetitor.getId();
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
            generateWind();

            for (Boat boat : race.getCompetitors()) {
                if(race.getRaceStatus().equals(RaceStatus.STARTED)){
                    checkForCollision(boat);
                    if(boat.isSailsIn() && boat.getCurrentSpeed() > 0){
                        boat.setCurrentSpeed(boat.getCurrentSpeed() - 0.2);
                        if(boat.getCurrentSpeed() < 0) boat.setCurrentSpeed(0);
                    } else if(!boat.isSailsIn()){
                        boat.setMaxSpeed(boat.updateBoatSpeed(race.getCourse())-boat.getDamageSpeed());
                        if(boat.getCurrentSpeed() < boat.getMaxSpeed()){
                            boat.setCurrentSpeed(boat.getCurrentSpeed() + 0.1);
                        } if(boat.getCurrentSpeed() > boat.getMaxSpeed() + 1)boat.setCurrentSpeed(boat.getMaxSpeed());
                    }
                    updateLocation(TimeUtils.convertSecondsToHours(raceSecondsPassed), boat);
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
            //TODO fix so that race doesn't immediately end when no boats have yet registered for race
            if (!atLeastOneBoatNotFinished) {
                //race.updateRaceStatus(RaceStatus.TERMINATED);
            }

            try{
                Thread.sleep((long) (SECONDS_PER_UPDATE * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the location of a given boat to be displayed to the clients
     * @param timePassed time passed since last update
     * @param boat boat that needs location update
     */
    public void updateLocation(double timePassed, Boat boat) {
        double boatHeading = boat.getHeading();
        Coordinate boatPosition = boat.getCurrentPosition();
        double distanceGained = timePassed * boat.getCurrentSpeed();
        Coordinate newPos = boatPosition.coordAt(distanceGained, boatHeading);
        boatPosition.update(newPos.getLat(), newPos.getLon());
    }


    /**
     * Updates the boat's coordinates by how much it moved in timePassed hours on the course
     * @param timePassed the amount of race hours since the last update
     * @param course the course the boat is racing on
     */
    public void autoUpdateLocation(double timePassed, Course course, Boat boat) {
        if(boat.isFinished()) return;

        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        double windDirection = course.getWindDirection();
        double headingBetweenMarks = course.headingsBetweenMarks(boat.getLastRoundedMarkIndex(),boat.getLastRoundedMarkIndex()+1);
        boolean onTack = false;
        boolean onGybe = false;

        if(!boat.isSailsIn()){
        if(MathUtils.pointBetweenTwoAngle(windDirection, polarTable.getOptimumTWA(true), headingBetweenMarks)){
            onTack = true;
            double optimumTackingVMG = polarTable.getOptimumVMG(onTack);
            boat.setCurrentVMG(optimumTackingVMG);
            boat.setCurrentSpeed(optimumTackingVMG / Math.cos(Math.toRadians(polarTable.getOptimumTWA(onTack))));
        } else if(MathUtils.pointBetweenTwoAngle((windDirection + 180) % 360, 180 - polarTable.getOptimumTWA(false), headingBetweenMarks)) {
            onGybe = true;
            double optimumGybingVMG = polarTable.getOptimumVMG(onTack);
            boat.setCurrentVMG(optimumGybingVMG * (-1.0));
            boat.setCurrentSpeed(optimumGybingVMG/ Math.cos(Math.toRadians(polarTable.getOptimumTWA(onTack))));
        } else {
            boat.setCurrentSpeed(boat.updateBoatSpeed(course));
            boat.setCurrentVMG(boat.getCurrentSpeed());
        }} else {
            boat.setCurrentVMG(boat.getCurrentSpeed());
        }

        CompoundMark nextMark = courseOrder.get(boat.getLastRoundedMarkIndex()+1);
        Coordinate nextMarkPosition = nextMark.getPosition();
        Coordinate boatPosition = boat.getCurrentPosition();
        double distanceGained = timePassed * boat.getCurrentSpeed();
        double distanceLeftInLeg = boatPosition.greaterCircleDistance(nextMark.getPosition());

        while(distanceGained > distanceLeftInLeg && boat.getLastRoundedMarkIndex() < courseOrder.size()-1) {
            distanceGained -= distanceLeftInLeg;
            boatPosition.update(nextMarkPosition.getLat(), nextMarkPosition.getLon());
            boat.setLastRoundedMarkIndex(boat.getLastRoundedMarkIndex() + 1);

            if(boat.getLastRoundedMarkIndex() < courseOrder.size()-1){
                boat.setHeading(course.headingsBetweenMarks(boat.getLastRoundedMarkIndex(), boat.getLastRoundedMarkIndex() + 1));
                nextMark = courseOrder.get(boat.getLastRoundedMarkIndex() +1 );
                nextMarkPosition = nextMark.getPosition();
                distanceLeftInLeg = boatPosition.greaterCircleDistance(nextMark.getPosition());
            } else {
                boat.setStatus(BoatStatus.FINISHED);
                boat.setCurrentSpeed(0);
                return;
            }
        }

        if (!onTack) boat.setLastTackMarkPassed(0);
        if (!onGybe) boat.setLastGybeMarkPassed(0);

        if(onTack || onGybe) {
            double alphaAngle = getAlphaAngle(windDirection - 180, headingBetweenMarks, onTack);
            Coordinate tackingPosition = tackingUpdateLocation(distanceGained, courseOrder, onTack, alphaAngle, boat);
            boatPosition.update(tackingPosition.getLat(), tackingPosition.getLon());
        } else {
            //Move the remaining distance in leg
            double percentGained = (distanceGained / distanceLeftInLeg);
            double newLat = boat.getCurrentLat() + percentGained * (nextMarkPosition.getLat() - boat.getCurrentLat());
            double newLon = boat.getCurrentLon() + percentGained * (nextMarkPosition.getLon() - boat.getCurrentLon());
            boatPosition.update(newLat, newLon);
            System.out.println(distanceGained);
        }
    }

    /**
     * @param windDirection the current wind direction for the course
     * @param bearing
     * @param onTack whether calculateOptimumTack is happening, or calculateOptimumGybe
     * @return the alpha angle
     */
    private double getAlphaAngle(double windDirection, double bearing, boolean onTack) {
        double alphaAngle;
        if(bearing <= (windDirection + 90.0)){
            alphaAngle = Math.abs(bearing - windDirection) % 360;
        } else {
            alphaAngle = (360 + windDirection - bearing) % 360;
        }
        return onTack ? alphaAngle : 180 - alphaAngle;
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
            TrueWindAngle = polarTable.getOptimumTWA(onTack);
        } else {
            TrueWindAngle = 180 - polarTable.getOptimumTWA(false);
        }
        CompoundMark nextMark = courseOrder.get(boat.getLastRoundedMarkIndex()+1);
        double lengthOfTack = calculateLengthOfTack(TrueWindAngle,alphaAngle,courseOrder,boat);
        ArrayList<CompoundMark> tackingMarks = new ArrayList<>(); //Arraylist to hold 'mock' marks for the boat to tack against
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

    public double calculateLengthOfTack(double TrueWindAngle, double alphaAngle,ArrayList<CompoundMark> courseOrder, Boat boat){
        CompoundMark nextMark = courseOrder.get(boat.getLastRoundedMarkIndex()+1);
        double lengthOfLeg = courseOrder.get(boat.getLastRoundedMarkIndex()).getPosition().greaterCircleDistance(nextMark.getPosition());
        double betaAngle = (2*TrueWindAngle) - alphaAngle;
        double lengthOfTack = ((lengthOfLeg* Math.sin(Math.toRadians(betaAngle)))/Math.sin(Math.toRadians(180 - 2*TrueWindAngle)))/2.0;
        return lengthOfTack;
    }

    private void prepareBoatForRace(Boat boat) {
        RaceLine startingLine = race.getCourse().getStartLine();
        boat.setPosition(startingLine.getPosition());
        boat.setHeading(race.getCourse().headingsBetweenMarks(0, 1));
        boat.setLastRoundedMarkIndex(0);
        boat.setStatus(BoatStatus.PRERACE);
    }

    /**
     * Gives each boat in the race a randomized speed so that each race is a bit different.
     */
    private void setRandomBoatSpeeds(){
        for (Boat boat : potentialCompetitors) {
            Random random = new Random();
            double rangeMin = 15.0;
            double rangeMax = 25.0;
            double speed = rangeMin + (rangeMax - rangeMin) * random.nextDouble();
            boat.setMaxSpeed(speed);
            boat.maximiseSpeed();
        }
    }

    /**
     * Sets boat speeds to zero
     */
    private void setInitialBoatSpeeds(){
        for (Boat boat : potentialCompetitors) {
            boat.setCurrentSpeed(0);
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
            double testTime = dist / boat.getCurrentVMG(); // 10 is the VMG estimate of the boats
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

    /**
     * generates a random windspeed and wind angle within a range of the current speed and angle
     */
    public void generateWind(){
        double range = 0.05;
        double maxSpeed = race.getCourse().getTrueWindSpeed() + range;
        double minSpeed = race.getCourse().getTrueWindSpeed() - range;

        double maxAngle = race.getCourse().getWindDirection() + range;
        double minAngle = race.getCourse().getWindDirection() - range;
        double speed = ThreadLocalRandom.current().nextDouble(minSpeed, maxSpeed);
        double angle = ThreadLocalRandom.current().nextDouble(minAngle, maxAngle);

        race.getCourse().setTrueWindSpeed(speed);
        race.getCourse().setWindDirection(angle);
    }

    /**
     * checks a boat to see if is colliding with another boat or mark
     * @param boat
     * @return boolean of collision
     */
    private boolean checkForCollision(Boat boat){
        boolean collision = false;
        for(Boat otherBoat : race.getCompetitors()){
            if(collisionOfBounds(boat.getCurrentPosition(), otherBoat.getCurrentPosition(), 16) && boat != otherBoat){
                collision = true;
                otherBoat.setBoatColliding(true);
                //System.out.println("EXPLOSION!!!!!!!!!!!!! YOUR BOAT IS SINKING, ABORT!!!!! !@#$%@*&^#$@ Collision of boat");
            }
        }
        for(Mark mark : race.getCourse().getAllMarks().values()){
            if(collisionOfBounds(boat.getCurrentPosition(), mark.getPosition(), 10)){
                collision = true;
                boat.setMarkColliding(true);
                //System.out.println("HOLLY HECK YOU HIT A MARK, GET YOUR RUBBER DINGY READY, YOU'VE LOST THIS RACE FOR SURE Collision of mark");
                markAvoider(boat);
            }
        }
        return collision;
    }

    private void markAvoider(Boat boat){
        boat.setHeading(boat.getHeading() - 5);
        boat.setCurrentSpeed(boat.getCurrentSpeed() - 0.8);
        if(boat.getCurrentSpeed() < 0){
            boat.setCurrentSpeed(0);
        }
        Coordinate currPos = boat.getCurrentPosition();
        Coordinate newPos = currPos.coordAt(0.01, (boat.getHeading() + 180) % 360);
        boat.setPosition(newPos);
    }

    /**
     * takes two circles and calculates if there is a collision
     * @param object1LatLon
     * @param object2LatLon
     * @return boolean of collision
     */
    private boolean collisionOfBounds(Coordinate object1LatLon, Coordinate object2LatLon, double sensitivity){
        CanvasCoordinate object1 = DisplayUtils.convertFromLatLon(object1LatLon);
        CanvasCoordinate object2 = DisplayUtils.convertFromLatLon(object2LatLon);
        double dx = object1.getX() - object2.getX();
        double dy = object2.getY() - object1.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < sensitivity;
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

    /**
     * Randomly generates an initial wind speed between race regulations of 6-24 knots
     */
    private void intialWindSpeedGenerator(){
        Random random = new Random();
        initialWindSpeed = MIN_WIND_SPEED + (MAX_WIND_SPEED - MIN_WIND_SPEED) * random.nextDouble();
    }


}
