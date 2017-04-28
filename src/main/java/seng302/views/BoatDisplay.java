package seng302.views;

import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import seng302.models.Boat;


/**
 * Encapsulates the display properties of the boat.
 */
public class BoatDisplay {

    private Boat boat;
    private Shape icon;
    private Polyline wake;
    private Label annotation;
    private Path path;

    private Color color;

    public BoatDisplay(Boat boat) {
        this.boat = boat;
        this.annotation = new Label();
        this.annotation.setCache(true);
        this.annotation.setCacheHint(CacheHint.SPEED);
    }

    public Label getAnnotation() {return annotation;}

    public void setWake(Polyline wake) {
        this.wake = wake;
    }

    public Polyline getWake() {
        return wake;
    }

    public void setIcon(Shape icon) {
        this.icon = icon;
    }

    public void setAnnotation(Label annotation) {this.annotation = annotation;}

    public Shape getIcon() {
        return icon;
    }

    public Boat getBoat() {
        return boat;
    }

    public void setPath(Path path) {this.path = path;}

    public Path getPath() {return path;}

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

