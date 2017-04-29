package seng302.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import seng302.utilities.DisplayUtils;
import seng302.models.Boat;
import seng302.models.Course;
import seng302.models.Race;
import seng302.utilities.TimeUtils;

import java.net.URL;
import java.util.*;

import static javafx.collections.FXCollections.observableArrayList;

public class Controller implements Initializable, Observer {

    @FXML
    public Canvas canvas;
    @FXML
    private ListView<String> placings;
    @FXML
    private GridPane sidePane;
    @FXML
    private Group root;
    @FXML
    private AnchorPane canvasAnchor;
    @FXML
    private Label fpsLabel;
    @FXML
    private CheckBox fpsToggle;
    @FXML
    private ListView<String> startersList;
    @FXML
    private ImageView imvCourseOverlay;
    @FXML
    private Pane raceClockPane;
    @FXML
    private Label raceTimerLabel;
    @FXML
    private Slider annotationsSlider;
    @FXML
    private Label clockLabel;
    @FXML
    private VBox startersOverlay;
    @FXML
    private Label startersOverlayTitle;
    @FXML
    private ImageView windDirectionImage;

    //number of from right edge of canvas that the wind arrow will be drawn
    private final int WIND_ARROW_OFFSET = 60;

    //FPS Counter
    public static SimpleStringProperty fpsString = new SimpleStringProperty();
    private static final long[] frameTimes = new long[100];
    private static int frameTimeIndex = 0 ;
    private static boolean arrayFilled = false ;

    //Race Clock
    public static SimpleStringProperty raceTimerString = new SimpleStringProperty();
    public static SimpleStringProperty clockString = new SimpleStringProperty();
    private static double totalRaceTime;

    private static ObservableList<String> formattedDisplayOrder = observableArrayList();
    private static double canvasHeight;
    private static double canvasWidth;

    private RaceViewController raceViewController;
    private boolean raceStartTimeChanged = true;
    private boolean raceStatusChanged = false;
    private double secondsElapsed = 0;
    private Race race;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        placings.setItems(formattedDisplayOrder);
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        race = Main.getRace();
        race.addObserver(this);

        Course course = race.getCourse();

        startersOverlayTitle.setText(race.getRegattaName());
        course.initCourseLatLon();
        race.setTotalRaceTime();

        DisplayUtils.setMaxMinLatLon(course.getMinLat(), course.getMinLon(), course.getMaxLat(), course.getMaxLon());
        raceViewController = new RaceViewController(root, race, this);

        course.addObserver(raceViewController);

        createCanvasAnchorListeners();

        setupAnnotationControl();
        fpsString.set("..."); //set to "..." while fps count loads
        fpsLabel.textProperty().bind(fpsString);
        totalRaceTime = race.getTotalRaceTime();

        secondsElapsed = race.getCurrentTimeInEpochMs() - race.getStartTimeInEpochMs();
        raceTimerLabel.textProperty().bind(raceTimerString);
        clockLabel.textProperty().bind(clockString);
        hideStarterOverlay();
        setWindDirection();
        displayStarters();
        raceViewController.start();
    }

    /**
     * Creates the change in width and height listeners to redraw course objects
     */
    private void createCanvasAnchorListeners(){
        canvasAnchor.widthProperty().addListener((observable, oldValue, newValue) -> {
            canvasWidth = (double) newValue;
            raceViewController.redrawCourse();
            raceViewController.moveWindArrow();
            raceViewController.redrawBoatPaths();
        });
        canvasAnchor.heightProperty().addListener((observable, oldValue, newValue) -> {
            canvasHeight = (double) newValue;
            raceViewController.redrawCourse();
            raceViewController.moveWindArrow();
            raceViewController.redrawBoatPaths();
        });
    }

    /**
     * Called from the RaceViewController handle if there is a change in race status
     * Handles the starters Overlay and timing for the boats objects to be created
     */
    public void updatePreRaceScreen(){
        switch(race.getRaceStatus()){
            case Race.WARNING_STATUS:
                showStarterOverlay();
                break;
            case Race.PREPARATORY_STATUS:
                hideStarterOverlay();
                raceViewController.initializeBoats();
                break;
            case Race.STARTED_STATUS:

                if(!raceViewController.hasInitializedBoats()){
                    raceViewController.initializeBoats();
                }
                break;
        }
    }

    /**
     * Sets the wind direction image to the correct rotation and position
     * Scales rotation value to be in degrees (a value between 0 and 360)
     */
    public void setWindDirection(){
        double windDirection = (float)race.getCourse().getWindDirection();
        double scaleFactor = ((double)360/(double)159999);
        double rotate = (windDirection * scaleFactor);
        windDirectionImage.setX(canvasWidth - WIND_ARROW_OFFSET);
        windDirectionImage.setRotate(rotate);
        raceViewController.setCurrentWindArrow(windDirectionImage);
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
     * Populate the starters overlay list with boats that are competing
     */
    public void displayStarters(){
        ObservableList<String> starters = observableArrayList();
        for (Boat boat : Main.getRace().getCompetitors()){
            starters.add(String.format("%s - %s", boat.getNickName(), boat.getName()));
        }
        startersList.setItems(starters);
    }

    /**
     * Keep the placings list up to date based on last past marked of boats
     */
    public void updatePlacings(){
        List<Boat> raceOrder = Main.getRace().getRaceOrder();
        formattedDisplayOrder.clear();
        for (int i = 0; i < raceOrder.size(); i++){
            Boat boat = raceOrder.get(i);
            String displayString = String.format("%d : %s (%s) - ", i+1, boat.getName(), boat.getNickName());
            if(raceOrder.get(i).isFinished()){
                displayString += "Finished!";
            } else{
                displayString += String.format("%.3f knots", boat.getSpeed());
            }
            formattedDisplayOrder.add(displayString);
        }
    }

    /**
     * Updates the fps counter to the current fps of the average of the last 100 frames of the Application.
     * @param now Is the current time
     */
    public void updateFPSCounter(long now) {
        long oldFrameTime = frameTimes[frameTimeIndex];
        frameTimes[frameTimeIndex] = now;
        frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length;
        if (frameTimeIndex == 0) {
            arrayFilled = true;
        }
        if (arrayFilled) {
            long elapsedNanos = now - oldFrameTime;
            long elapsedNanosPerFrame = elapsedNanos / frameTimes.length;
            double frameRate = 1_000_000_000.0 / elapsedNanosPerFrame;
            fpsString.set(String.format("%.1f", frameRate));
        }
    }

    /**
     * Updates the race clock to display the current time
     * @param timePassed the number of seconds passed since the last update call
     */
    public void updateRaceClock(double timePassed) {
        secondsElapsed += timePassed;
        if(totalRaceTime <= secondsElapsed) {
            secondsElapsed = totalRaceTime;
        }
        int hours = (int) secondsElapsed / 3600;
        int minutes = ((int) secondsElapsed % 3600) / 60;
        int seconds = (int) secondsElapsed % 60;
        if(secondsElapsed < 0) {
            raceTimerString.set(String.format("-%02d:%02d:%02d", Math.abs(hours), Math.abs(minutes), Math.abs(seconds)));
        } else {
            raceTimerString.set(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }
    }

    /**
     * Recalculates the base time (time when visualiser starts), needed when the expected start time of the race
     * changes
     */
    public void rebaseRaceClock(){
        if(raceStartTimeChanged){
            secondsElapsed = (race.getCurrentTimeInEpochMs() - race.getStartTimeInEpochMs()) / 1000;
        }
    }

    /**
     * displays the current time according to the UTC offset, in the GUI on the overlay
     */
    public static void setTimeZone(int UTCOffset) {
        clockString.set(TimeUtils.setTimeZone(UTCOffset));
    }


    @FXML
    /**
     * Called from the GUI when the fpsToggle checkbox is clicked. Updates visibility of fpsLabel.
     */
    private void fpsToggle(){
        fpsLabel.setVisible(fpsToggle.isSelected());
    }

    /**
     * Causes the starters overlay to hide itself, enabling a proper view of the course and boats beneath
     */
    private void hideStarterOverlay(){
        startersOverlay.setVisible(false);
    }

    private void showStarterOverlay(){
        startersOverlay.toFront();
        startersOverlay.setVisible(true);
    }

    public static void setCanvasHeight(double canvasHeight) {
        Controller.canvasHeight = canvasHeight;
    }

    public static void setCanvasWidth(double canvasWidth) {
        Controller.canvasWidth = canvasWidth;
    }

    public static double getCanvasHeight() {
        return canvasHeight;
    }

    public static double getCanvasWidth() {
        return canvasWidth;
    }

    public boolean hasRaceStatusChanged() {
        return raceStatusChanged;
    }

    public void setRaceStatusChanged(boolean raceStatusChanged) {
        this.raceStatusChanged = raceStatusChanged;
    }

    public boolean hasRaceStartTimeChanged() {
        return raceStartTimeChanged;
    }

    public void setRaceStartTimeChanged(boolean raceStartTimeChanged) {
        this.raceStartTimeChanged = raceStartTimeChanged;
    }

    /**
     * Changes aspects of the race visualizer based on changes in the race object it observes
     * Updates the pre-race overlay when its informed race status has changed
     * Updates the race clock when the expected start time changes
     * @param updatedRace the race that its race status changed
     * @param signal determines which part of the race has changed
     */
    @Override
    public void update(Observable updatedRace, Object signal) {
        if(this.race == updatedRace && signal instanceof Integer){
            Integer sig = (Integer) signal;
            switch(sig){
                case Race.UPDATED_STATUS_SIGNAL:

                    raceStatusChanged = true;
                    break;
                case Race.UPDATED_START_TIME_SIGNAL:
                    raceStartTimeChanged = true;
                    break;
            }
        }
    }
}