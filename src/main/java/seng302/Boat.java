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

        if (cumulativeDistance <= distanceTraveled ) {
            lastPassedMark = currentMarkIndex;
            Mark finishMark = courseOrder.get(currentMarkIndex);
            currentLat = finishMark.getLat();
            currentLon = finishMark.getLon();
        } else{
            currentMarkIndex--;
            lastPassedMark = currentMarkIndex;
            double restOfLeg = cumulativeDistance - distanceTraveled;
            double legLength = course.distanceBetweenMarks(currentMarkIndex, currentMarkIndex + 1);
            double percent = 1 - (restOfLeg / legLength);

            Mark startMark = courseOrder.get(currentMarkIndex);
            Mark endMark = courseOrder.get(currentMarkIndex+1);

            // convert the lat lon to x y to display correctly
            // TODO boat gets to finsh in 2 loops???
            ArrayList<Double> startXY = DisplayUtils.convertFromLatLon(startMark.getLat(), startMark.getLon());
            ArrayList<Double> endXY = DisplayUtils.convertFromLatLon(endMark.getLat(), endMark.getLon());

            currentLat = (endXY.get(0)- startXY.get(0)) * percent + startXY.get(0);
            currentLon = (endXY.get(1)- startXY.get(1)) * percent + startXY.get(1);

        }
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
