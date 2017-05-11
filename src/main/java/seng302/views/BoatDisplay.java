package seng302.views;

import javafx.animation.ParallelTransition;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
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

    public void unFocus(){
        FadeTransition iconTransition = new FadeTransition();
        iconTransition.setNode(icon);
        iconTransition.setDuration(new Duration(500));
        iconTransition.setFromValue(1.0);
        iconTransition.setToValue(FADEDBOAT);

        FadeTransition wakeTransition = new FadeTransition();
        wakeTransition.setNode(wake);
        wakeTransition.setDuration(new Duration(500));
        wakeTransition.setFromValue(1.0);
        wakeTransition.setToValue(0.15);

        FadeTransition annoTransition = new FadeTransition();
        annoTransition.setNode(annotation);
        annoTransition.setDuration(new Duration(500));
        annoTransition.setFromValue(1.0);
        annoTransition.setToValue(FADEDBOAT);

        ParallelTransition allTransitions = new ParallelTransition(iconTransition, wakeTransition, annoTransition);
        allTransitions.play();
        if(path.getElements().size() > 1){
            path.setOpacity(FADEDBOAT);
        }
        annotationLine.setOpacity(FADEDBOAT);
    }

    public void focus(){
        FadeTransition iconTransition = new FadeTransition();
        iconTransition.setNode(icon);
        iconTransition.setDuration(new Duration(500));
        iconTransition.setFromValue(icon.getOpacity());
        iconTransition.setToValue(1.0);

        FadeTransition wakeTransition = new FadeTransition();
        wakeTransition.setNode(wake);
        wakeTransition.setDuration(new Duration(500));
        wakeTransition.setFromValue(wake.getOpacity());
        wakeTransition.setToValue(0.5);

        FadeTransition annoTransition = new FadeTransition();
        annoTransition.setNode(annotation);
        annoTransition.setDuration(new Duration(500));
        annoTransition.setFromValue(annotation.getOpacity());
        annoTransition.setToValue(1.0);

        ParallelTransition allTransitions = new ParallelTransition(iconTransition, wakeTransition, annoTransition);
        allTransitions.play();
        if(path.getElements().size() > 1){
            path.setOpacity(1);
        }
        annotationLine.setOpacity(1);
    }
}

