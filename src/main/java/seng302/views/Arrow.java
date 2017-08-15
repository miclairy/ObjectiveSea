package seng302.views;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import seng302.models.CanvasCoordinate;
import seng302.models.Coordinate;
import seng302.utilities.DisplayUtils;

/**
 * Polyline wrapper to make arrows easy
 */
public class Arrow extends Polyline {

    private double length;
    private double width;
    private Polyline arrowLine;
    private CanvasCoordinate center;


    public Arrow(double length, double width, Coordinate center) {
        this.length = length;
        this.width = width;
        this.center = DisplayUtils.convertFromLatLon(center);
        double bottomY = this.center.getY() - length;
        arrowLine = new Polyline(this.center.getX() - width / 2, bottomY,
                                this.center.getX(), this.center.getY(),
                                width / 2  + this.center.getX(), bottomY);
        relocate(this.center);
        arrowLine.setStrokeWidth(4.0);
        arrowLine.setId("distanceArrow");
    }

    public Arrow(double length, double width, Coordinate center, double heading) {
        this(length, width, center);
        rotate(heading);
    }

    /**
     * Moves the arrows to the given CanvasCoordinate center
     * @param center the canvas coordinate to move to
     */
    public void relocate(CanvasCoordinate center) {
        this.center = center;
        arrowLine.relocate(center.getX() - width / 2, center.getY() - length);
    }

    /**
     * Rotates the arrow by the given amount
     * @param amount The angle to rotate by
     */
    public void rotate(double amount){
        arrowLine.getTransforms().clear();
        arrowLine.getTransforms().add(new Rotate(amount, center.getX(), center.getY()));
    }

    public void addToCanvas(Group root){
        root.getChildren().add(arrowLine);
    }

    public void removeFromCanvas(Group root){
        root.getChildren().remove(arrowLine);
    }

    public void setColour(Color color){
        arrowLine.setStroke(color);
    }

    /**
     * Emphasizes the arrow
     */
    public void emphasize(){
        arrowLine.setOpacity(1.0);
        arrowLine.setStrokeWidth(5.0);
    }

    /**
     * Makes the arrow appear faded out
     */
    public void fade(){
        arrowLine.setOpacity(0.2);
        arrowLine.setStrokeWidth(4.0);
    }

    /**
     * Scales the arrow by zoomLevel in X and Y directions
     * @param zoomLevel the amount to scale by
     */
    public void setScale(Double zoomLevel) {
        arrowLine.setScaleY(zoomLevel);
        arrowLine.setScaleX(zoomLevel);
    }
}
