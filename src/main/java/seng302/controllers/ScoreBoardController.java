package seng302.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import seng302.models.Boat;
import seng302.models.Race;

import java.util.Objects;

/**
 * Created by Louis on 20-Apr-17.
 */
public class ScoreBoardController {

    // Controllers
    private Controller parent;
    private RaceViewController raceViewController;
    private Race race;

    //FXML fields
    @FXML private CheckBox fpsToggle;
    @FXML private ListView<String> placings;
    @FXML private Slider annotationsSlider;
    @FXML private Label raceTimerLabel;
    @FXML private CheckBox chkName;
    @FXML private CheckBox chkSpeed;
    @FXML private CheckBox chkPassMarkTime;
    @FXML private CheckBox chkEst;
    @FXML public Button btnTrack;
    @FXML private ComboBox<String> boatDropDown1;
    @FXML private ComboBox<String> boatDropDown2;

    public void setControllers(Controller parent, RaceViewController raceViewController, Race race){
        this.parent = parent;
        this.raceViewController = raceViewController;
        this.race = race;
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
        for (Boat boat : race.getCompetitors()){
            boatDropDown1.getItems().addAll(boat.getName());
            boatDropDown2.getItems().addAll(boat.getName());
        }
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

    @FXML
    private void drawDistanceLines(){
        Boat firstBoat = null;
        Boat secondBoat = null;
        for (Boat boat : race.getCompetitors()){
            if (Objects.equals(boat.getName(), boatDropDown1.getValue())){
                System.out.println(boat.getName());
                System.out.println(boatDropDown1.getValue());
                firstBoat = boat;
            }
            if (Objects.equals(boat.getName(), boatDropDown2.getValue())){
                secondBoat = boat;
            }
        }
        boatDropDown1.setValue(null);
        boatDropDown2.setValue(null);
        assert firstBoat != null;
        System.out.println(firstBoat.getName() + ", " + secondBoat.getName());
        raceViewController.updateDistanceLine(firstBoat, secondBoat);
    }

    public boolean isSpeedSelected(){return chkSpeed.isSelected();}

    public boolean isNameSelected(){return chkName.isSelected();}

    public boolean isEstSelected(){return chkEst.isSelected();}

    public boolean isTimePassedSelected(){return chkPassMarkTime.isSelected();}
}
