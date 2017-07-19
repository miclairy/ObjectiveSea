package seng302.models;


import javafx.util.Pair;
import seng302.data.StartTimingStatus;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.chart.XYChart.Data;

import seng302.data.BoatStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

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

    private int lastRoundedMarkIndex;
    private long lastRoundedMarkTime;
    private int lastTackMarkPassed;
    private int lastGybeMarkPassed;
    private boolean finished;
    private double heading;
    private double maxSpeed;
    private double playerHeading = -1;

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
        if (this.heading != heading) {
            this.heading = heading;
            addPathCoord(new Coordinate(getCurrentLat(), getCurrentLon()));
        }
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

        return Math.cos(Math.toRadians(angle)) * speed;
    }

    public void autoPilot(){
        //Optimal heading and speed
    }

    public void sailsIn(){
        speed = 0;
    }

    public void sailsOut(){
        speed = getCurrentVMG();
    }

    public void tackOrGybe(){
        //tack of gybe
    }

    private void headingChange(double windAngle) {

        if(playerHeading == -1) {
            playerHeading = heading;
        }
        playerHeading += 360;
        double windAngleCheck = windAngle + 360;
        if(windAngleCheck > playerHeading && windAngleCheck-180<playerHeading) {
            playerHeading += 3;
            if(playerHeading > 359) {
                playerHeading -= 360;
            }
        } else if(windAngleCheck < playerHeading && windAngleCheck+180>playerHeading) {
            playerHeading -= 3;
            if(playerHeading < 0) {
                playerHeading += 360;
            }
        }
        playerHeading -= 360;

    }

    public void upWind(double windAngle){
        // change heading to go into the wind

        headingChange(windAngle);

        //heading = (int) windAngle;
    }

    public void downWind(double windAngle){
        // change heading to go with the wind
        double newWindAngle = windAngle;
        if(newWindAngle > 180) {
            newWindAngle -= 360;
        }
        headingChange(newWindAngle + 180);

        //heading = (int) windAngle + 180;
    }
}
