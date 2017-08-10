package seng302.utilities;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.util.Duration;
import seng302.controllers.Controller;

/**
 * Created by Devin on 25/07/17.
 */
public class AnimationUtils {

    /**
     * scales a collision node shape in and out to create explosion effect
     * @param node a circle representing the collision area
     * @param duration the length to run the explosion for
     * @param amount amount to scale explosion by
     * @return
     */
    public static ScaleTransition scaleTransitionCollision(Node node, int duration, double amount){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(duration), node);
        scaleTransition.setByY(amount);
        scaleTransition.setByX(amount);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        return scaleTransition;
    }

    /**
     * fades a node out of a scene
     * @param node the node to be faded out
     * @param duration the length of fade
     * @return
     */
    public static FadeTransition fadeOutTransition(Node node, int duration){
        FadeTransition fadeTransition = new FadeTransition(new Duration(duration), node);
        fadeTransition.setFromValue(node.getOpacity());
        fadeTransition.setToValue(0);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);

        return fadeTransition;
    }

    /**
     * scales a node up when mouse hovers over it
     * @param node the node to be scaled
     * @return the transition
     */
    public static ScaleTransition scaleButtonHover(Node node){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(200), node);
        scaleTransition.setByY(0.1);
        scaleTransition.setByX(0.1);
        scaleTransition.setInterpolator(Interpolator.EASE_IN);
        scaleTransition.play();
        return scaleTransition;
    }

    /**
     * scales a node back to defualt size once mouse exits
     * @param node the node to scale back
     * @return
     */
    public static ScaleTransition scaleButtonHoverExit(Node node){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(200), node);
        scaleTransition.setFromX(node.getScaleX());
        scaleTransition.setFromY(node.getScaleY());
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);
        scaleTransition.setInterpolator(Interpolator.EASE_IN);
        scaleTransition.play();
        return scaleTransition;
    }

    /**
     * scales and fades a node to remove it from the scene
     * @param node the node to scale and fade
     */
    public static void slideOutTransition(Node node){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(200), node);
        scaleTransition.setByY(-1);
        scaleTransition.setByX(-1);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeTransition = new FadeTransition(new Duration(100), node);
        fadeTransition.setFromValue(node.getOpacity());
        fadeTransition.setToValue(0);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition pt = new ParallelTransition(fadeTransition, scaleTransition);
        pt.play();
        fadeTransition.setOnFinished(new EventHandler<ActionEvent>(){public void handle(ActionEvent AE){node.setVisible(false); }});
    }

    /**
     * scales and fades in a node to the scene
     * @param node the node to bring in
     */
    public static void slideInTransition(Node node){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(200), node);
        scaleTransition.setFromX(node.getScaleX());
        scaleTransition.setFromY(node.getScaleY());
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeTransition = new FadeTransition(new Duration(100), node);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition pt = new ParallelTransition(fadeTransition, scaleTransition);
        pt.play();
    }

    /**
     * shifts a label from a text field to above it for input
     * @param label the label to slide
     */
    public static void shiftPromptLabel(Label label, int direction){
        TranslateTransition translateTransition = new TranslateTransition(new Duration(80), label);
        translateTransition.setByX(18 * direction);
        translateTransition.setByY(26 * direction);
        translateTransition.setInterpolator(Interpolator.EASE_IN);

        ScaleTransition scaleTransition = new ScaleTransition(new Duration(80), label);
        scaleTransition.setByX(0.15 * direction);
        scaleTransition.setByY(0.15 * direction);

        ParallelTransition pt = new ParallelTransition(translateTransition, scaleTransition);
        pt.play();

    }

    public static void transitionFXML(Node newScene){
        FadeTransition ft2 = new FadeTransition(Duration.millis(1000), newScene);
        ft2.setFromValue(0.0);
        ft2.setToValue(1.0);
        ft2.play();
    }

    public static void dullNode(Node node){
        FadeTransition fadeTransition = new FadeTransition(new Duration(100), node);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0.7);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeTransition.play();
    }

    public static void focusNode(Node node){
        FadeTransition fadeTransition = new FadeTransition(new Duration(100), node);
        fadeTransition.setFromValue(node.getOpacity());
        fadeTransition.setToValue(1);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeTransition.play();
    }

    public static void shiftPaneNodes(Node node, int amount, boolean visible){
        node.setVisible(true);
        TranslateTransition translateTransition = new TranslateTransition(new Duration(200), node);
        translateTransition.setByX(amount);
        translateTransition.setInterpolator(Interpolator.EASE_IN);
        if(!visible){
            translateTransition.setOnFinished(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent AE){
                    node.setVisible(false);
                }});
        }
        translateTransition.play();
    }

    public static void shiftPaneArrow(Node node, int amount, int rotation){
        TranslateTransition translateTransition = new TranslateTransition(new Duration(200), node);
        translateTransition.setByX(amount);
        translateTransition.setInterpolator(Interpolator.EASE_IN);

        RotateTransition rotateTransition = new RotateTransition(new Duration(200), node);
        rotateTransition.setByAngle(180 * rotation);
        rotateTransition.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition pt = new ParallelTransition(translateTransition, rotateTransition);
        pt.play();

    }

    public static void toggleHiddenBoardNodes(Node node, boolean visible){
        FadeTransition fadeTransition = new FadeTransition(new Duration(200), node);

        TranslateTransition translateTransition = new TranslateTransition(new Duration(200), node);
        translateTransition.setInterpolator(Interpolator.EASE_IN);

        if(visible){
            translateTransition.setFromY(node.getLayoutY());
            translateTransition.setToY(node.getLayoutY() - 10);
            fadeTransition.setFromValue(0.8);
            fadeTransition.setToValue(0);
            fadeTransition.setOnFinished(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent AE){
                    node.setVisible(false);
                }});
        }else{
            node.setVisible(true);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(0.8);
            translateTransition.setFromY(node.getLayoutY() - 10);
            translateTransition.setToY(node.getLayoutY());
        }
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition pt = new ParallelTransition(translateTransition, fadeTransition);
        pt.play();
    }

}
