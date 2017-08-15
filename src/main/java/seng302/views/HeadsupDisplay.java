package seng302.views;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import seng302.utilities.AnimationUtils;

public class HeadsupDisplay {

    private BoatDisplay boat;
    private VBox display;

    public HeadsupDisplay(BoatDisplay boat, VBox display){
        this.boat = boat;
        this.display = display;
        addInfoToDisplay();
        addListeners();
    }

    private void addInfoToDisplay(){
        Label speedLabel = new Label();
        speedLabel.textProperty().bind(boat.getBoat().getSpeedProperty().asObject().asString());
        display.setPickOnBounds(false);
        speedLabel.setId("headsupDisplayLabel");
        display.getChildren().add(speedLabel);
    }

    private void addListeners(){
        display.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> AnimationUtils.scaleButtonHover(display));
        display.addEventHandler(MouseEvent.MOUSE_EXITED,
                e ->  AnimationUtils.scaleButtonHoverExit(display));
    }
}
