package seng302;

import javafx.scene.shape.Shape;

import java.util.ArrayList;

/**
 * Created by mjt169 on 6/03/17.
 * Class to encapsulate properties associated with a boat.
 */

public class Boat implements Comparable<Boat>{

    private String name;
    private double speed;
    private int finishingPlace;
    private Shape icon;
    private double currentLat;
    private double currentLon;
    private int lastPassedMark;
    private boolean finished;

    public Boat(String name, double speed) {
        this.name = name;
        this.speed = speed;
        this.finished = false;
        this.lastPassedMark = 0;
    }

    /**
     * Sets the latitude and longitude of the boat
     * @param lat the latitude of the boat
     * @param lon the longitude of the boat
     */
    public void setPosition(double lat, double lon){
        currentLat = lat;
        currentLon = lon;
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

//        //Test by sabotaging the Chinese Team
//        if(name.equals("Chinese Team") && lastPassedMark == 2) return;

        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        CompoundMark nextMark = courseOrder.get(lastPassedMark+1);

        double distanceGained = timePassed * speed;
        double distanceLeftInLeg = Course.greaterCircleDistance(currentLat, nextMark.getLat(), currentLon, nextMark.getLon());

        //If boat moves more than the remaining distance in the leg
        while(distanceGained > distanceLeftInLeg && lastPassedMark < courseOrder.size()-1){
            distanceGained -= distanceLeftInLeg;
            //Set boat position to next mark
            currentLat = nextMark.getLat();
            currentLon = nextMark.getLon();
            lastPassedMark++;

            if(lastPassedMark < courseOrder.size()-1){
                nextMark = courseOrder.get(lastPassedMark+1);
                distanceLeftInLeg = Course.greaterCircleDistance(currentLat, nextMark.getLat(), currentLon, nextMark.getLon());
            }
        }

        //Check if boat has finished
        if(lastPassedMark == courseOrder.size()-1){
            finished = true;
        } else{
            //Move the remaining distance in leg
            double percentGained = (distanceGained / distanceLeftInLeg);
            currentLat = currentLat + percentGained * (nextMark.getLat() - currentLat);
            currentLon = currentLon + percentGained * (nextMark.getLon() - currentLon);
        }
    }

    public int compareTo(Boat otherBoat){
        return otherBoat.getLastPassedMark() - lastPassedMark;
    }

    public String getName() {
        return this.name;
    }

    public double getSpeed() {
        return this.speed;
    }

    public int getFinishingPlace() {
        return this.finishingPlace;
    }

    public void setFinishingPlace(int place) {
        this.finishingPlace = place;
    }

    public Shape getIcon() {
        return icon;
    }

    public int getLastPassedMark() {
        return lastPassedMark;
    }

    public void setIcon(Shape icon) {
        this.icon = icon;
    }

    public double getCurrentLat() {
        return currentLat;
    }

    public double getCurrentLon() {
        return currentLon;
    }

    public boolean isFinished() {
        return finished;
    }
}
