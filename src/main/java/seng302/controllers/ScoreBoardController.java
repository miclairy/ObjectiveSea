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

    //FXML fields
    @FXML
    private CheckBox fpsToggle;
    @FXML
    private ListView<String> placings;
    @FXML
    private Slider annotationsSlider;
    @FXML
    private Label raceTimerLabel;

    public void setControllers(Controller parentToBe, RaceViewController raceViewController){
        this.parent = parentToBe;
        this.raceViewController = raceViewController;
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
        annotationsSlider.valueProperty().addListener((observable, oldValue, newValue) -> raceViewController.changeAnnotations(newValue.intValue(), false));
        annotationsSlider.adjustValue(annotationsSlider.getMax());
    }
}
