package seng302;

import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

import java.util.ArrayList;

/**
 *
 */
public class BoatDisplay {

    private Boat boat;
    private Shape icon;
    private Polyline wake;
    private Text annotation;
    private Path path;

    public BoatDisplay(Boat boat) {
        this.boat = boat;
    }

    public Text getAnnotation() {return annotation;}

    public void setWake(Polyline wake) {
        this.wake = wake;
    }

    public Polyline getWake() {
        return wake;
    }

    public void setIcon(Shape icon) {
        this.icon = icon;
    }

    public void setAnnotation(Text annotation) {this.annotation = annotation;}

    public Shape getIcon() {
        return icon;
    }

    public Boat getBoat() {
        return boat;
    }

    public void setPath(Path path) {this.path = path;}

    public Path getPath() {return path;}
}

