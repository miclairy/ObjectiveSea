package seng302.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import java.util.List;
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
    @FXML private CheckBox fpsToggle;
    @FXML private ListView<String> placings;
    @FXML private Slider annotationsSlider;
    @FXML private Label raceTimerLabel;
    @FXML private CheckBox chkName;
    @FXML private CheckBox chkSpeed;
    @FXML private CheckBox chkPassMarkTime;
    @FXML private CheckBox chkEst;
    @FXML public Button btnTrack;
    @FXML public CheckBox chkMultipleSelect;
    @FXML private CheckBox chkLaylines;
    @FXML private LineChart chtSparkLine;
    @FXML private NumberAxis xAxis ;
    @FXML private NumberAxis yAxis ;

    private Race race;

    public void setControllers(Controller parent, RaceViewController raceViewController){
        this.parent = parent;
        this.raceViewController = raceViewController;
    }

    public void setUp(){
        race = Main.getRace();
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

    public boolean isSpeedSelected(){return chkSpeed.isSelected();}

    public boolean isNameSelected(){return chkName.isSelected();}

    public boolean isEstSelected(){return chkEst.isSelected();}

    public boolean isTimePassedSelected(){return chkPassMarkTime.isSelected();}

    public boolean isLayLinesSelected(){
        return chkLaylines.isSelected();
    }

    public void addBoatToSparkLine(Series boatSeries){
        if(!chtSparkLine.getData().contains(boatSeries)){
            chtSparkLine.getData().add(boatSeries);
        }
    }
}
