package seng302.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.util.StringConverter;
import seng302.utilities.DisplayUtils;
import seng302.views.BoatDisplay;

/**
 * Created by Louis on 20-Apr-17.
 *
 */
public class ScoreBoardController {

    // Controllers
    private Controller parent;
    private RaceViewController raceViewController;

    //FXML fields
    @FXML private CheckBox fpsToggle;
    @FXML private ListView<String> placings;
    @FXML private Slider annotationsSlider;
    @FXML private Label raceTimerLabel;
    @FXML private CheckBox chkName;
    @FXML private CheckBox chkSpeed;
    @FXML private CheckBox chkPassMarkTime;
    @FXML private CheckBox chkEst;
    @FXML private CheckBox zoomToggle;
    @FXML public Button btnTrack;

    // Class Variables
    private boolean zoomed = false;


    public void setControllers(Controller parent, RaceViewController raceViewController){
        this.parent = parent;
        this.raceViewController = raceViewController;
    }

    public void setUp(){
        placings.setItems(parent.getFormattedDisplayOrder());
        raceTimerLabel.textProperty().bind(parent.raceTimerString);
        setupAnnotationControl();
        annotationsSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if(n < 1) return "None";
                if(n < 2) return "Important";
                if(n < 3) return "All";

                return "All";
            }

            @Override
            public Double fromString(String s) {
                switch(s) {
                    case "None":
                        return 0d;
                    case "Important":
                        return 1d;
                    case "All":
                        return 2d;
                    default:
                        return 2d;
                }
            }
        });
    }

    @FXML
    /**
     * Called from the GUI when the fpsToggle checkbox is clicked. Updates visibility of fpsLabel.
     */
    private void fpsToggle(){
        parent.fpsLabel(fpsToggle.isSelected());
    }

    @FXML
    private void btnTrackPressed(){
        BoatDisplay selectedBoat = raceViewController.getSelectedBoat();
        if(selectedBoat != null){
            parent.setZoomSliderValue(3);
            raceViewController.setTrackingBoat(true);
            raceViewController.redrawCourse();
            raceViewController.moveWindArrow();
            raceViewController.redrawBoatPaths();
            raceViewController.setMapVisibility(false);
        }

    }

    /**
     * Set up a listener for the annotation slider so that we can keep the annotations on the boats up to date with
     * the user's selection
     */
    private void setupAnnotationControl() {
        annotationsSlider.valueProperty().addListener((observable, oldValue, newValue) -> raceViewController.changeAnnotations(newValue.intValue(), false));
        zoomToggle.selectedProperty().addListener((observable, oldValue, newValue) -> raceViewController.zoomToggle(newValue));
        annotationsSlider.adjustValue(annotationsSlider.getMax());
    }

    public boolean isSpeedSelected(){return chkSpeed.isSelected();}

    public boolean isNameSelected(){return chkName.isSelected();}

    public boolean isEstSelected(){return chkEst.isSelected();}

    public boolean isTimePassedSelected(){return chkPassMarkTime.isSelected();}



}
