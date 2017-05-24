package seng302.views;

import javafx.scene.Group;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.util.Pair;
import seng302.data.StartTimingStatus;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.Node;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import seng302.models.*;
import seng302.utilities.MathUtils;
import seng302.utilities.DisplayUtils;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.Observable;
import java.util.Observer;

import static seng302.utilities.DisplayUtils.fadeNodeTransition;

import static seng302.utilities.MathUtils.pointBetweenTwoAngle;


/**
 * Encapsulates the display properties of the boat.
 */
public class BoatDisplay implements Observer {

    private Boat boat;
    private Shape icon;
    private Polyline wake;
    private VBox annotation;
    private Path path;
    private Line annotationLine;
    private Polyline SOGVector;
    private Polyline VMGVector;
    private Series series;
    private final double FADEDBOAT = 0.3;

    private Laylines laylines;
    private PolarTable polarTable;
    private Color color;

    public BoatDisplay(Boat boat, PolarTable polarTable) {
        this.boat = boat;
        this.annotation = new VBox();
        this.laylines = new Laylines();
        this.polarTable = polarTable;
        this.series = new Series();
        series.getData().add(new Data(boat.getLastRoundedMarkIndex(), boat.getCurrPlacing()));

    }

    public Line getAnnotationLine() {return annotationLine;}

    public void setAnnotationLine(Line line) {this.annotationLine = line;}

    public VBox getAnnotation() {return annotation;}

    public void setWake(Polyline wake) {
        this.wake = wake;
    }

    public Polyline getWake() {
        return wake;
    }

    public Polyline getSOGVector() {
        return SOGVector;
    }

    public void setSOGVector(Polyline SOGVector) {
        this.SOGVector = SOGVector;
    }

    public Polyline getVMGVector() {return VMGVector;}

    public void setVMGVector(Polyline VMGVector) {this.VMGVector = VMGVector;}

    public void setIcon(Shape icon) {
        this.icon = icon;
    }

    public void setAnnotation(VBox annotation) {this.annotation = annotation;}

    public Shape getIcon() {
        return icon;
    }

    public Boat getBoat() {
        return boat;
    }

    public void setPath(Path path) {this.path = path;}

    public Path getPath() {return
            path;}

    public String getSpeed(){
        return String.format("%.1fkn", boat.getSpeed());
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getTimeSinceLastMark(long currTime){
        String timeSincePassed;
        if(boat.getLastRoundedMarkTime() == 0){
            timeSincePassed = "-";
        }else{
            long timeElapsed = currTime - boat.getLastRoundedMarkTime();
            Instant instant = Instant.ofEpochMilli(timeElapsed);
            ZonedDateTime zdt = ZonedDateTime.ofInstant (instant , ZoneOffset.UTC );
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern ("mm:ss");
            timeSincePassed = formatter.format(zdt);
        }
        return timeSincePassed;
    }

    public String getTimeToNextMark(long timeAtMark, long currTime){
        String timeTillMark;
        if (timeAtMark > 0) {
            long ConvertedTime = (timeAtMark - currTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss");
            Instant instant = Instant.ofEpochMilli(ConvertedTime);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
            timeTillMark = formatter.format(zdt);
        } else {
            timeTillMark = "-";
        }
        return timeTillMark;
    }

    /**
     * A getter for the start timing annotation
     * @return a string representation of the boat being early/late or on time
     */
    public String getStartTimingAnnotation(){
            if(boat.getTimeStatus().equals(StartTimingStatus.EARLY)){

                return "- : Early";
            } else if(boat.getTimeStatus().equals(StartTimingStatus.LATE)){
                return "+ : Late";
            }
            return "";
    }

    /**
     * Set's the boats start timing status to Late if the boat is going to be more than 5sec late, early if it'll cross the start line early or nothing if neither
     * @param race the race the boat is in
     */
    public void getStartTiming(Race race){
        long secondsElapsed = (race.getCurrentTimeInEpochMs() - race.getStartTimeInEpochMs()) / 1000; //time till race starts
        Course course = race.getCourse();
        Coordinate position = boat.getCurrentPosition();
        Coordinate startLine1 = course.getCourseOrder().get(0).getMark1().getPosition(); //position of start line mark 1
        Coordinate startLine2 = course.getCourseOrder().get(0).getMark2().getPosition(); //position of start line mark 2

        Coordinate mark = course.getCourseOrder().get(1).getPosition(); //Position of first mark to determine which side of the course the start line is on
        Boolean correctSideOfStart = MathUtils.boatBeforeStartline(position.getLat(),position.getLon(),startLine1.getLat(),startLine1.getLon(),startLine2.getLat(),startLine2.getLon(),mark.getLat(),mark.getLon()); //checks if boat on correct side of the line

        double timeToStart;
        double timeToCrossStartLine = 0;
        double boatsHeading = boat.getHeading();
        double headingOfStartLine = startLine1.headingToCoordinate(startLine2);
        double headingOfMark = mark.headingToCoordinate(startLine1);

        Boolean boatHeadingToStart = MathUtils.boatHeadingToLine(boatsHeading, headingOfStartLine, headingOfMark); //Checks if the boat is heading towards the start line from either direction
        InfiniteLine startlineInf = new InfiniteLine(startLine1,startLine2); //creates an infinite line in that contains the startline
        Coordinate closestPoint = startlineInf.closestPoint(position); //finds the closest point from the boat to the previous infinite line
        //Calculates whether the closest point from the boat is on the start line, if it isn't then the closest point is the closest end
        double distanceToStart;
        if(closestPoint.getLat() < Math.min(startLine1.getLat(),startLine2.getLat()) || closestPoint.getLat() > Math.max(startLine1.getLat(),startLine2.getLat())){
            double distanceToStartLine1 = position.greaterCircleDistance(startLine1);
            double distanceToStartLine2 = position.greaterCircleDistance(startLine2);
            distanceToStart = Math.min(distanceToStartLine1,distanceToStartLine2); // finds which end is closest
        } else {
            distanceToStart = position.greaterCircleDistance(closestPoint); //if the closest point is on the start line already
        }
        //If the boat is on the correct side of the start and heading towards it

        if(correctSideOfStart && boatHeadingToStart){
            timeToStart = distanceToStart/boat.getSpeed() * 60 * 60; //converted to seconds (nautical miles/knots = hours)
            timeToCrossStartLine = timeToStart + secondsElapsed;
        } else if(!correctSideOfStart && !boatHeadingToStart){ // If boat is on the wrong side of the line but heading to the mark (from wrong direction), this checks if it is possible for the boat to even get there in time
            timeToStart = distanceToStart/boat.getSpeed() * 60 * 60; //converted to seconds (nautical miles/knots = hours)
            timeToCrossStartLine = timeToStart + secondsElapsed;
            if(timeToCrossStartLine < 5.0){ //if it is possible for the boat to get to the other side then we can't tell much else about it
                timeToCrossStartLine = 0.0;
            }
        }
        if(timeToCrossStartLine > 5.0){ //Time to cross the line is greater than 5 sec so will be late
            boat.setTimeStatus(StartTimingStatus.LATE);
        } else if(timeToCrossStartLine < 0.0){ //Time to cross the line is less than zero so will be early
            boat.setTimeStatus(StartTimingStatus.EARLY);
        } else { //else the boat is essentially on time
            boat.setTimeStatus(StartTimingStatus.ONTIME);
        }
    }


    public void showVectors() {
        SOGVector.setVisible(true);
        VMGVector.setVisible(true);
    }

    public void hideVectors(){
        SOGVector.setVisible(false);
        VMGVector.setVisible(false);
    }

    public void unFocus(){
        fadeNodeTransition(icon, FADEDBOAT);
        fadeNodeTransition(wake, FADEDBOAT);
        fadeNodeTransition(annotation, FADEDBOAT);
        if(path != null){
            fadeNodeTransition(path, FADEDBOAT);
        }
        annotationLine.setOpacity(FADEDBOAT);
    }

    public void focus(){
        fadeNodeTransition(icon, 1.0);
        fadeNodeTransition(wake, 1.0);
        fadeNodeTransition(annotation, 1.0);
        if(path != null){
            fadeNodeTransition(path, 1.0);
        }
        annotationLine.setOpacity(1);
        SOGVector.setOpacity(1);
        VMGVector.setOpacity(1);
    }

    public Series getSeries() {return series;}

    /**
     * updates display boat when boat passes a mark and positon updates. Adds new position to sparkline
     * @param boatObservable the boat that has an updated placing
     * @param arg
     */
    @Override
    public void update(Observable boatObservable, Object arg) {
        Boat boat = (Boat) boatObservable;
        series.getData().add(new Data(boat.getLastRoundedMarkIndex(), boat.getCurrPlacing()));
    }


    public Laylines getLaylines() {
        return laylines;
    }

    public void setLaylines(Laylines laylines) {
        this.laylines = laylines;
    }

    public PolarTable getPolarTable() {
        return polarTable;
    }
}

