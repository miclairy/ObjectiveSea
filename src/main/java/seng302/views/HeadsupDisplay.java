package seng302.views;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import seng302.models.Race;
import seng302.utilities.AnimationUtils;

/** class to mange the heads up display
 *  shows when sidepanel toggled off
 */
public class HeadsupDisplay {

    private BoatDisplay boat;
    private VBox display;
    private Label positionLabel;
    private ProgressBar healthBar;
    private Race race;

    public HeadsupDisplay(BoatDisplay boat, VBox display, Race race){
        this.boat = boat;
        this.display = display;
        this.race = race;
        display.setPickOnBounds(false);
        addInfoToDisplay();
        addListeners();
    }

    private void addInfoToDisplay(){
        addSpeed();
        addPosition();
        addHealth();
    }

    /**
     * adds a label bound to the boats speed property to the display
     */
    private void addSpeed(){
        Label speedTitleLabel = new Label("Speed");
        speedTitleLabel.setId("titleLabel");

        Label speedLabel = new Label();
        speedLabel.setId("speedLabel");
        boat.getBoat().getSpeedProperty().addListener((obs, oldStatus, newStatus) ->
                Platform.runLater(() -> speedLabel.setText(String.format("%.2f kn", newStatus))));

        display.getChildren().add(speedTitleLabel);
        display.getChildren().add(speedLabel);
    }

    /**
     * adds a progress bar showing the boats health to the display
     */
    private void addHealth(){
        Label healthLabel = new Label("Health");
        healthLabel.setId("healthLabel");

        healthBar = new ProgressBar(boat.getBoat().getHealthProperty().doubleValue());
        healthBar.setMinWidth(120);
        boat.getBoat().getHealthProperty().addListener((obs, oldStatus, newStatus) ->
                Platform.runLater(() -> updateHealth((Double)newStatus)));
        healthBar.setId("boatHealth");

        display.getChildren().add(healthLabel);
        display.getChildren().add(healthBar);
    }

    /**
     * adds a label bound to the boats speed position property to the display
     */
    private void addPosition(){
        Label positionTitleLabel = new Label("Position");
        positionTitleLabel.setId("titleLabel");

        positionLabel = new Label(String.format("%d / %d", boat.getBoat().getCurrPlacingProperty().get(), race.getCompetitors().size()));
        positionLabel.setId("positionLabel");
        boat.getBoat().getCurrPlacingProperty().addListener((obs, oldStatus, newStatus) ->
                Platform.runLater(() -> updatePlacing((int)newStatus)));

        display.getChildren().add(positionTitleLabel);
        display.getChildren().add(positionLabel);
    }

    private void updatePlacing(int newPos){
        positionLabel.setText(String.format("%d / %d", newPos, race.getCompetitors().size()));
        AnimationUtils.drawAttentionToNode(positionLabel);
    }

    private void updateHealth(Double value){
        healthBar.setProgress(value);
    }

    /**
     * adds listeners to the vbox so it grows when mouse enters
     */
    private void addListeners(){
        display.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> AnimationUtils.scaleButtonHover(display));
        display.addEventHandler(MouseEvent.MOUSE_EXITED,
                e ->  AnimationUtils.scaleButtonHoverExit(display));
    }

    public void competitorAdded(){
        positionLabel.setText(String.format("%d / %d", boat.getBoat().getCurrPlacingProperty().get(), race.getCompetitors().size()));
    }
}
