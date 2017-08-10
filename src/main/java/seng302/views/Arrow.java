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
    private Coordinate centerLatLon;


    public Arrow(double length, double width, Coordinate center) {
        this.length = length;
        this.width = width;
        this.centerLatLon = center;
        this.center = DisplayUtils.convertFromLatLon(center);
        double bottomY = this.center.getY() - length;
        arrowLine = new Polyline(this.center.getX() - width / 2, bottomY,
                                this.center.getX(), this.center.getY(),
                                width / 2  + this.center.getX(), bottomY);
        relocate(this.center);
        arrowLine.setStrokeWidth(4.0);
        arrowLine.setId("distanceArrow");
//        setOpacity(0);
    }

    public Arrow (double length, double width, Coordinate center, double rotation, Color color){
        this(length, width, center);
        rotate(rotation);
        setColour(color);


    }

    public void relocate(CanvasCoordinate center) {
        this.center = center;
        arrowLine.relocate(center.getX() - width / 2, center.getY() - length);

    }

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

    public Polyline getArrowLine() {
        return arrowLine;
    }

    public void setColour(Color color){
        arrowLine.setStroke(color);
    }

    public void setVisible1(Boolean visible){
        arrowLine.setVisible(visible);
    }

    public void setOpacity1(Double value){
        arrowLine.setOpacity(value);
    }

    public void setStrokeWidth1(Double value){
        arrowLine.setStrokeWidth(value);
    }

    public void setScale(Double zoomLevel) {
        arrowLine.setScaleY(zoomLevel);
        arrowLine.setScaleX(zoomLevel);
    }
}
