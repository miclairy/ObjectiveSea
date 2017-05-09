package seng302.models;


import javafx.util.Pair;
import seng302.utilities.readPolars;

import seng302.data.BoatStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to encapsulate properties associated with a boat.
 */

public class Boat implements Comparable<Boat>{

    private final double KNOTS_TO_MMS_MULTIPLIER = 514.444;

    private String name;
    private String nickName;
    private double speed;
    private int finishingPlace;
    private double currentVMGSpeed;
    private int currPlacing;

    private Coordinate currentPosition;

    private int lastRoundedMarkIndex;
    private long lastRoundedMarkTime;
    private int lastTackMarkPassed;
    private int lastGybeMarkPassed;
    private boolean finished;
    private double heading;
    private double maxSpeed;

    private BoatStatus status = BoatStatus.UNDEFINED;

    private ArrayList<Coordinate> pathCoords;
    private double VMGofBoat;
    private double TWAofBoat;
    private double gybeVMGofBoat;
    private double gybeTWAofBoat;
    private long timeTillMark;
    private long timeTillFinish;
    private Integer id;

    public Boat(Integer id, String name, String nickName, double speed) {
        this.id = id;
        this.name = name;
        this.nickName = nickName;
        this.maxSpeed = speed;
        this.finished = false;
        this.lastRoundedMarkIndex = -1;
        this.pathCoords = new ArrayList<>();
        this.currentPosition = new Coordinate(0,0);
        try {
            readPolars.polars();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Integer> TWSList = readPolars.getTWS();
        ArrayList<ArrayList<Pair<Double, Double>>> polars = readPolars.getPolars();
        Pair<Double,Double> tackingInfo = tacking(25,TWSList,polars, true);
        Pair<Double,Double> gybingInfo = tacking(25, TWSList,polars,false);
        gybeVMGofBoat = gybingInfo.getKey();
        gybeTWAofBoat = gybingInfo.getValue();
        VMGofBoat = tackingInfo.getKey();
        TWAofBoat = tackingInfo.getValue();
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
     * Updates the boat's coordinates by how much it moved in timePassed hours on the course
     * @param timePassed the amount of race hours since the last update
     * @param course the course the boat is racing on
     */
    public void updateLocation(double timePassed, Course course) {
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        if(finished || courseOrder.get(lastRoundedMarkIndex).isFinishLine()) {
            return;
        }
        double windDirection = course.getWindDirection();
        double bearing = course.headingsBetweenMarks(lastRoundedMarkIndex,lastRoundedMarkIndex+1);
        boolean onTack = false;
        boolean onGybe = false;
        currentVMGSpeed = speed;
        if(((windDirection - TWAofBoat)%360)+360 <= (bearing+360) && ((windDirection + TWAofBoat)%360)+360 >= (bearing + 360)){
            onTack = true;
            currentVMGSpeed = VMGofBoat;
        } else if((((180+windDirection) - (180-gybeTWAofBoat))% 360)+360 <= (bearing+360) && (((180+windDirection) + (180-gybeTWAofBoat))%360)+360 >= (bearing+360)){
            onGybe = true;
            currentVMGSpeed = gybeVMGofBoat * (-1.0);
        }

        CompoundMark nextMark = courseOrder.get(lastRoundedMarkIndex+1);
        double currentSpeed = speed;


        //The polars currently used aren't for our fancy catamaran's so it is super slow and boring
        // so I've commented them out for practicality of watching :)
        if(onTack) {
            currentSpeed = VMGofBoat / Math.cos(Math.toRadians(TWAofBoat));
        } else if(onGybe){
            currentSpeed = (gybeVMGofBoat) / Math.cos(Math.toRadians(gybeTWAofBoat));
        }

        double distanceGained = timePassed * currentSpeed;
        double distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());


        //If boat moves more than the remaining distance in the leg
        while(distanceGained > distanceLeftInLeg && lastRoundedMarkIndex < courseOrder.size()-1) {
            distanceGained -= distanceLeftInLeg;
            //Set boat position to next mark
            currentPosition.setLat(nextMark.getPosition().getLat());
            currentPosition.setLon(nextMark.getPosition().getLon());
            lastRoundedMarkIndex++;

            if(lastRoundedMarkIndex < courseOrder.size()-1){
                setHeading(course.headingsBetweenMarks(lastRoundedMarkIndex, lastRoundedMarkIndex + 1));
                nextMark = courseOrder.get(lastRoundedMarkIndex +1);
                distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());
            }
        }

        //Check if boat has finished
        if(!finished && lastRoundedMarkIndex != courseOrder.size()-1) {
            if(lastRoundedMarkIndex == courseOrder.size()-1){
                finished = true;
                status = BoatStatus.FINISHED; //   finished
                speed = 0;
            } if(onTack) {
                double alphaAngle;
                if(bearing <= (windDirection + 90.0)){
                    alphaAngle = Math.abs(bearing - windDirection)%360;
                } else {
                    alphaAngle = (360 + windDirection - bearing)%360;
                }
                    lastGybeMarkPassed = 0;
                    Coordinate tackingPosition = tackingUpdateLocation(distanceGained, courseOrder, true,alphaAngle);
                    //Move the remaining distance in leg
                    currentPosition.update(tackingPosition.getLat(), tackingPosition.getLon());
            } else if(onGybe) {
                double alphaAngle;
                if(bearing <= (windDirection + 90.0)){
                    alphaAngle = 180 - Math.abs(bearing - windDirection)%360;
                } else {
                    alphaAngle = 180 - (360 + windDirection - bearing)%360;
                }
                lastTackMarkPassed = 0;
                Coordinate tackingPosition = tackingUpdateLocation(distanceGained, courseOrder, false,alphaAngle);
                //Move the remaining distance in leg
                currentPosition.update(tackingPosition.getLat(), tackingPosition.getLon());
            } else {
                lastTackMarkPassed = 0;
                lastGybeMarkPassed = 0;
                //Move the remaining distance in leg
                double percentGained = (distanceGained / distanceLeftInLeg);
                double newLat = getCurrentLat() + percentGained * (nextMark.getPosition().getLat() - getCurrentLat());
                double newLon = getCurrentLon() + percentGained * (nextMark.getPosition().getLon() - getCurrentLon());
                currentPosition.update(newLat, newLon);
            }
        } else {
            finished = true;
        }
    }

    /**
     * Updates the boat's coordinates by how much it moved in timePassed hours on the course
     * @param distanceGained the distance gained by the boat since last update
     * @param courseOrder the order of the set marks
     * @param onTack this decides whether to calculate a tack or a gybe
     */
    public Coordinate tackingUpdateLocation(double distanceGained, ArrayList<CompoundMark> courseOrder, Boolean onTack, double alphaAngle){
        double TrueWindAngle;
        if(onTack){
            TrueWindAngle = TWAofBoat;
        } else {
            TrueWindAngle = 180 - gybeTWAofBoat;}

        CompoundMark nextMark = courseOrder.get(lastRoundedMarkIndex+1);
        double lengthOfLeg = courseOrder.get(lastRoundedMarkIndex).getPosition().greaterCircleDistance(nextMark.getPosition());
        double betaAngle = (2*TrueWindAngle) - alphaAngle;
        double lengthOfTack = ((lengthOfLeg* Math.sin(Math.toRadians(betaAngle)))/Math.sin(Math.toRadians(180 - 2*TrueWindAngle)))/2.0;
        ArrayList<CompoundMark> tackingMarks = new ArrayList<>();
        tackingMarks.add(courseOrder.get(lastRoundedMarkIndex));
        CompoundMark currentMark = courseOrder.get(lastRoundedMarkIndex);
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
            lastMarkPassed = lastTackMarkPassed;
        } else {
            lastMarkPassed = lastGybeMarkPassed;
        }
        CompoundMark nextTackMark = tackingMarks.get(lastMarkPassed+1);
        double distanceLeftinTack = currentPosition.greaterCircleDistance(nextTackMark.getPosition());
        if(lastMarkPassed == 0){
            double newHeading = tackingMarks.get(lastMarkPassed).getPosition().headingToCoordinate(tackingMarks.get(lastMarkPassed + 1).getPosition());
            setHeading(newHeading);
        }
        //If boat moves more than the remaining distance in the leg
        while(distanceGained > distanceLeftinTack && lastMarkPassed < tackingMarks.size()-1){
            distanceGained -= distanceLeftinTack;
            //Set boat position to next mark
            currentPosition.setLat(nextTackMark.getPosition().getLat());
            currentPosition.setLon(nextTackMark.getPosition().getLon());
            if(onTack){
                lastTackMarkPassed++;
            } else {
                lastGybeMarkPassed++;
            }
            lastMarkPassed++;

            if(lastMarkPassed < tackingMarks.size()-1){
                double newHeading = tackingMarks.get(lastMarkPassed).getPosition().headingToCoordinate(tackingMarks.get(lastMarkPassed + 1).getPosition());
                setHeading(newHeading);
                nextTackMark = tackingMarks.get(lastMarkPassed+1);
                distanceLeftinTack = currentPosition.greaterCircleDistance(nextTackMark.getPosition());
            }
        }
        if(lastMarkPassed == tackingMarks.size()-1 && nextMark.isFinishLine()){
            finished = true;
            speed = 0;
        }
        double percentGained = (distanceGained / distanceLeftinTack);
        double newLat = getCurrentLat() + percentGained * (nextTackMark.getPosition().getLat() - getCurrentLat());
        double newLon = getCurrentLon() + percentGained * (nextTackMark.getPosition().getLon() - getCurrentLon());
        return new Coordinate(newLat, newLon);
    }


    /**
     * Calculates the VMG of the boat
     * @param boatSpeed the speed of the boat
     * @param TWA the true wind angle that the boat is heading at
     * @return the velocity made good of the boat
     */
    public double VMG(double boatSpeed, double TWA){
        return boatSpeed * Math.cos(Math.toRadians(TWA));
    }

    /**
     * A function for interpolating three values, only does three as the math is easier and this provides an accurate enough
     * interpolation for its current uses
     * @param A first pair
     * @param B second pair
     * @param C third pair
     * @param x the value to interpolate on
     * @return the interpolated value using x as input
     */
    public double lagrangeInterpolation(Pair<Double, Double> A, Pair<Double, Double> B, Pair<Double, Double> C, double x) {
        return (((x - B.getKey()) * (x - C.getKey())) / ((A.getKey() - B.getKey()) * (A.getKey() - C.getKey()))) * A.getValue() + (((x - A.getKey()) * (x - C.getKey())) / ((B.getKey() - A.getKey()) * (B.getKey() - C.getKey()))) * B.getValue() + (((x - A.getKey()) * (x - B.getKey())) / ((C.getKey() - A.getKey()) * (C.getKey() - B.getKey()))) * C.getValue();}

    /**
     * This function calculates the optimum tacking or gybing angle and speed based on a polar table
     * @param TWS true wind speed
     * @param trueWindSpeeds the list of true wind speeds given from polar table
     * @param polars the polars from the table
     * @param onTack decides whether to calculate the TWA and VMG for tack or gybe
     * @return the TWA and VMG optimum for given boat
     */
    public Pair<Double,Double> tacking(int TWS, ArrayList<Integer> trueWindSpeeds, ArrayList<ArrayList<Pair<Double, Double>>> polars, Boolean onTack){
//        double TWS = course.getTrueWindSpeed();
        Pair<Double,Double> boatsTack;
        int index = 0;
        double TWSDiff = 1000000000;
        //Find the 3 values closest to the TWS to interpolate with
        for(int i = 0; i < trueWindSpeeds.size(); i++){
            if(Math.abs(TWS - trueWindSpeeds.get(i)) < TWSDiff){
                index = i;
                TWSDiff = Math.abs(TWS - trueWindSpeeds.get(i));
            }
        }
        //Check that these values aren't 0 or the size of the list as this will cause an error
        if(index == 0){index ++;}
        if(index == trueWindSpeeds.size() - 1){index -= 1;}
        double TWS1 = trueWindSpeeds.get(index-1);
        double TWS2 = trueWindSpeeds.get(index);
        double TWS3 = trueWindSpeeds.get(index+1);
        double VMG1 = 0;
        double TWA1 = 0;
        double VMG2 = 0;
        double TWA2 = 0;
        double VMG3 = 0;
        double TWA3 = 0;
        double gybeVMG1 = 1000000;
        double gybeVMG2 = 1000000;
        double gybeVMG3 = 1000000;

        Pair<Double,Double> pair4;
        Pair<Double,Double> pair5;
        Pair<Double,Double> pair6;
        double TrueVMG;
        //Interpolate between closet TWS to get highest VMG of each
        //if gybing 5,6,7 if tacking 1,3,5
        if(onTack){
        for(double k = 0; k < 91; k++){
            double BSP1 = lagrangeInterpolation(polars.get(index - 1).get(2), polars.get(index - 1).get(3), polars.get(index - 1).get(6), k);
            if(VMG(BSP1, k) > VMG1){VMG1 = VMG(BSP1, k); TWA1 = k;}
            double BSP2 = lagrangeInterpolation(polars.get(index).get(2), polars.get(index).get(3), polars.get(index).get(6), k);
            if(VMG(BSP2, k) > VMG2){VMG2 = VMG(BSP2, k); TWA2 = k;}
            double BSP3 = lagrangeInterpolation(polars.get(index + 1).get(2), polars.get(index + 1).get(3), polars.get(index + 1).get(6), k);
            if(VMG(BSP3, k) > VMG3){VMG3 = VMG(BSP3, k);TWA3 = k;}
        }
            Pair<Double,Double> pair1 = new Pair<>(TWS1, VMG1);
            Pair<Double,Double> pair2 = new Pair<>(TWS2, VMG2);
            Pair<Double,Double> pair3 =  new Pair<>(TWS3, VMG3);
            TrueVMG = lagrangeInterpolation(pair1,pair2, pair3, TWS);
            //interpolate back to get TWA based on found VMG
            pair4 = new Pair<>(VMG1, TWA1);
            pair5 = new Pair<>(VMG2, TWA2);
            pair6 = new Pair<>(VMG3, TWA3);
        } else {
            for(double k = 90; k < 181; k++){
                double BSP1 = lagrangeInterpolation(polars.get(index - 1).get(7), polars.get(index - 1).get(8), polars.get(index - 1).get(9), k);
                if(VMG(BSP1, k) < gybeVMG1){gybeVMG1 = VMG(BSP1, k); TWA1 = k;}
                double BSP2 = lagrangeInterpolation(polars.get(index).get(7), polars.get(index).get(8), polars.get(index).get(9), k);
                if(VMG(BSP2, k) < gybeVMG2){gybeVMG2 = VMG(BSP2, k); TWA2 = k;}
                double BSP3 = lagrangeInterpolation(polars.get(index + 1).get(7), polars.get(index + 1).get(8), polars.get(index + 1).get(9), k);
                if(VMG(BSP3, k) < gybeVMG3){gybeVMG3 = VMG(BSP3, k);TWA3 = k;}
                }
            Pair<Double,Double> pair1 = new Pair<>(TWS1, gybeVMG1);
            Pair<Double,Double> pair2 = new Pair<>(TWS2, gybeVMG2);
            Pair<Double,Double> pair3 =  new Pair<>(TWS3, gybeVMG3);
            TrueVMG = lagrangeInterpolation(pair1,pair2, pair3, TWS);
            //interpolate back to get TWA based on found VMG
            pair4 = new Pair<>(gybeVMG1, TWA1);
            pair5 = new Pair<>(gybeVMG2, TWA2);
            pair6 = new Pair<>(gybeVMG3, TWA3);

        }
        double TWA = lagrangeInterpolation(pair4,pair5,pair6,TrueVMG);
        boatsTack = new Pair<>(TrueVMG, TWA);
        return boatsTack;
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

    public double getSpeed() {
        return this.speed;
    }

    public int getSpeedInMMS(){
        return (int) (this.speed * KNOTS_TO_MMS_MULTIPLIER);
    }

    public int getFinishingPlace() {
        return this.finishingPlace;
    }

    public void setFinishingPlace(int place) {
        this.finishingPlace = place;
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

    public void setCurrPlacing(int placing){this.currPlacing = placing;}

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

    public ArrayList<Coordinate> getPathCoords() {return pathCoords;}

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

    public double getVMGofBoat() { return VMGofBoat;}

    public double getGybeVMGofBoat() {return gybeVMGofBoat;}

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public long getTimeAtNextMark() {
        return timeTillMark;
    }

    public void setTimeTillMark(long timeTillMark) {
        this.timeTillMark = timeTillMark;
    }

    public double getCurrentVMGSpeed() {
        return currentVMGSpeed;
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
}
