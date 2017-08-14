package seng302.models;


import seng302.data.StartTimingStatus;

import seng302.data.BoatStatus;
import seng302.utilities.MathUtils;
import seng302.utilities.PolarReader;
import seng302.utilities.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.min;
import static java.lang.Double.max;
import static java.lang.StrictMath.abs;
import static seng302.utilities.MathUtils.pointBetweenTwoAngle;

/**
 * Class to encapsulate properties associated with a boat.
 */

public class Boat extends Observable implements Comparable<Boat>{

    private final double KNOTS_TO_MMS_MULTIPLIER = 514.444;

    private String name;
    private String nickName;
    private double currentSpeed;
    private double currentVMG;
    private int currPlacing;
    private int leg;

    private Coordinate currentPosition;
    private Coordinate previousPosition;
    private PolarTable polarTable;

    private int lastRoundedMarkIndex;
    private long lastRoundedMarkTime;
    private int lastTackMarkPassed;
    private int lastGybeMarkPassed;
    private boolean finished;
    private double heading;
    private double targetHeading;
    private double maxSpeed;
    private double boatHealth = 100;
    private double damageSpeed;
    private boolean boatCheck = false;
    private double boatPenalty;
    private boolean justFinished = true;
    private double timeSinceLastCollision = 0;


    private double finishTime;
    private double finalTime;
    private boolean lastPlayerDirection = true; //true = Clockwise / false = AntiClockwise

    private int penaltyCount;
    private boolean markColliding;
    private boolean boatColliding;

    private BoatStatus status = BoatStatus.UNDEFINED;
    private StartTimingStatus timeStatus = StartTimingStatus.ONTIME;

    private List<Coordinate> pathCoords;
    private long timeTillMark;
    private long timeTillFinish;
    private Integer id;
    private boolean sailsIn = false;
    private boolean inGate = false;


    private double TWAofBoat;
    private boolean rotate;
    private boolean tackOrGybe;
    private double totalRotatedAmount;
    private double currRotationAmount;
    private int rotateDirection;

    public Boat(Integer id, String name, String nickName, double speed) {
        this.id = id;
        this.name = name;
        this.nickName = nickName;
        this.maxSpeed = speed;
        this.finished = false;
        this.lastRoundedMarkIndex = -1;
        this.pathCoords = new ArrayList<>();
        this.currentPosition = new Coordinate(0,0);
        this.previousPosition = new Coordinate(0,0);
    }

    /**
     * Sets the latitude and longitude of the boat
     * @param lat the latitude of the boat
     * @param lon the longitude of the boat
     */
    public void setPosition(double lat, double lon){
        previousPosition = new Coordinate(currentPosition.getLat(), currentPosition.getLon());
        currentPosition.setLat(lat);
        currentPosition.setLon(lon);
    }

    /**
     * Overload for the set position method taking a Coordinate
     * @param coord a Coordinate object to copy position from
     */
    public void setPosition(Coordinate coord) {
        previousPosition = new Coordinate(currentPosition.getLat(), currentPosition.getLon());
        currentPosition.setLat(coord.getLat());
        currentPosition.setLon(coord.getLon());
    }

    public void setStatus(BoatStatus status) {
        this.status = status;
    }

    public Coordinate getCurrentPosition() {
        return currentPosition;
    }

    public Coordinate getPreviousPosition() {
        return previousPosition;
    }

    /**
     * Compares boat objects based on the index of last mark rounded in race order and if that is equals, compares
     * based on time (lower time first).
     * @param otherBoat The other boat that this boat is being compared to
     * @return Negative number if this boat comes before other boat in order, 0 if equal or positive number
     * if this boat comes after other boat in order.
     */
    @Override
    public int compareTo(Boat otherBoat){
        if(lastRoundedMarkIndex != otherBoat.getLastRoundedMarkIndex()){
            return Integer.compare(otherBoat.getLastRoundedMarkIndex(), lastRoundedMarkIndex);
        }
        return Long.compare(lastRoundedMarkTime, otherBoat.getLastRoundedMarkTime());
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return this.name;
    }

    public String getNickName() {
        return nickName;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    private void checkPenaltySpeed() {
        double boatPenalty = 100 - boatHealth;
        if(boatPenalty > 0 && boatPenalty < 100) {
            damageSpeed = boatPenalty / 10;
        } else if(boatPenalty == 0) {
            damageSpeed = 0;
        }
    }

    public void addDamage(int damage) {
        if((boatHealth - damage) > 0) {
            boatHealth -= damage;
        } else {
            boatHealth = 0;
            status = BoatStatus.DNF;
        }
        checkPenaltySpeed();
    }

    public void addPenalty(double penalty) {
        boatPenalty += penalty;
    }

    public double getDamageSpeed() {
        return damageSpeed;
    }

    public int getSpeedInMMS(){
        return (int) (this.currentSpeed * KNOTS_TO_MMS_MULTIPLIER);
    }

    public int getLastRoundedMarkIndex() {
        return lastRoundedMarkIndex;
    }

    public void setLastRoundedMarkIndex(int lastRoundedMarkIndex) {
        this.lastRoundedMarkIndex = lastRoundedMarkIndex;
    }

    public long getLastRoundedMarkTime() {
        return lastRoundedMarkTime;
    }

    public void setLastRoundedMarkTime(long lastRoundedMarkTime) {
        this.lastRoundedMarkTime = lastRoundedMarkTime;
    }

    public double getCurrentLat() {
        return currentPosition.getLat();
    }

    public double getCurrentLon() {
        return currentPosition.getLon();
    }

    public boolean isFinished() {
        return status.equals(BoatStatus.FINISHED);
    }

    public double getHeading() {
        return heading;
    }

    public int getCurrPlacing(){return currPlacing;}

    public void setCurrPlacing(int placing){
        this.currPlacing = placing;
        setChanged();
        notifyObservers();
    }

    /**
     * Sets the boats heading to the current value. If the heading has changed,
     * a new record is added the pathCoords list
     * @param heading the new heading
     * */
    public void setHeading(double heading) {
        this.heading = ((heading + 360)%360);
    }

    public List getPathCoords() {
        return Collections.unmodifiableList(new ArrayList<>(pathCoords));
    }

    public void addPathCoord(Coordinate newCoord){
        this.pathCoords.add(newCoord);
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setTWAofBoat(double TWAofBoat) {
        this.TWAofBoat = TWAofBoat;
    }

    public double getTWAofBoat() {
        return TWAofBoat;
    }

    public void setCurrentSpeed(double speed) {
        this.currentSpeed = max(0.0, speed);
    }

    public long getTimeAtNextMark() {
        return timeTillMark;
    }

    public void setTimeTillMark(long timeTillMark) {
        this.timeTillMark = timeTillMark;
    }

    public double getCurrentVMG() {
        return currentVMG;
    }

    public BoatStatus getStatus() {
        return status;
    }

    public void setMaxSpeed(double maxSpeed) {
        if(maxSpeed <= 0) {
            maxSpeed = 0;
            this.boatCheck = true;
        }
        this.maxSpeed = maxSpeed;
    }

    public int getPenaltyCount(){return penaltyCount;}

    public void addPenalty(int penaltyCount) {this.penaltyCount = penaltyCount;}

    public boolean isMarkColliding() {return markColliding;}

    public boolean isBoatColliding() {return  boatColliding;}

    public void setMarkColliding(boolean colliding) {markColliding = colliding;}

    public PolarTable getPolarTable() {
        return polarTable;
    }

    public void setBoatColliding(boolean colliding) {boatColliding = colliding;}

    public long getTimeTillFinish() {
        return timeTillFinish;
    }

    public void setTimeTillFinish(long timeTillFinish) {
        this.timeTillFinish = timeTillFinish;
    }

    public StartTimingStatus getTimeStatus() {
        return timeStatus;
    }

    public void setTimeStatus(StartTimingStatus timeStatus) {
        this.timeStatus = timeStatus;
    }

    public void setCurrentVMG(double currentVMGSpeed) {
        this.currentVMG = currentVMGSpeed;
    }

    public void setLastGybeMarkPassed(int lastGybeMarkPassed) {
        this.lastGybeMarkPassed = lastGybeMarkPassed;
    }

    public void setLastTackMarkPassed(int lastTackMarkPassed) {
        this.lastTackMarkPassed = lastTackMarkPassed;
    }

    public int getLastTackMarkPassed() {
        return lastTackMarkPassed;
    }

    public int getLastGybeMarkPassed() {
        return lastGybeMarkPassed;
    }

    public boolean isTackOrGybe() {
        return tackOrGybe;
    }

    public synchronized Boolean isSailsIn() {
        return sailsIn;
    }

    public void setLeg(int leg){
        if(lastRoundedMarkIndex == -1){
            if(status.equals(BoatStatus.FINISHED)){
                lastRoundedMarkIndex = leg;
            } else {
                lastRoundedMarkIndex = leg - 1;
            }
        }
        this.leg = leg;
    }

    public int getLeg(){return leg;}


    /**
     * Calculates boat's VMG
     * @param course the course the boat is on
     * @return the VMG of the boat (in direction of next mark)
     */
    public double calculateVMGToMark(Course course){
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        Coordinate markLocation;
        if(lastRoundedMarkIndex + 1 < courseOrder.size()){
            markLocation = courseOrder.get(lastRoundedMarkIndex + 1).getPosition();
        } else {
            markLocation = courseOrder.get(courseOrder.size() - 1).getPosition();
        }

        double lineBearing = currentPosition.headingToCoordinate(markLocation);
        double angle = Math.abs(heading - lineBearing);

        return Math.cos(Math.toRadians(angle)) * currentSpeed;
    }


    /**
     * Determines boat's heading (upwind or downwind0 and calculates optimum headings
     * Returns optimum headings as a pair. if boat is in deadzone (not tacking or gybing) the heading is returned
     * @param course
     * @param polarTable
     * @return OptimumHeadings
     */
    public OptimumHeadings getOptimumHeadings(Course course, PolarTable polarTable) {
        double TWA = Math.abs(((course.getWindDirection() - heading)));
        double TWD = course.getWindDirection();
        double optimumTWA;
        boolean tacking = isTacking(TWA);
        boolean gybing = isGybing(TWA);

        if(tacking) {
            optimumTWA = polarTable.getOptimumTWA(true);
        } else if (gybing) {
            optimumTWA = polarTable.getOptimumTWA(false);
        } else {
            return new OptimumHeadings(heading, heading);
        }
        double optimumHeadingA = (TWD - optimumTWA + 360) % 360;
        double optimumHeadingB = (TWD + optimumTWA + 360) % 360;

        return new OptimumHeadings(optimumHeadingA, optimumHeadingB);
    }

    public boolean isInGate() {
        return inGate;
    }

    public void setInGate(boolean inGate) {
        this.inGate = inGate;
    }


    /**
     * Class to store optimum headings as a pair
     */
    private class OptimumHeadings {
        public double headingA;
        public double headingB;

        public OptimumHeadings(double headingA, double headingB) {
            this.headingA = headingA;
            this.headingB = headingB;
        }
    }


    /**
     * Function to calculate boats closest VMG heading using the course wind direction and polar table.
     * Checks if boat is heading upwind or downwind, then finds the optimal headings rightOfTWDAngle and leftOfTWDAngle.
     * Then returns the optimum heading that is closest to the boat's heading.
     * @param course
     * @param polarTable
     * @return optimum heading
     */
    public double getVMGHeading(Course course, PolarTable polarTable) {
        OptimumHeadings optimumHeadings = getOptimumHeadings(course, polarTable);
        double TWA = Math.abs(((course.getWindDirection() - heading)));
        if(isTacking(TWA)) {
            if(inRange(optimumHeadings.headingA, optimumHeadings.headingB, heading)) {
                return -1;
            }
        }
        if ((int) optimumHeadings.headingA == (int) heading && (int) optimumHeadings.headingB == (int) heading){
            return -1;
        }
        double angleToOptimumA = abs( heading - optimumHeadings.headingA);
        double angleToOptimumB = abs( heading - optimumHeadings.headingB);

        if (angleToOptimumA < angleToOptimumB) {
            return optimumHeadings.headingA;
        } else {
            return optimumHeadings.headingB;
        }
    }

    public void VMG(Course course, PolarTable polarTable){
        targetHeading = getVMGHeading(course, polarTable);
        if (targetHeading == -1){
            targetHeading = heading;
        }
        rotate = true;
    }


    public void tackOrGybe(Course course, PolarTable polarTable) {
        targetHeading = getTackOrGybeHeading(course, polarTable);
        if (targetHeading == -1){
            targetHeading = heading;
        }
        totalRotatedAmount = min(360 - abs(targetHeading - heading), abs(targetHeading - heading));
        currRotationAmount = 0;
        double TWA = Math.abs(((course.getWindDirection() - heading)));
        if (TWA < 89 || TWA < 270 && TWA > 180){
            rotateDirection = -1;
        } else {
            rotateDirection = 1;
        }
        tackOrGybe = true;
    }

    /**
     * Function to change heading of boat when the tack/gybe button is pressed
     * If already on an optimum heading it switches to the "opposite" optimum
     * If in dead zones or no sail zone (heading into wind) the heading is unchanged
     * @param course
     * @param polarTable
     * @return new tack/gybe heading
     */
    public double getTackOrGybeHeading(Course course, PolarTable polarTable) {
        OptimumHeadings optimumHeadings = getOptimumHeadings(course, polarTable);
        double TWA = Math.abs(((course.getWindDirection() - heading)));
        double optimumHeadingA = optimumHeadings.headingA;
        double optimumHeadingB = optimumHeadings.headingB;

        if(heading - 1 <= optimumHeadingA && optimumHeadingA <= heading + 1) {
            return optimumHeadingB;
        } else if (heading - 1 <= optimumHeadingB && optimumHeadingB <= heading + 1) {
            return optimumHeadingA;
        }

        if(isTacking(TWA)) {
            if(inRange(optimumHeadings.headingA, optimumHeadings.headingB, heading)) {
                return -1;
            }
        }

        if ((int) optimumHeadings.headingA == (int) heading && (int) optimumHeadings.headingB == (int) heading){
            return -1;
        }

        double angleToOptimumA = abs( heading - optimumHeadingA);
        double angleToOptimumB = abs( heading - optimumHeadingB);

        if (angleToOptimumA <= angleToOptimumB) {
            return optimumHeadingB;
        } else {
            return optimumHeadingA;
        }
    }

    /**
     * Checks if point is in range of start and end
     * @param start
     * @param end
     * @param point
     * @return true if in range
     */
    private boolean inRange(double start, double end, double point){
        return (point + 360 - start) % 360 <= (end + 360 - start) % 360;
    }

    private boolean isTacking(double TWA) {
        return TWA < 89 || TWA > 271;
    }

    private boolean isGybing(double TWA) {
        return TWA > 91 && TWA < 269;
    }

    /**
     * This method takes the current wind angle and checks to see what side of the compass the players boat is on,
     * as to turn the boat the right direction towards the appropriate upWind or downWind.
     * This 'windAngleCheck == playerHeading' statement, takes the last direction the boat was turning,
     * either clockwise or anti clockwise, and turns the boat again in that direction, as the boat will
     * sometimes be facing the exact wind direction and not know which way to turn.
     * @param windAngle windangle of the course
     */
    public void headingChange(double windAngle) {

        double DELTA = 0.00001;
        heading += 360;
        double windAngleCheck = windAngle + 360;

        if(heading <= windAngleCheck && heading >= windAngleCheck-2) {
            heading = windAngleCheck;

        } else if(heading >= windAngleCheck && heading <= windAngleCheck+2) {
            heading = windAngleCheck;

        } else if ((windAngleCheck > heading && windAngleCheck - 180 < heading && abs(windAngleCheck - 180 - heading) > DELTA) ||           //1
                (windAngleCheck < heading && windAngleCheck + 180 < heading && abs(windAngleCheck + 180 - heading) > DELTA)) {       //2
            heading += 3;
            lastPlayerDirection = true;

        } else if ((windAngleCheck > heading && windAngleCheck - 180 > heading && abs(windAngleCheck - 180 - heading) > DELTA) ||    //3
                (windAngleCheck < heading && windAngleCheck + 180 > heading && abs(windAngleCheck + 180 - heading) > DELTA)) {       //4
            heading -= 3;
            lastPlayerDirection = false;

        } else if (abs(windAngleCheck - heading) < DELTA ||
                abs((windAngleCheck - 180) - heading) < DELTA ||
                abs((windAngleCheck + 180) - heading) < DELTA) {
            if (lastPlayerDirection) {
                heading += 3;
            } else {
                heading -= 3;
            }
        }

        if (heading >= 720) {
            heading -= 720;
        } else if (heading >= 360) {
            heading -= 360;
        }
    }


    public synchronized void changeSails() {
        sailsIn = !sailsIn;
    }

    public void setSailsIn(boolean sailsIn) {
        this.sailsIn = sailsIn;
    }

    public void upWind(double windAngle){
        rotate = false;
        tackOrGybe = false;
        headingChange(windAngle);
    }

    public void downWind(double windAngle){
        rotate = false;
        tackOrGybe = false;
        double newWindAngle = windAngle;
        if(newWindAngle > 180) {
            newWindAngle -= 360;
        }
        headingChange(newWindAngle +180);
    }

    /**
     * A function to update the boat currentSpeed whenever the windspeed or boat heading is changed
     * @param course
     * @return the new speed for the boat
     */
    public double updateBoatSpeed(Course course){
        double TWS = course.getTrueWindSpeed();
        double windDirection = course.getWindDirection();
        double TWA = Math.abs(((windDirection - heading)));
        if(TWA > 180) {
            TWA = 360 - TWA;
        }
        this.polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), course);
        ArrayList<Polar> interpPolars = polarTable.TWSForInterp((int) course.getTrueWindSpeed(), PolarReader.getPolarsForAC35Yachts());

        Polar polar1 = interpPolars.get(0);
        Polar polar3 = interpPolars.get(2);
        ArrayList<WindAngleAndSpeed> windAngleAndSpeeds1 = polarTable.TWAForInterp((int) TWA, polar1);
        ArrayList<WindAngleAndSpeed> windAngleAndSpeeds2 = polarTable.TWAForInterp((int) TWA, polar3);

        double TWS0 = polar1.getTWS();
        double TWS1 = polar3.getTWS();
        double TWA0 = windAngleAndSpeeds1.get(0).getWindAngle();
        double TWA1 = windAngleAndSpeeds2.get(2).getWindAngle();
        double z00 = windAngleAndSpeeds1.get(0).getSpeed();
        double z10 = windAngleAndSpeeds1.get(2).getSpeed();
        double z01 = windAngleAndSpeeds2.get(0).getSpeed();
        double z11 = windAngleAndSpeeds2.get(2).getSpeed();

        return MathUtils.bilinearInterpolation(TWS0,TWS1,TWA0,TWA1,z00,z01,z10,z11,TWS,TWA);
    }

    public synchronized double getSailAngle(double windDirection){
        double sailAngle;
        if(!sailsIn){
            sailAngle = windDirection;
        } else {
            double TWA = Math.abs(((windDirection - heading)));
            if(TWA > 180) {
                TWA = 360 - TWA;
            }
            if(TWA > 90) {
                sailAngle = windDirection - 90;
            } else {
                sailAngle = windDirection + 90;
            }
        }
        return sailAngle;
    }

    public void setFinishTime(double finishTime) {
        this.finishTime = finishTime;
        finalTime = (finishTime / 1000.0) - TimeUtils.convertHoursToSeconds(boatPenalty);
    }

    public double getFinalTime() {
        return finalTime;
    }

    public boolean isJustFinished() {
        return justFinished;
    }

    public void setJustFinished(boolean justFinished) {
        this.justFinished = justFinished;
    }


    public double getTimeSinceLastCollision() {
        return timeSinceLastCollision;
    }

    public void setTimeSinceLastCollision(double timeSinceLastCollision) {
        this.timeSinceLastCollision = timeSinceLastCollision;
    }

    public double getTargetHeading() {
        return targetHeading;
    }

    /**
     * Updates the boat heading every loop the race updated run method makes.
     * Calculating how much rotation should occur at each run
     * @param time the time since last calculation
     */
    public void updateBoatHeading(double time){
        double angleOfRotation = 3 * time;
        double headingDiff = targetHeading - heading;
        if (rotate) {
            if (headingDiff > 0) {
                heading += angleOfRotation;
            } else {
                heading -= angleOfRotation;
            }
            if(abs(headingDiff) <= angleOfRotation) {
                heading = targetHeading;
                rotate = false;
            }
        }
        if (tackOrGybe) {
            if (currRotationAmount < totalRotatedAmount){
                heading += angleOfRotation * rotateDirection;
                if (heading < 0){
                    heading = 360;
                } else if (heading > 360){
                    heading = 0;
                }
                currRotationAmount += angleOfRotation;
            }
            if(abs(headingDiff) <= angleOfRotation) {
                heading = targetHeading;
                tackOrGybe = false;
            }
        }
    }
}
