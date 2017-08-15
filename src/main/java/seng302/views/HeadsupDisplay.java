package seng302.views;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import seng302.utilities.AnimationUtils;

public class HeadsupDisplay {

    private BoatDisplay boat;
    private VBox display;
    private Label headingLabel;
    private ProgressBar healthBar;

    public HeadsupDisplay(BoatDisplay boat, VBox display){
        this.boat = boat;
        this.display = display;
        display.setPickOnBounds(false);
        addInfoToDisplay();
        addListeners();
    }

    private void addInfoToDisplay(){
        Label speedLabel = new Label();
        speedLabel.setId("speedLabel");
        boat.getBoat().getSpeedProperty().addListener((obs, oldStatus, newStatus) ->
                Platform.runLater(() -> speedLabel.setText(String.format("%.2f kn", newStatus))));

        headingLabel = new Label(String.format("%.0f°", boat.getBoat().getHeadingProperty().getValue()));
        headingLabel.setId("headingLabel");
        boat.getBoat().getHeadingProperty().addListener((obs, oldStatus, newStatus) ->
                Platform.runLater(() -> updateHeading((Double)newStatus)));

        healthBar = new ProgressBar(boat.getBoat().getHealthProperty().doubleValue());
        boat.getBoat().getHealthProperty().addListener((obs, oldStatus, newStatus) ->
                Platform.runLater(() -> updateHealth((Double)newStatus)));
        healthBar.setId("boatHealth");

        Label healthLabel = new Label("Health");
        healthLabel.setId("healthLabel");

        display.getChildren().add(speedLabel);
        display.getChildren().add(headingLabel);
        display.getChildren().add(healthLabel);
        display.getChildren().add(healthBar);

    }

    private void updateHeading(Double newHeading){
        headingLabel.setText(String.format("%.0f°", newHeading));
    }

    private void updateHealth(Double value){
        healthBar.setProgress(value);
    }
    private void addListeners(){
        display.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> AnimationUtils.scaleButtonHover(display));
        display.addEventHandler(MouseEvent.MOUSE_EXITED,
                e ->  AnimationUtils.scaleButtonHoverExit(display));
    }
}
