package seng302;

import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

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
    private Shape icon;
    private Text annotation;
    private double currentLat;
    private double currentLon;
    private int lastPassedMark;
    private boolean finished;
    private Polyline wake;
    private double heading;

    public Boat(String name, String nickName, double speed) {
        this.name = name;
        this.nickName = nickName;
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

        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        CompoundMark nextMark = courseOrder.get(lastPassedMark+1);

        double distanceGained = timePassed * speed / 360; // 3600 for accurate speed
        double distanceLeftInLeg = Course.greaterCircleDistance(currentLat, nextMark.getLat(), currentLon, nextMark.getLon());

        //If boat moves more than the remaining distance in the leg
        while(distanceGained > distanceLeftInLeg && lastPassedMark < courseOrder.size()-1){
            distanceGained -= distanceLeftInLeg;
            //Set boat position to next mark
            currentLat = nextMark.getLat();
            currentLon = nextMark.getLon();
            lastPassedMark++;

            if(lastPassedMark < courseOrder.size()-1){
                heading = course.headingsBetweenMarks(lastPassedMark, lastPassedMark + 1);
                nextMark = courseOrder.get(lastPassedMark+1);
                distanceLeftInLeg = Course.greaterCircleDistance(currentLat, nextMark.getLat(), currentLon, nextMark.getLon());
            }
        }

        //Check if boat has finished
        if(lastPassedMark == courseOrder.size()-1){
            finished = true;
            heading = 0;
            speed = 0;
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

    public Shape getIcon() {
        return icon;
    }

    public int getLastPassedMark() {
        return lastPassedMark;
    }

    public void setIcon(Shape icon) {
        this.icon = icon;
    }

    public void setAnnotation(Text annotation) {this.annotation = annotation;}

    public double getCurrentLat() {
        return currentLat;
    }

    public double getCurrentLon() {
        return currentLon;
    }

    public boolean isFinished() {
        return finished;
    }

    public Text getAnnotation() {return annotation;}

    public void setWake(Polyline wake) {
        this.wake = wake;
    }

    public Polyline getWake() {
        return wake;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }
}
