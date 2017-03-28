package seng302;

import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.shape.Path;

import java.util.ArrayList;

/**
 * Created by mjt169 on 6/03/17.
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

    private ArrayList<Coordinate> pathCoords;

    public Boat(String name, String nickName, double speed) {
        this.name = name;
        this.nickName = nickName;
        this.speed = speed;
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
}
