package seng302.views;

import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
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
    private Polyline SOGVector;
    private Polyline VMGVector;
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

    public Path getPath() {return path;}

    public double getCurrentVMGSpeed() {return boat.getCurrentVMGSpeed();}

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

    public void showVectors() {
        SOGVector.setVisible(true);
        VMGVector.setVisible(true);
    }

    public void hideVectors(){
        SOGVector.setVisible(false);
        VMGVector.setVisible(false);
    }

    public void unFocus(){
        icon.setOpacity(FADEDBOAT);
        wake.setOpacity(0.15);
        if(path.getElements().size() > 1){
            path.setOpacity(FADEDBOAT);
        }
        annotationLine.setOpacity(FADEDBOAT);
        annotation.setOpacity(FADEDBOAT);
        SOGVector.setOpacity(FADEDBOAT);
        VMGVector.setOpacity(FADEDBOAT);

    }

    public void focus(){
        icon.setOpacity(1);
        wake.setOpacity(0.5);
        if(path.getElements().size() > 1){
            path.setOpacity(1);
        }
        annotation.setOpacity(1);
        annotationLine.setOpacity(1);
        SOGVector.setOpacity(1);
        VMGVector.setOpacity(1);
    }
}

