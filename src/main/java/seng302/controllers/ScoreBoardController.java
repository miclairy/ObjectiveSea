package seng302.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import seng302.models.Boat;
import seng302.models.Race;

/**
 * Created by Louis on 20-Apr-17.
 */
public class ScoreBoardController {

    // Controllers
    private Controller parent;
    private RaceViewController raceViewController;

    private final int PREP_SIGNAL_SECONDS_BEFORE_START = 120; //2 minutes
    private Race race;

    //FXML fields
    @FXML
    private CheckBox fpsToggle;
    @FXML
    private ListView<String> placings;
    @FXML
    private Slider annotationsSlider;
    @FXML
    private Label raceTimerLabel;

    public void setControllers(Controller parentToBe, RaceViewController raceViewController, Race race){
        this.parent = parentToBe;
        this.raceViewController = raceViewController;
        this.race = race;
    }

    public void setUp(){
        placings.setItems(parent.getFormattedDisplayOrder());
        raceTimerLabel.textProperty().bind(parent.raceTimerString);
        setupAnnotationControl();
    }


    @FXML
    /**
     * Called from the GUI when the fpsToggle checkbox is clicked. Updates visibility of fpsLabel.
     */
    private void fpsToggle(){
        parent.fpsLabel(fpsToggle.isSelected());
    }

    /**
     * Set up a listener for the annotation slider so that we can keep the annotations on the boats up to date with
     * the user's selection
     */
    private void setupAnnotationControl() {
        annotationsSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                raceViewController.changeAnnotations(newValue.intValue(), false);
            }
        });
        annotationsSlider.adjustValue(annotationsSlider.getMax());
    }

    /**
     * Called from the RaceViewController handle if the race has not yet begun (the boats are not moving)
     * Handles the starters Overlay and timing for the boats to line up on the start line
     * @param currentTime the current time
     * @param raceStartTime the time at which the race will begin and pre-race ends
     */
    public void handlePrerace(double currentTime, double raceStartTime){
        double overlayFadeTime = (raceStartTime - PREP_SIGNAL_SECONDS_BEFORE_START);
        if (currentTime > overlayFadeTime && parent.startersOverlay.isVisible()) {
            parent.hideStarterOverlay();
            raceViewController.initializeBoats();
        }
        if (currentTime >= raceStartTime) {
            parent.raceBegun = true;
            for (Boat boat : race.getCompetitors()){
                boat.maximiseSpeed();
            }
            raceViewController.changeAnnotations((int) annotationsSlider.getValue(), true);
        }
    }
}
