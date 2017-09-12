package seng302.controllers;

import javafx.animation.ScaleTransition;
import javafx.scene.Scene;
import javafx.scene.input.TouchPoint;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.transform.Transform;
import seng302.models.CanvasCoordinate;
import seng302.utilities.AnimationUtils;
import seng302.utilities.MathUtils;

/**
 * Created by Chris on 24/08/2017.
 */
public class DisplayTouchController {

    private Scene scene;
    public static Pane touchPane;
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
        circle.setRadius(10);
        circle.setId("tapCircle");
        circle.setCenterX(point.getX());
        circle.setCenterY(point.getY());
        touchPane.getChildren().add(circle);
        return circle;
    }

    public static void setTouchPane(Pane touchPane) {
        DisplayTouchController.touchPane = touchPane;
    }

    public void displaySwipe(CanvasCoordinate currentCoordinate, CanvasCoordinate previousCoordinate) {
        if(MathUtils.distanceBetweenTwoPoints(currentCoordinate, previousCoordinate) > 100){
            previousCoordinate = currentCoordinate;
        }
        Line line = new Line(previousCoordinate.getX(), previousCoordinate.getY(), currentCoordinate.getX(), currentCoordinate.getY());
        line.setStrokeWidth(10.0);
        line.setStroke(Color.color(123.0/255.0, 209.0/255.0, 197.0/255.0));
        line.setId("touchLine");
        AnimationUtils.swipeAnimation(touchPane, line);
    }

}
