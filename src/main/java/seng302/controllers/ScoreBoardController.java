package seng302.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.StringConverter;
import seng302.views.BoatDisplay;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import seng302.models.Race;
import seng302.models.Boat;

import java.util.Objects;

/**
 * Created by Louis on 20-Apr-17.
 *
 */
public class ScoreBoardController {

    // Controllers
    private Controller parent;
    private RaceViewController raceViewController;
    private Race race;
    private SelectionController selectionController;

    //FXML fields
    @FXML private CheckBox fpsToggle;
    @FXML private ListView<String> placings;
    @FXML private Slider annotationsSlider;
    @FXML private Label raceTimerLabel;
    @FXML private CheckBox chkName;
    @FXML private CheckBox chkSpeed;
    @FXML private CheckBox chkPassMarkTime;
    @FXML private CheckBox chkEst;
    @FXML private CheckBox chkStart;
    @FXML private CheckBox zoomToggle;
    @FXML public Button btnTrack;
    @FXML private CheckBox chkLaylines;
    @FXML private CheckBox chkVectors;
    @FXML private LineChart chtSparkLine;
    @FXML private NumberAxis xAxis ;
    @FXML private NumberAxis yAxis ;
    @FXML private CheckBox DistanceLinesToggle;



    public void setControllers(Controller parent, RaceViewController raceViewController, Race race, SelectionController selectionController){
        this.parent = parent;
        this.raceViewController = raceViewController;
        this.selectionController = selectionController;
        this.race = race;
    }

    public class YourFormatCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(item);
            setTextFill(Color.WHITE);

            BoatDisplay userBoat = raceViewController.getCurrentUserBoatDisplay();
            if(userBoat != null && item != null){
                if(item.contains(userBoat.getBoat().getName())){
                    setTextFill(Color.rgb(77, 197, 138));

                }
            }

        }
    }

    public void setUp(){
        race = Client.getRace();

        placings.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> list) {
                return new YourFormatCell();
            }
        });

        placings.setItems(parent.getFormattedDisplayOrder());
        raceTimerLabel.textProperty().bind(parent.raceTimerString);
        setupAnnotationControl();
        setupSparkLine();
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
        annotationsSlider.setValue(1);
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
        selectionController.trackBoat();
    }

    /**
     * Set up a listener for the annotation slider so that we can keep the annotations on the boats up to date with
     * the user's selection
     */
    private void setupAnnotationControl() {
        annotationsSlider.valueProperty().addListener((observable, oldValue, newValue) -> raceViewController.changeAnnotations(newValue.intValue(), false));
        zoomToggle.selectedProperty().addListener((observable, oldValue, newValue) -> selectionController.zoomToggle(newValue));
        annotationsSlider.adjustValue(annotationsSlider.getMax());
    }

    private void setupSparkLine(){
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(race.getCourse().getCourseOrder().size());
        xAxis.setTickUnit(1);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(race.getCompetitors().size() + 1);
        yAxis.setUpperBound(0);
        yAxis.setTickUnit(1);
        chtSparkLine.setCreateSymbols(false);
        chtSparkLine.setLegendVisible(false);
        chtSparkLine.getYAxis().setTickLabelsVisible(false);
        chtSparkLine.getXAxis().setTickLabelsVisible(false);
        chtSparkLine.getXAxis().setTickLength(0);
        chtSparkLine.getYAxis().setTickLength(0);
    }

    @FXML
    private void toggleDistanceLines() {
        raceViewController.updateDistanceLine(DistanceLinesToggle.isSelected());
    }

    public boolean isSpeedSelected(){return chkSpeed.isSelected();}

    public boolean isNameSelected(){return chkName.isSelected();}

    public boolean isStartTimeSelected(){return chkStart.isSelected();}

    public boolean isEstSelected(){return chkEst.isSelected();}

    public boolean isTimePassedSelected(){return chkPassMarkTime.isSelected();}

    public boolean areVectorsSelected() {
        return chkVectors.isSelected();
    }

    public boolean isLayLinesSelected(){
        return chkLaylines.isSelected();
    }

    public boolean isDistanceLineSelected(){return DistanceLinesToggle.isSelected();}

    public void addBoatToSparkLine(Series boatSeries){
        if(!chtSparkLine.getData().contains(boatSeries)){
            chtSparkLine.getData().add(boatSeries);
        }
    }
}
