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
    private boolean isShowLaylines = true;

    private Color color;
    private Line layline1;

    private Line layline2;
    private Pair<Line, Line> boatLayLines;

    public BoatDisplay(Boat boat) {
        this.boat = boat;
        this.annotation = new VBox();
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

    public void setLaylines(Line layline1, Line layline2) {
        this.layline1 = layline1;
        this.layline2 = layline2;
    }

    public void setBoatLaylines(Pair<Line, Line> boatLayLines) {
        this.boatLayLines = boatLayLines;
    }

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
     * @param course the course the boat is on
     */
    public void getStartTiming(Course course){
        Coordinate position = boat.getCurrentPosition();
        CompoundMark startLine = course.getStartLine();
        Coordinate startLine1 = course.getCourseOrder().get(0).getMark1().getPosition(); //position of startline
        Coordinate startLine2 = course.getCourseOrder().get(0).getMark2().getPosition(); //position of startline
        Coordinate mark = course.getCourseOrder().get(0).getPosition(); //Position of first mark to determine which way the course goes
        Boolean toLeftOfStart = MathUtils.boatBeforeStartline(position.getLat(),position.getLon(),startLine1.getLat(),startLine1.getLon(),startLine2.getLat(),startLine2.getLon(),mark.getLat(),mark.getLon()); //checks if boat on correct side of the line
        double timeToStart = 0;
        double boatsHeading = boat.getHeading();
        double headingOfStartLine = startLine1.headingToCoordinate(startLine2);
        Boolean boatHeadingToStart = boatHeadingToLine(boatsHeading, headingOfStartLine, mark, startLine1);
        Coordinate midPointOfStart = MathUtils.calculateMidPoint(startLine);
        if(toLeftOfStart && boatHeadingToStart){
            double distanceToStart = position.greaterCircleDistance(midPointOfStart); // need to use Ray's formula
            timeToStart = distanceToStart/boat.getSpeed() * 60 * 60; //converted to seconds (nautical miles/knots = hours)
        } else if(!toLeftOfStart && !boatHeadingToStart){ // If boat is on the wrong side of the line but heading to the mark (from wrong direction), this checks if it is possible for the boat to even get there
            double distanceToStart = position.greaterCircleDistance(midPointOfStart);  // need to use Ray's formula
            timeToStart = distanceToStart/boat.getSpeed() * 60 * 60; //converted to seconds (nautical miles/knots = hours)
            if(timeToStart < 5.0){
                timeToStart = 0.0;
            }
        }
        if(timeToStart > 5.0){
            boat.setTimeStatus(StartTimingStatus.LATE);
        } if(timeToStart < 0.0){
            boat.setTimeStatus(StartTimingStatus.EARLY);
        }  else{
            boat.setTimeStatus(StartTimingStatus.ONTIME);
        }
    }


    public Boolean boatHeadingToLine(double boatsHeading, double lineHeading, Coordinate MarkPosition, Coordinate startLine){
        Boolean towardsLine = false;
        double headingOfMark = MarkPosition.headingToCoordinate(startLine);
        if(pointBetweenTwoAngle(lineHeading + 270, 90, boatsHeading) && pointBetweenTwoAngle(lineHeading + 90, 90, headingOfMark)){
            towardsLine = true;
        } else if(pointBetweenTwoAngle(lineHeading + 90, 90, boatsHeading) && pointBetweenTwoAngle(lineHeading + 270, 90, headingOfMark)){
            towardsLine = true;
        }
        return towardsLine;
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

    public void removeLaylines(Group root) {
        if (layline1 != null && layline2 != null) {
            root.getChildren().remove(layline1);
            root.getChildren().remove(layline2);
        }
        layline1= null;
        layline2= null;
    }

    public boolean isShowLaylines() {
        return isShowLaylines;
    }

    public void removeBoatLaylines(Group root) {
        if (boatLayLines != null) {
            root.getChildren().remove(boatLayLines.getKey());
            root.getChildren().remove(boatLayLines.getValue());
        }
        boatLayLines = null;
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
}

