package seng302;

import javafx.scene.shape.Shape;

import java.util.ArrayList;

/**
 * Created by mjt169 on 6/03/17.
 * Class to encapsulate properties associated with a boat.
 */

public class Boat {

    private String name;
    private double speed;
    private int finishingPlace;
    private Shape icon;
    private double currentLat = 50;
    private double currentLon = 100;
    private int lastPassedMark;

    public Boat(String name, double speed) {
        this.name = name;
        this.speed = speed;
    }


    public void setLocation(double totalTime, Course course) {
        double distanceTraveled = speed * totalTime;

        int currentMarkIndex = 0;
        double cumulativeDistance = 0;
        ArrayList<Mark> courseOrder = course.getCourseOrder();
        while(cumulativeDistance <= distanceTraveled && currentMarkIndex < courseOrder.size() - 1){
            cumulativeDistance += course.distanceBetweenMarks(currentMarkIndex, currentMarkIndex + 1);
            currentMarkIndex++;
        }
        currentMarkIndex--;

        lastPassedMark = currentMarkIndex;
        double restOfLeg = cumulativeDistance - distanceTraveled;
        double legLength = course.distanceBetweenMarks(currentMarkIndex, currentMarkIndex + 1);
        double percent = 1 - (restOfLeg / legLength);

        Mark startMark = courseOrder.get(currentMarkIndex);
        Mark endMark = courseOrder.get(currentMarkIndex+1);

        currentLat = (endMark.getLat() - startMark.getLat()) * percent + startMark.getLat();
        currentLon = (endMark.getLon() - startMark.getLon()) * percent + startMark.getLon();

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

}
