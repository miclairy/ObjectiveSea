package seng302.models;


import seng302.data.StartTimingStatus;

import seng302.data.BoatStatus;
import seng302.utilities.MathUtils;
import seng302.utilities.PolarReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import static java.lang.StrictMath.abs;
import static seng302.utilities.MathUtils.pointBetweenTwoAngle;

/**
 * Class to encapsulate properties associated with a boat.
 */

public class Boat extends Observable implements Comparable<Boat>{

    private final double KNOTS_TO_MMS_MULTIPLIER = 514.444;

    private String name;
    private String nickName;
    private double speed;
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
    private int playerHeading = -1;
    private double lastPlayerDirection = 0; //0 = Clockwise / 1 = AntiClockwise

    private BoatStatus status = BoatStatus.UNDEFINED;
    private StartTimingStatus timeStatus = StartTimingStatus.ONTIME;

    private List<Coordinate> pathCoords;
    private long timeTillMark;
    private long timeTillFinish;
    private Integer id;

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

    public double getSpeed() { return this.speed; }

    public int getSpeedInMMS(){
        return (int) (this.speed * KNOTS_TO_MMS_MULTIPLIER);
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
        if(playerHeading == -1) {
            return heading;
        } else {
            return playerHeading;
        }
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
     * Make speed be the max speed.
     */
    public void maximiseSpeed(){
        this.speed = maxSpeed;
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
        this.speed = speed;
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

        return Math.cos(Math.toRadians(angle)) * speed;
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
        //no sail zone
        if(isTacking(TWA)) {
            if(inRange(optimumHeadings.headingA, optimumHeadings.headingB, heading)) {
                return heading;
            }
        }

        if (optimumHeadings.headingA == heading && optimumHeadings.headingB == heading){
            return heading;
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
        heading = getVMGHeading(course, polarTable);
    }

    public void sailsIn(){
        speed = 0;
    }

    public void sailsOut(){
        speed = getCurrentVMG();
    }

    public void tackOrGybe(Course course, PolarTable polarTable) {
        heading = tackingFunction(course, polarTable);
    }

    /**
     * Function to change heading of boat when the tack/gybe button is pressed
     * If already on an optimum heading it switches to the "opposite" optimum
     * If in dead zones or no sail zone (heading into wind) the heading is unchanged
     * @param course
     * @param polarTable
     * @return new tack/gybe heading
     */
    public double tackingFunction(Course course, PolarTable polarTable) {
        OptimumHeadings optimumHeadings = getOptimumHeadings(course, polarTable);
        double TWA = Math.abs(((course.getWindDirection() - heading)));

        double optimumHeadingA = optimumHeadings.headingA;
        double optimumHeadingB = optimumHeadings.headingB;

        if(heading == (optimumHeadingA)) {
            return optimumHeadingB;
        } else if (heading == optimumHeadingB) {
            return optimumHeadingA;
        }

        //no sail zone
        if(isTacking(TWA)) {
            if(inRange(optimumHeadings.headingA, optimumHeadings.headingB, heading)) {
                return heading;
            }
        }

        if (optimumHeadings.headingA == heading && optimumHeadings.headingB == heading){
            return heading;
        }


        double angleToOptimumA = abs( heading - optimumHeadingA);
        double angleToOptimumB = abs( heading - optimumHeadingB);

        if (angleToOptimumA < angleToOptimumB) {
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
     * If true wind angle of boat is less than 90, boat is heading downwind. The heading is set to the true wind angle.
     * Otherwise the boat is heading upwind. The heading is set to the true wind angle - 90 degrees.
     */
    public void oldTackOrGybe(double TWD){
        double TWA = Math.abs(((TWD - heading)));
        if(TWA > 180) {
            TWA = 360 - TWA;
        }

        double downwindBuffer = 0;
        if(TWA > 90){
            downwindBuffer = 270;
        }

        if(pointBetweenTwoAngle((TWD - 45 + downwindBuffer)%360,45,heading)){ //side on wind boat is on
            heading += 2 * TWA;
        } else {
            heading -= 2 *TWA;
        }
        heading = (heading + 360) % 360;
    }

    public void resetPlayerHeading() {
        playerHeading = -1;
    }

    /**
     * This method takes the current wind angle and checks to see what side of the compass the players boat is on,
     * as to turn the boat the right direction towards the appropriate upWind or downWind.
     * @param windAngle
     */
    private void headingChange(double windAngle) {

        if(playerHeading == -1) {
            playerHeading = (int) heading;
        }

        playerHeading += 360;
        int windAngleCheck = (int) windAngle + 360;

        if((windAngleCheck > playerHeading && windAngleCheck-180 < playerHeading) ||
           (windAngleCheck < playerHeading && windAngleCheck+180 < playerHeading)) {
            playerHeading += 3;
            lastPlayerDirection = 0;

        } else if((windAngleCheck < playerHeading && windAngleCheck+180 > playerHeading) ||
                  (windAngleCheck > playerHeading && windAngleCheck-180 > playerHeading)) {
            playerHeading -= 3;
            lastPlayerDirection = 1;

        } else if(windAngleCheck == playerHeading ||
                  windAngleCheck-180 == playerHeading ||
                  windAngleCheck+180 == playerHeading) {
            if(lastPlayerDirection == 0) {
                playerHeading += 3;

            } else if(lastPlayerDirection == 1) {
                playerHeading -= 3;
            }
            /**This 'windAngleCheck == playerHeading' statement, takes the last direction the boat was turning,
             * either clockwise or anti clockwise, and turns the boat again in that direction, as the boat will
             * sometimes be facing the exact wind direction and not know which way to turn.
             */
        }

        if(playerHeading >= 720) {
            playerHeading -= 720;
        } else if(playerHeading >= 360) {
            playerHeading -= 360;
        }


    }

    public void upWind(double windAngle){
        // change heading to go into the wind

        headingChange(windAngle);
        // change heading to go into the wind
    }

    public void downWind(double windAngle){
        // change heading to go with the wind
        double newWindAngle = windAngle;
        if(newWindAngle > 180) {
            newWindAngle -= 360;
        }
        headingChange(newWindAngle + 180);
    }

    /**
     * A function to update the boat speed whenever the windspeed or boat heading is changed
     * @param TWS
     * @param course
     * @param windDirection
     */
    public void updateBoatSpeed(double TWS, Course course, double windDirection){
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

        setCurrentSpeed(MathUtils.bilinearInterpolation(TWS0,TWS1,TWA0,TWA1,z00,z01,z10,z11,TWS,TWA));
    }
}
