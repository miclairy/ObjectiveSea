package seng302.models;


import java.util.ArrayList;
import java.util.List;

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
    private Integer id;

    public Boat(Integer id, String name, String nickName, double speed) {
        this.id = id;
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
        List<CompoundMark> courseOrder = course.getCourseOrder();
        CompoundMark nextMark = courseOrder.get(lastPassedMark+1);

        double distanceGained = timePassed * speed;
        double distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());

        //If boat moves more than the remaining distance in the leg
        while(distanceGained > distanceLeftInLeg && lastPassedMark < courseOrder.size()-1){
            distanceGained -= distanceLeftInLeg;
            //Set boat position to next mark
            currentPosition.setLat(nextMark.getPosition().getLat());
            currentPosition.setLon(nextMark.getPosition().getLon());
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
            double newLat = getCurrentLat() + percentGained * (nextMark.getPosition().getLat() - getCurrentLat());
            double newLon = getCurrentLon() + percentGained * (nextMark.getPosition().getLon() - getCurrentLon());
            currentPosition.update(newLat, newLon);
        }
    }

    public int compareTo(Boat otherBoat){
        return otherBoat.getLastPassedMark() - lastPassedMark;
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
