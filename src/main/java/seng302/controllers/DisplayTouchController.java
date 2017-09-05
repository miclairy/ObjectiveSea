package seng302.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.TouchPoint;
import javafx.scene.shape.Circle;
import seng302.models.CanvasCoordinate;
import seng302.utilities.AnimationUtils;
import seng302.views.BoatDisplay;

import static seng302.utilities.DisplayUtils.zoomLevel;

/**
 * Created by Chris on 24/08/2017.
 */
public class DisplayTouchController {

    private Scene scene;
    public static Group root;
    private ScaleTransition touchTransition;

    public DisplayTouchController(Scene scene){
        this.scene = scene;
    }

    public void displayTouch(TouchPoint point){
            Circle highlightCircle1 = createHighlightCircle(point);

            touchTransition = AnimationUtils.touchInAction(highlightCircle1);
            touchTransition.play();
    }

    /**
     * creates and returns a circle
     * @param point canvas coord point that is used as the center of the circle
     */
    private Circle createHighlightCircle(TouchPoint point){
        Circle circle = new Circle();
        circle.setRadius(40);
        circle.setId("tapCircle");
        circle.setCenterX(point.getX());
        circle.setCenterY(point.getY());
        root.getChildren().add(circle);
        return circle;
    }

    public static void setRoot(Group root) {
        DisplayTouchController.root = root;
    }
}
