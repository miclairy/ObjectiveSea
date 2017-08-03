package seng302.views;

import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.transform.Rotate;
import seng302.models.CanvasCoordinate;

/**
 * Polyline wrapper to make arrows easy
 */
public class Arrow extends Group{

    private double length;
    private double width;
    private Polyline arrowLine;
    private Line rightSide;
    private CanvasCoordinate center;


    public Arrow(double length, double width, CanvasCoordinate center) {
        this.length = length;
        this.width = width;
        this.center = center;
        double bottomY = center.getY() - length;
        arrowLine = new Polyline(center.getX() - width / 2, bottomY,
                                center.getX(), center.getY(),
                                width / 2  + center.getX(), bottomY);
        rightSide = new Line();
        relocate(center);
        arrowLine.setStrokeWidth(10.0);
        rightSide.setStrokeWidth(10.0);
    }

    public void relocate(CanvasCoordinate center) {
        this.center = center;
        double bottomY = center.getY() - length;
        arrowLine.relocate(center.getX(), center.getY());

    }

    public void rotate(double amount){
        arrowLine.getTransforms().add(new Rotate(amount, center.getX(), center.getY()));
    }

    public void addToCanvas(Group root){
        root.getChildren().add(arrowLine);
    }
}
