package seng302.models;


import javafx.util.Pair;

import java.util.ArrayList;

/**
 * Class to encapsulate properties associated with a boat.
 */

public class Boat implements Comparable<Boat>{

    private String name;
    private String nickName;
    private double speed;
    private int finishingPlace;

    private Coordinate currentPosition;

    private int lastPassedMark;
    private boolean finished;
    private double heading;
    private double maxSpeed;
    private ArrayList<Coordinate> pathCoords;

    public Boat(String name, String nickName, double speed) {
        this.name = name;
        this.nickName = nickName;
        this.maxSpeed = speed;
        this.finished = false;
        this.lastPassedMark = 0;
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

    public Coordinate getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Updates the boat's coordinates by how much it moved in timePassed hours on the course
     * @param timePassed the amount of race hours since the last update
     * @param course the course the boat is racing on
     */
    public void updateLocation(double timePassed, Course course) {
        if(finished){
            return;
        }
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        CompoundMark nextMark = courseOrder.get(lastPassedMark+1);

        double distanceGained = timePassed * speed;
        double distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());

        //If boat moves more than the remaining distance in the leg
        while(distanceGained > distanceLeftInLeg && lastPassedMark < courseOrder.size()-1){
            distanceGained -= distanceLeftInLeg;
            //Set boat position to next mark
            currentPosition.setLat(nextMark.getLat());
            currentPosition.setLon(nextMark.getLon());
            lastPassedMark++;

            if(lastPassedMark < courseOrder.size()-1){
                setHeading(course.headingsBetweenMarks(lastPassedMark, lastPassedMark + 1));
                nextMark = courseOrder.get(lastPassedMark+1);
                distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());
            }
        }

        //Check if boat has finished
        if(lastPassedMark == courseOrder.size()-1){
            finished = true;
            speed = 0;
        } else{
            //Move the remaining distance in leg
            double percentGained = (distanceGained / distanceLeftInLeg);
            double newLat = getCurrentLat() + percentGained * (nextMark.getLat() - getCurrentLat());
            double newLon = getCurrentLon() + percentGained * (nextMark.getLon() - getCurrentLon());
            currentPosition.update(newLat, newLon);
        }
    }

    public void tackingUpdateLocation(double timePassed, Course course, double VMG, double TWA){

        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        CompoundMark nextMark = courseOrder.get(lastPassedMark+1);
        double distanceGained = timePassed * VMG; //Linear distance taken by tacking boat
        double distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());
        //If boat moves more than the remaining distance in the leg
        while(distanceGained > distanceLeftInLeg && lastPassedMark < courseOrder.size()-1){
            distanceGained -= distanceLeftInLeg;
            //Set boat position to next mark
            currentPosition.setLat(nextMark.getLat());
            currentPosition.setLon(nextMark.getLon());
            lastPassedMark++;

            if(lastPassedMark < courseOrder.size()-1){
                setHeading(course.headingsBetweenMarks(lastPassedMark, lastPassedMark + 1));
                nextMark = courseOrder.get(lastPassedMark+1);
                distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());
            }
        }
        //Check if boat has finished
        if(lastPassedMark == courseOrder.size()-1){
            finished = true;
            speed = 0;
        } else{
            //Move the remaining distance in leg
            double percentGained = (distanceGained / distanceLeftInLeg);
            double newLat = getCurrentLat() + percentGained * (nextMark.getLat() - getCurrentLat());
            double newLon = getCurrentLon() + percentGained * (nextMark.getLon() - getCurrentLon());
            currentPosition.update(newLat, newLon);
        }

    }

    public double VMG(double boatSpeed, double TWA){
        double VMG = boatSpeed * Math.cos(Math.toRadians(TWA));
        return VMG;
    }

    public double lagrangeInterpolation(Pair<Double, Double> A, Pair<Double, Double> B, Pair<Double, Double> C, double x) {
        double answer = (((x - B.getKey()) * (x - C.getKey())) / ((A.getKey() - B.getKey()) * (A.getKey() - C.getKey()))) * A.getValue() + (((x - A.getKey()) * (x - C.getKey())) / ((B.getKey() - A.getKey()) * (B.getKey() - C.getKey()))) * B.getValue() + (((x - A.getKey()) * (x - B.getKey())) / ((C.getKey() - A.getKey()) * (C.getKey() - B.getKey()))) * C.getValue();
        return answer;
    }

    public Pair<Double,Double> tacking(Course course, ArrayList<Integer> trueWindSpeeds, ArrayList<ArrayList<Pair<Double, Double>>> polars){
        double TWS = course.getTrueWindSpeed();
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
        //Interpolate between closet TWS to get highest VMG of each
        double TWA3 = 0;
        for(double k = 0; k < 91; k++){
            double BSP1 = lagrangeInterpolation(polars.get(index - 1).get(1), polars.get(index - 1).get(3), polars.get(index - 1).get(5), k);
            if(VMG(BSP1, k) > VMG1){VMG1 = VMG(BSP1, k); TWA1 = k;}
            double BSP2 = lagrangeInterpolation(polars.get(index).get(1), polars.get(index).get(3), polars.get(index).get(5), k);
            if(VMG(BSP2, k) > VMG2){VMG2 = VMG(BSP2, k); TWA2 = k;}
            double BSP3 = lagrangeInterpolation(polars.get(index + 1).get(1), polars.get(index + 1).get(3), polars.get(index + 1).get(5), k);
            if(VMG(BSP3, k) > VMG3){VMG3 = VMG(BSP3, k);TWA3 = k;}
        }
        //interpolate between TWS and VMG to get given VMG at the TWS
        Pair<Double,Double> pair1 = new Pair<>(TWS1, VMG1);
        Pair<Double,Double> pair2 = new Pair<>(TWS2, VMG2);
        Pair<Double,Double> pair3 =  new Pair<>(TWS3, VMG3);
        double TrueVMG = lagrangeInterpolation(pair1,pair2, pair3, TWS);
        //interpolate back to get TWA based on found VMG
        Pair<Double,Double> pair4 = new Pair<>(VMG1, TWA1);
        Pair<Double,Double> pair5 = new Pair<>(VMG2, TWA2);
        Pair<Double,Double> pair6 = new Pair<>(VMG3, TWA3);
        double TWA = lagrangeInterpolation(pair4,pair5,pair6,TrueVMG);
        boatsTack = new Pair<>(TrueVMG, TWA);
        return boatsTack;
    }




    public int compareTo(Boat otherBoat){
        return otherBoat.getLastPassedMark() - lastPassedMark;
    }

    public String getName() {
        return this.name;
    }

    public String getNickName() {return nickName;}

    public double getSpeed() {
        return this.speed;
    }

    public int getFinishingPlace() {
        return this.finishingPlace;
    }

    public void setFinishingPlace(int place) {
        this.finishingPlace = place;
    }

    public int getLastPassedMark() {
        return lastPassedMark;
    }

    public double getCurrentLat() {
        return currentPosition.getLat();
    }

    public double getCurrentLon() {
        return currentPosition.getLon();
    }

    public boolean isFinished() {
        return finished;
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

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
