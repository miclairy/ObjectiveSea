package seng302.models;


import javafx.util.Pair;
import seng302.utilities.MathUtils;
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
        //ArrayList<Integer> TWSList = readPolars.getTWS();
        ArrayList<Polars> polars = readPolars.getPolars();
        Pair<Double,Double> tackingInfo = tacking(25,polars);
        Pair<Double,Double> gybingInfo = gybing(25,polars);
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


    public ArrayList<Polars> TWSForInterp(int TWS,ArrayList<Polars> polars){
        ArrayList<Polars> interpPolars = new ArrayList<>();
        int index = 0;
        double TWSDiff = 1000000000;
        //Find the 3 values closest to the TWS to interpolate with

        for(int i = 0; i < polars.size(); i++){
            if(Math.abs(TWS - polars.get(i).getTWS()) < TWSDiff){
                index = i;
                TWSDiff = Math.abs(TWS - polars.get(i).getTWS());
            }
        }
        //Check that these values aren't 0 or the size of the list as this will cause an error
        if(index == 0){index ++;}
        if(index == polars.size() - 1){index -= 1;}

        Polars polar1 = polars.get(index - 1);
        Polars polar2 = polars.get(index);
        Polars polar3 = polars.get(index + 1);

        interpPolars.add(polar1);
        interpPolars.add(polar2);
        interpPolars.add(polar3);
        return interpPolars;
    }

    public Pair<Double,Double> gybing(int TWS, ArrayList<Polars> polars){
        Pair<Double,Double> boatsGybe;
        ArrayList<Polars> interpPolars = TWSForInterp(TWS, polars);

        Polars polar1 = interpPolars.get(0);
        Polars polar2 = interpPolars.get(1);
        Polars polar3 = interpPolars.get(2);

        double TWS1 = polar1.getTWS();
        double TWS2 = polar2.getTWS();
        double TWS3 = polar3.getTWS();

        double TWA1 = 0;
        double TWA2 = 0;
        double TWA3 = 0;

        double gybeVMG1 = 1000000;
        double gybeVMG2 = 1000000;
        double gybeVMG3 = 1000000;

        Pair<Double,Double> pair4;
        Pair<Double,Double> pair5;
        Pair<Double,Double> pair6;

        double TrueVMG;

        if(polar2.hasDownwindOptimum()){
            Pair<Double,Double> dnWind1 = polar1.getDownWindOptimum();
            Pair<Double,Double> dnWind2 = polar2.getDownWindOptimum();
            Pair<Double,Double> dnWind3 = polar3.getDownWindOptimum();
            gybeVMG1 = MathUtils.VMG(dnWind1.getValue(), dnWind1.getKey());
            gybeVMG2 = MathUtils.VMG(dnWind2.getValue(), dnWind2.getKey());
            gybeVMG3 = MathUtils.VMG(dnWind3.getValue(), dnWind3.getKey());

            TWA1 = dnWind1.getKey();
            TWA2 = dnWind2.getKey();
            TWA3 = dnWind3.getKey();

        } else {
            for(double k = 90; k < 181; k++){
                double BSP1 = MathUtils.lagrangeInterpolation(polar1.getTWAandBSP().get(5), polar1.getTWAandBSP().get(6), polar1.getTWAandBSP().get(7), k);
                if(MathUtils.VMG(BSP1, k) < gybeVMG1){gybeVMG1 = MathUtils.VMG(BSP1, k); TWA1 = k;}
                double BSP2 = MathUtils.lagrangeInterpolation(polar2.getTWAandBSP().get(5), polar2.getTWAandBSP().get(6), polar2.getTWAandBSP().get(7), k);
                if(MathUtils.VMG(BSP2, k) < gybeVMG2){gybeVMG2 = MathUtils.VMG(BSP2, k); TWA2 = k;}
                double BSP3 = MathUtils.lagrangeInterpolation(polar3.getTWAandBSP().get(5), polar3.getTWAandBSP().get(6), polar3.getTWAandBSP().get(7), k);
                if(MathUtils.VMG(BSP3, k) < gybeVMG3){gybeVMG3 = MathUtils.VMG(BSP3, k);TWA3 = k;}

                //interpolate back to get TWA based on found VMG

            } }
        pair4 = new Pair<>(gybeVMG1, TWA1);
        pair5 = new Pair<>(gybeVMG2, TWA2);
        pair6 = new Pair<>(gybeVMG3, TWA3);
        TrueVMG = MathUtils.lagrangeInterpolation(new Pair<Double, Double>(TWS1, gybeVMG1),new Pair<Double, Double>(TWS2, gybeVMG2), new Pair<Double, Double>(TWS3, gybeVMG3), TWS);
        double TWA = MathUtils.lagrangeInterpolation(pair4,pair5,pair6,TrueVMG);
        boatsGybe = new Pair<>(TrueVMG, TWA);
        return boatsGybe;
    }

    /**
     * This function calculates the optimum tacking or gybing angle and speed based on a polar table
     * @param TWS true wind speed
     * @param polars the polars from the table
     * @return the TWA and VMG optimum for given boat
     */
    public Pair<Double,Double> tacking(int TWS, ArrayList<Polars> polars){
//        double TWS = course.getTrueWindSpeed();
        Pair<Double,Double> boatsTack;
        ArrayList<Polars> interpPolars = TWSForInterp(TWS, polars);

        Polars polar1 = interpPolars.get(0);
        Polars polar2 = interpPolars.get(1);
        Polars polar3 = interpPolars.get(2);

        double TWS1 = polar1.getTWS();
        double TWS2 = polar2.getTWS();
        double TWS3 = polar3.getTWS();

        double TWA1 = 0;
        double TWA2 = 0;
        double TWA3 = 0;

        double VMG1 = 0;
        double VMG2 = 0;
        double VMG3 = 0;

        Pair<Double,Double> pair4;
        Pair<Double,Double> pair5;
        Pair<Double,Double> pair6;

        double TrueVMG;


        if(polar2.hasUpwindOptimum()){
            Pair<Double,Double> upWind1 = polar1.getUpWindOptimum();
            Pair<Double,Double> upWind2 = polar2.getUpWindOptimum();
            Pair<Double,Double> upWind3 = polar3.getUpWindOptimum();
            VMG1 = MathUtils.VMG(upWind1.getValue(), upWind1.getKey());
            VMG2 = MathUtils.VMG(upWind2.getValue(), upWind2.getKey());
            VMG3 = MathUtils.VMG(upWind3.getValue(), upWind3.getKey());
            TWA1 = upWind1.getKey();
            TWA2 = upWind2.getKey();
            TWA3 = upWind3.getKey();

        } else {
            for(double k = 0; k < 91; k++){
                double BSP1 = MathUtils.lagrangeInterpolation(polar1.getTWAandBSP().get(1), polar1.getTWAandBSP().get(2), polar1.getTWAandBSP().get(3), k);
                if(MathUtils.VMG(BSP1, k) > VMG1){VMG1 = MathUtils.VMG(BSP1, k); TWA1 = k;}
                double BSP2 = MathUtils.lagrangeInterpolation(polar2.getTWAandBSP().get(1), polar2.getTWAandBSP().get(2), polar2.getTWAandBSP().get(3), k);
                if(MathUtils.VMG(BSP2, k) > VMG2){VMG2 = MathUtils.VMG(BSP2, k); TWA2 = k;}
                double BSP3 = MathUtils.lagrangeInterpolation(polar3.getTWAandBSP().get(1), polar3.getTWAandBSP().get(2), polar3.getTWAandBSP().get(3), k);
                if(MathUtils.VMG(BSP3, k) > VMG3){VMG3 = MathUtils.VMG(BSP3, k);TWA3 = k;}
            }

            //interpolate back to get TWA based on found VMG
        }
        TrueVMG = MathUtils.lagrangeInterpolation(new Pair<Double, Double>(TWS1, VMG1),new Pair<Double, Double>(TWS2, VMG2), new Pair<Double, Double>(TWS3, VMG3), TWS);
        pair4 = new Pair<>(VMG1, TWA1);
        pair5 = new Pair<>(VMG2, TWA2);
        pair6 = new Pair<>(VMG3, TWA3);
        //Interpolate between closet TWS to get highest VMG of each
        //if gybing 5,6,7 if tacking 1,2,3

        double TWA = MathUtils.lagrangeInterpolation(pair4,pair5,pair6,TrueVMG);
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

    public void setCurrentSpeed(double speed) {
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

    public double calculateLaylineHeading() { //TODO implement me
        return 145;
    }
    
    public void setCurrentVMGSpeed(double currentVMGSpeed) {
        this.currentVMGSpeed = currentVMGSpeed;
    }

    public double getTWAofBoat() {
        return TWAofBoat;
    }

    public double getGybeTWAofBoat() {
        return gybeTWAofBoat;
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
}
