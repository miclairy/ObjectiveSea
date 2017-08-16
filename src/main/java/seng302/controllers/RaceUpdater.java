package seng302.controllers;

import seng302.data.BoatStatus;
import seng302.data.RaceStatus;
import seng302.data.RaceVisionXMLParser;
import seng302.models.*;
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
    private final double PREPATORY_SIGNAL_TIME_IN_MS = (1000 * 60 * 2);
    private final double MIN_WIND_SPEED = 6.0;
    private final double MAX_WIND_SPEED = 24.0;
    private double initialWindSpeed;
    private final int MAX_BOATS_IN_RACE = 6;
    private Race race;
    private PolarTable polarTable;
    private Collection<Boat> potentialCompetitors;
    private CollisionManager collisionManager;
    private Coordinate startingPosition;
    private boolean serverRunning;

    public RaceUpdater(String selectedCourse){
        collisionManager = new CollisionManager();
        //set race up with default files
        initialWindSpeedGenerator();
        RaceVisionXMLParser raceVisionXMLParser = new RaceVisionXMLParser();
        raceVisionXMLParser.setCourseFile(selectedCourse);
        potentialCompetitors = raceVisionXMLParser.importDefaultStarters();
        race = raceVisionXMLParser.importRace();
        Course course = race.getCourse();
        course.setTrueWindSpeed(initialWindSpeed);
        course.setWindDirection(course.getWindDirectionBasedOnGates());
        this.serverRunning = true;
        initialize();
    }

    public RaceUpdater(Race race) {
        this.race = race;
        initialize();
    }

    public void initialize(){
        //for now we assume all boats racing are AC35 class yachts such that we can use the polars we have for them
        this.polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
        race.updateRaceStatus(RaceStatus.PRESTART);
        long currentTime = Instant.now().toEpochMilli();
        race.setCurrentTimeInEpochMs(currentTime);
        race.setStartTimeInEpochMs(currentTime + (1000 * 60 * 3)); //3 minutes from now
    }


    /**
     * Adds a competitor from the potential competitors collection into the race
     * @return the id of the added competitor, or -1 if max number reached.
     */
    public int addCompetitor() {
        if (potentialCompetitors.iterator().hasNext()) {
            Boat newCompetitor = potentialCompetitors.iterator().next();
            potentialCompetitors.remove(newCompetitor);
            race.addCompetitor(newCompetitor);
            prepareBoatForRace(newCompetitor);
            return newCompetitor.getId();
        } else {
            return -1;
        }
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
        Course course = race.getCourse();
        while (!race.getRaceStatus().isRaceEndedStatus() && serverRunning) {
            boolean atLeastOneBoatNotFinished = false;
            double raceSecondsPassed = SECONDS_PER_UPDATE * scaleFactor;
            long millisBeforeStart = race.getStartTimeInEpochMs() - race.getCurrentTimeInEpochMs();
            race.setCurrentTimeInEpochMs(race.getCurrentTimeInEpochMs()
                    + (long)(TimeUtils.secondsToMilliseconds(raceSecondsPassed)));
            generateWind();
            if (race.hasStarted() || race.getRaceStatus().equals(RaceStatus.PREPARATORY)) {
                collisionManager.checkForCollisions(race);
            }

            for (Boat boat : race.getCompetitors()) {
                if(race.hasStarted() || race.getRaceStatus().equals(RaceStatus.PREPARATORY)){
                    if (collisionManager.boatIsInCollision(boat)) {
                        //revert the last location update as it was a collision
                        updateLocation(-TimeUtils.convertSecondsToHours(raceSecondsPassed), boat);
                        boat.setCurrentSpeed(boat.getCurrentSpeed() - 0.8);
                    }
                    adjustSpeed(boat);
                    updateLocation(TimeUtils.convertSecondsToHours(raceSecondsPassed), boat);
                    boat.updateBoatHeading(raceSecondsPassed);
                    if (course.getCourseOrder().size() > 0) {
                        checkMarkRounding(boat, course);
                    }
                    calculateTimeAtNextMark(boat);
                } else {
                    if(millisBeforeStart < WARNING_SIGNAL_TIME_IN_MS && millisBeforeStart > PREPATORY_SIGNAL_TIME_IN_MS) {
                        race.updateRaceStatus(WARNING);
                    }else if(millisBeforeStart < PREPATORY_SIGNAL_TIME_IN_MS && millisBeforeStart > 0){
                        race.updateRaceStatus(RaceStatus.PREPARATORY);
                    }
                }
                if (!boat.getStatus().equals(BoatStatus.FINISHED) && !boat.getStatus().equals(BoatStatus.DNF)) {
                    atLeastOneBoatNotFinished = true;
                }
                if (boat.getStatus().equals(BoatStatus.DNF)) {
                    boat.setCurrentSpeed(0);
                }
                if(boat.getStatus().equals(BoatStatus.PRERACE) && race.getRaceStatus().equals(RaceStatus.STARTED)){
                    boat.setStatus(BoatStatus.RACING);
                }
            }
            if (millisBeforeStart < 0 && race.getRaceStatus().equals(RaceStatus.PREPARATORY)){
                race.updateRaceStatus(RaceStatus.STARTED);
                for(Boat boat : race.getCompetitors()){
                    if(!boat.getStatus().equals(BoatStatus.DNF)){
                        boat.setStatus(BoatStatus.RACING);
                    }
                }
            }

            if (race.getCompetitors().size() > 0 && !atLeastOneBoatNotFinished) {
                race.updateRaceStatus(RaceStatus.TERMINATED);
            }

            try {
                Thread.sleep((long) (SECONDS_PER_UPDATE * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Adjusts the speed of the boat based on sails luffing or damage taken
     * @param boat the boat who's speed we are adjusting
     */
    private void adjustSpeed(Boat boat) {
        if(boat.isSailsIn() && boat.getCurrentSpeed() > 0){
            boat.setCurrentSpeed(boat.getCurrentSpeed() - 0.2);
            if(boat.getCurrentSpeed() < 0) boat.setCurrentSpeed(0);
        } else if(!boat.isSailsIn()){
            boat.setMaxSpeed(boat.updateBoatSpeed(race.getCourse())-boat.getDamageSpeed());
            if(boat.getCurrentSpeed() < boat.getMaxSpeed()){
                boat.setCurrentSpeed(boat.getCurrentSpeed() + 0.1);
            }
            if(boat.getCurrentSpeed() > boat.getMaxSpeed() + 1) {
                boat.setCurrentSpeed(boat.getMaxSpeed());
            }
        }
    }

    private void checkMarkRounding(Boat boat, Course course) {
        CompoundMark currentMark = course.getCourseOrder().get(boat.getLastRoundedMarkIndex() + 1);
        CompoundMark previousMark = null;
        CompoundMark nextMark = null;
        if (!currentMark.isStartLine()){
            previousMark = course.getCourseOrder().get(boat.getLastRoundedMarkIndex());
        }
        if(!currentMark.isFinishLine()){
            nextMark = course.getCourseOrder().get(boat.getLastRoundedMarkIndex() + 2);
        }
        if(currentMark.isStartLine()){
            if(RoundingMechanics.boatPassedThroughCompoundMark(boat, course.getStartLine(), startingPosition, true)){
                boat.setLastRoundedMarkIndex(boat.getLastRoundedMarkIndex() + 1);
            }
        } else if (currentMark.isFinishLine()) {
            if(RoundingMechanics.boatPassedThroughCompoundMark(boat, course.getFinishLine(), previousMark.getPosition(), true)) {
                boat.setCurrentSpeed(0);
                boat.setStatus(BoatStatus.FINISHED);
            }
        } else if (!currentMark.hasTwoMarks()){
            RoundingMechanics.boatHeadingToMark(boat, currentMark, previousMark, nextMark);
        } else if(currentMark.hasTwoMarks()) {
            RoundingMechanics.boatHeadingToGate(boat, currentMark, previousMark, nextMark);
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
        boat.setPosition(new Coordinate(newPos.getLat(), newPos.getLon()));
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
        setStartingPosition(boat);
        boat.updateBoatSpeed(race.getCourse());
        boat.setLastRoundedMarkIndex(-1);
        boat.setStatus(BoatStatus.PRERACE);
    }

    /**
     * Spreads the starting positions of the boats over the start line
     */
    private void setStartingPosition(Boat boat){
        RaceLine startingLine = race.getCourse().getStartLine();
        CompoundMark startingEnd2 = new CompoundMark(-2, "", new Mark(-2, "", startingLine.getMark1().getPosition()));
        CompoundMark startingEnd1 = new CompoundMark(-1, "", new Mark(-1, "", startingLine.getMark2().getPosition()));
        double heading1 = (MathUtils.calculateBearingBetweenTwoPoints(startingEnd1,startingEnd2));
        double heading2 = (MathUtils.calculateBearingBetweenTwoPoints(startingEnd2,startingEnd1));
        double heading3;
        if(heading1 < heading2) {
            heading3 = heading1;
        } else {
            heading3 = heading2;
        }

        Coordinate startPosition1 = startingEnd1.getPosition().coordAt(0.2, heading3 + 90);
        Coordinate startPosition2 = startingEnd2.getPosition().coordAt(0.2, heading3 + 90);

        if(!MathUtils.boatBeforeStartline(startPosition1, startingLine, race.getCourse().getCourseOrder().get(1))){
            startPosition1 = startingEnd1.getPosition().coordAt(0.2, heading3 + 270);
            startPosition2 = startingEnd2.getPosition().coordAt(0.2, heading3 + 270);
        }

        Double dLat = (startPosition2.getLat() - startPosition1.getLat()) / (MAX_BOATS_IN_RACE / 2);
        Double dLon = (startPosition2.getLon() - startPosition1.getLon()) / (MAX_BOATS_IN_RACE / 2);
        if (boat.getId() % 2 == 0){
            dLat *= -1;
            dLon *= -1;
        }
        Double curLat = startPosition1.getLat() + (dLat * race.getCompetitors().size());
        Double curLon = startPosition1.getLon() + (dLon * race.getCompetitors().size());
        boat.setPosition(curLat, curLon);
        boat.setHeading(boat.getCurrentPosition().headingToCoordinate(startingLine.getPosition()));
        startingPosition = new Coordinate(curLat, curLon);
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
            double testTime = dist / boat.getCurrentVMG();
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
        //TODO: wind angle has been changed to a hard coded number.
        double range = 0.05;
        double maxSpeed = race.getCourse().getTrueWindSpeed() + range;
        double minSpeed = race.getCourse().getTrueWindSpeed() - range;

        //double maxAngle = race.getCourse().getWindDirection() + range;
        //double minAngle = race.getCourse().getWindDirection() - range;
        double speed = ThreadLocalRandom.current().nextDouble(minSpeed, maxSpeed);
        double angle = race.getCourse().getWindDirection();//ThreadLocalRandom.current().nextDouble(minAngle, maxAngle);

        race.getCourse().setTrueWindSpeed(speed);
        race.getCourse().setWindDirection(angle);
    }

    /**
     * changes a boat's heading and speed when it collides into a mark
     * @param boat the boat that has collided
     */
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
     * Sets the race status to started so we don't have to wait around for the prerace countdown
     * Currently used in tutorial mode.
     */
    public void skipPrerace() {
        race.updateRaceStatus(STARTED);
    }

    /**
     * Randomly generates an initial wind speed between race regulations of 6-24 knots
     */
    private void initialWindSpeedGenerator(){
        Random random = new Random();
        initialWindSpeed = MIN_WIND_SPEED + (MAX_WIND_SPEED - MIN_WIND_SPEED) * random.nextDouble();
    }

    public void stopRunning(){
        this.serverRunning = false;
    }

    public CollisionManager getCollisionManager() {
        return collisionManager;
    }
}
