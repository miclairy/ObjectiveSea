package seng302.views;

import javafx.animation.ParallelTransition;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import seng302.data.StartTimingStatus;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.Node;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import seng302.models.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;


/**
 * Encapsulates the display properties of the boat.
 */
public class BoatDisplay {

    private Boat boat;
    private Shape icon;
    private Polyline wake;
    private VBox annotation;
    private Path path;
    private Line annotationLine;
    private final double FADEDBOAT = 0.3;

    private Color color;

    public BoatDisplay(Boat boat) {
        this.boat = boat;
        this.annotation = new VBox();
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

    public Path getPath() {return path;}

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

    public String getStartTimingAnnotation(){
            if(boat.getTimeStatus().equals(StartTimingStatus.EARLY)){
                return "-";
            } else if(boat.getTimeStatus().equals(StartTimingStatus.LATE)){
                return "+";
            }
            return "";
    }

    public void getStartTiming(Course course){
        Coordinate position = boat.getCurrentPosition();
        Coordinate startLine1 = course.getCourseOrder().get(0).getMark1().getPosition(); //position of startline
        Coordinate startLine2 = course.getCourseOrder().get(0).getMark2().getPosition(); //position of startline
        Boolean toLeftOfStart = pointOnLeftOfLine(position.getLat(),position.getLon(),startLine1.getLat(),startLine1.getLon(),startLine2.getLat(),startLine2.getLon()); //if left of startline e.g heading towards the line
        double timeToStart = 0;
        if(toLeftOfStart){
            double distanceToStart = position.greaterCircleDistance(startLine1); // maybe need to compute a midpoint? to be more accurate
            timeToStart = distanceToStart/boat.getSpeed() * 60 * 60; //converted to seconds (nautical miles/knots = hours)
        }
        if(timeToStart > 5.0){
            boat.setTimeStatus(StartTimingStatus.LATE);
        } if(timeToStart < 0.0){
            boat.setTimeStatus(StartTimingStatus.EARLY);
        } else{
            boat.setTimeStatus(StartTimingStatus.ONTIME);
        }
    }

    //doesn't yet work for the negative values
    public Boolean pointOnLeftOfLine(double pointLat, double pointLong, double mark1Lat, double mark1Long, double mark2Lat, double mark2Long){
        double determinant = (pointLong - mark1Long)*(mark2Lat - mark1Lat) - (pointLat - mark1Lat)*(mark2Long - mark1Long);
        if(determinant > 0){
            return true;
        } else {
            return false;
        }
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
    }

    /**
     * adds a fade transition to a node, so that a node fades over a set period of time
     * @param node a node in the scene that will be faded
     * @param endOpacity a double that represents the nodes opacity at the end of the fade
     */
    private void fadeNodeTransition(Node node, double endOpacity){
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setNode(node);
        fadeTransition.setDuration(new Duration(500));
        fadeTransition.setFromValue(node.getOpacity());
        fadeTransition.setToValue(endOpacity);
        fadeTransition.play();
    }
}

