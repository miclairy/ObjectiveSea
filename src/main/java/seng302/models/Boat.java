package seng302.models;


import seng302.data.StartTimingStatus;

import seng302.data.BoatStatus;
import seng302.utilities.MathUtils;
import seng302.utilities.PolarReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.StrictMath.abs;

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
    private PolarTable polarTable;

    private int lastRoundedMarkIndex;
    private long lastRoundedMarkTime;
    private int lastTackMarkPassed;
    private int lastGybeMarkPassed;
    private boolean finished;
    private double heading;
    private double maxSpeed;

    private BoatStatus status = BoatStatus.UNDEFINED;
    private StartTimingStatus timeStatus = StartTimingStatus.ONTIME;

    private List<Coordinate> pathCoords;
    private long timeTillMark;
    private long timeTillFinish;
    private Integer id;
    private AtomicBoolean sailsIn = new AtomicBoolean();

    private double TWAofBoat;

    public Boat(Integer id, String name, String nickName, double speed) {
        this.id = id;
        this.name = name;
        this.nickName = nickName;
        this.maxSpeed = speed;
        this.finished = false;
        this.lastRoundedMarkIndex = -1;
        this.pathCoords = new ArrayList<>();
        this.currentPosition = new Coordinate(0,0);
    }

    /**
     * Sets the latitude and longitude of the boat
     * @param lat the latitude of the boat
     * @param lon the longitude of the boat
     */
    public void setPosition(double lat, double lon){
        currentPosition.setLat(lat);
        currentPosition.setLon(lon);
    }

    /**
     * Overload for the set position method taking a Coordinate
     * @param coord a Coordinate object to copy position from
     */
    public void setPosition(Coordinate coord) {
        currentPosition.setLat(coord.getLat());
        currentPosition.setLon(coord.getLon());
    }

    public void setStatus(BoatStatus status) {
        this.status = status;
    }

    public Coordinate getCurrentPosition() {
        return currentPosition;
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

    public String getNickName() {return nickName;}

    public double getCurrentSpeed() { return this.currentSpeed; }

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
        this.heading = heading;
    }

    public List getPathCoords() {
        return Collections.unmodifiableList(new ArrayList<>(pathCoords));
    }

    public void addPathCoord(Coordinate newCoord){
        this.pathCoords.add(newCoord);
    }

    /**
     * Make currentSpeed be the max currentSpeed.
     */
    public void maximiseSpeed(){
        this.currentSpeed = maxSpeed;
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
        this.currentSpeed = speed;
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
        this.maxSpeed = maxSpeed;
    }

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

    public synchronized Boolean isSailsIn() {
        return sailsIn.get();
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
    public double calculateVMG(Course course){
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
     * Function to calculate boats optimum heading using the course wind direction and polar table.
     * Checks if boat is heading upwind or downwind, then finds the optimal headings rightOfTWDAngle and leftOfTWDAngle.
     * Then returns the optimum heading that is closest to the boat's heading.
     * @param course
     * @param polarTable
     * @return optimum heading
     */
    public double getOptimumHeading(Course course, PolarTable polarTable) {
        double TWD = course.getWindDirection();
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        CompoundMark lastMark = courseOrder.get(getLastRoundedMarkIndex());
        CompoundMark nextMark = courseOrder.get(getLastRoundedMarkIndex() + 1);

        double TWDTo = TWD; //shows wind as TO not FROM
        Coordinate lastMarkPosition = lastMark.getPosition();
        Coordinate nextMarkPosition = nextMark.getPosition();
        double markBearing = lastMarkPosition.headingToCoordinate(nextMarkPosition);
        boolean upwind = MathUtils.pointBetweenTwoAngle(TWD, 90, markBearing);
        double TWA = polarTable.getOptimumTWA(upwind);
        double rightOptimumHeading;
        double leftOptimumHeading;

        if (upwind) {
            rightOptimumHeading = (TWDTo - TWA + 360) % 360;
            leftOptimumHeading = (TWDTo + TWA + 360) % 360;
        } else {
            rightOptimumHeading = (TWDTo - TWA + 540) % 360;
            leftOptimumHeading = (TWDTo + TWA + 540) % 360;
        }

        double angleToLeft = abs( heading - leftOptimumHeading);
        double angleToRight = abs( heading - rightOptimumHeading);

        if (angleToLeft < angleToRight) {
            return leftOptimumHeading;
        } else {
            return rightOptimumHeading;
        }
    }

    /**
     * sets the optimal heading and currentSpeed
     * @param course the current course
     * @param polarTable the relevant polar table
     */
    public void autoPilot(Course course, PolarTable polarTable){
        double optimumHeading = getOptimumHeading(course, polarTable);
        currentSpeed = updateBoatSpeed(course);
        heading = optimumHeading;
    }

    /**
     * If true wind angle of boat is less than 90, boat is heading downwind. The heading is set to the true wind angle.
     * Otherwise the boat is heading upwind. The heading is set to the true wind angle - 90 degrees.
     */
    public void tackOrGybe(double TWD){
        double TWA = Math.abs(((TWD - heading)));
        if(TWA > 180) {
            TWA = 360 - TWA;
        }

        double downwindBuffer = 0;
        if(TWA > 90){
            downwindBuffer = 270;
        }

        if(MathUtils.pointBetweenTwoAngle((TWD - 45 + downwindBuffer)%360,45,heading)){ //side on wind boat is on
            heading += 2 * TWA;
        } else {
            heading -= 2 *TWA;
        }
        heading = (heading + 360) % 360;
    }

    public synchronized void changeSails() {
        sailsIn.set(!sailsIn.get());
    }

    public void upWind(){
        // change heading to go into the wind
        heading +=3;
    }

    public void downWind(){
        // change heading to go with the wind
        heading -=3;
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
        ArrayList<Polar> interpPolars = polarTable.TWSForInterp((int) TWS, PolarReader.getPolarsForAC35Yachts());

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
        if(!sailsIn.get()){
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

}
