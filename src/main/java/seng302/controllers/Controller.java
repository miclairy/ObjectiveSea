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
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polyline;
import seng302.utilities.DisplayUtils;
import seng302.models.Boat;
import seng302.models.Course;
import seng302.models.Race;
import seng302.utilities.TimeUtils;

import java.net.URL;
import java.util.*;

import static javafx.collections.FXCollections.observableArrayList;

public class Controller implements Initializable, Observer {

    @FXML public Canvas canvas;
    @FXML private Group root;
    @FXML private AnchorPane canvasAnchor;
    @FXML private AnchorPane rightHandSide;
    @FXML private Label fpsLabel;
    @FXML private ListView<String> startersList;
    @FXML private Label clockLabel;
    @FXML public VBox startersOverlay;
    @FXML private Label startersOverlayTitle;
    @FXML private ImageView windDirectionImage;
    @FXML public ImageView mapImageView;
    @FXML private Slider zoomSlider;
    @FXML public Label lblUserHelp;

    //number of from right edge of canvas that the wind arrow will be drawn
    private final int WIND_ARROW_OFFSET = 60;

    //FPS Counter
    private SimpleStringProperty fpsString = new SimpleStringProperty();
    private final long[] frameTimes = new long[100];
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;

    //Race Clock
    public SimpleStringProperty raceTimerString = new SimpleStringProperty();
    private SimpleStringProperty clockString = new SimpleStringProperty();

    private ObservableList<String> formattedDisplayOrder = observableArrayList();
    private static double canvasHeight;
    private static double canvasWidth;

    private static double anchorHeight;
    private static double anchorWidth;
    private static String timeZone;
    private final String BOAT_CSS = "/style/boatStyle.css";
    private final String COURSE_CSS = "/style/courseStyle.css";
    private final String STARTERS_CSS = "/style/startersOverlayStyle.css";
    private final String SETTINGSPANE_CSS = "/style/settingsPaneStyle.css";
    private final String DISTANCELINE_CSS = "/style/distanceLineStyle.css";

    // Controllers
    @FXML private RaceViewController raceViewController;
    @FXML private ScoreBoardController scoreBoardController = new ScoreBoardController();

    public boolean raceBegun;
    private boolean raceStatusChanged = true;
    private Race race;


    private final double FOCUSED_ZOOMSLIDER_OPACITY =0.8;
    private final double IDLE_ZOOMSLIDER_OPACITY = 0.4;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        canvasAnchor.getStylesheets().addAll(BOAT_CSS, COURSE_CSS, STARTERS_CSS, SETTINGSPANE_CSS, DISTANCELINE_CSS);
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        anchorWidth = canvasAnchor.getWidth();
        anchorHeight = canvasAnchor.getHeight();

        race = Main.getRace();
        race.addObserver(this);
        Course course = race.getCourse();
        startersOverlayTitle.setText(race.getRegattaName());
        course.initCourseLatLon();
        DisplayUtils.setMaxMinLatLon(course.getMinLat(), course.getMinLon(), course.getMaxLat(), course.getMaxLon());
        raceViewController = new RaceViewController(root, race, this, scoreBoardController);
        course.addObserver(raceViewController);

        createCanvasAnchorListeners();
        scoreBoardController.setControllers(this, raceViewController, race);
        scoreBoardController.setUp();
        fpsString.set("..."); //set to "..." while fps count loads
        fpsLabel.textProperty().bind(fpsString);
        clockLabel.textProperty().bind(clockString);
        hideStarterOverlay();
        raceViewController.updateWindArrow();

        displayStarters();
        startersOverlay.toFront();
        raceViewController.start();
        initDisplayDrag();
        initZoom();
    }

    /**
     * initilizes display listeners to detect dragging on display. Calls DisplayUtils to move display
     * and redraw course and paths as appropriate.
     */
    private void initDisplayDrag(){
        canvasAnchor.setOnMouseDragged(event -> {
            if(DisplayUtils.zoomLevel != 1){
                DisplayUtils.dragDisplay((int)event.getX(),(int) event.getY());
                raceViewController.redrawCourse();
                raceViewController.redrawBoatPaths();
            }
        });
    }

    /**
     * Initilizes zoom slider on display. Resets zoom on slide out
     */
    private void initZoom(){
        //Zoomed out
        zoomSlider.valueProperty().addListener((arg0, arg1, arg2) -> {
            zoomSlider.setOpacity(FOCUSED_ZOOMSLIDER_OPACITY);
            DisplayUtils.setZoomLevel(zoomSlider.getValue());
            if(DisplayUtils.zoomLevel != 1){
                mapImageView.setVisible(false);
            }else{
                //Zoom out full, reset everything
                raceViewController.setRotationOffset(0);
                root.getTransforms().clear();
                mapImageView.setVisible(true);
                raceViewController.setTrackingPoint(false);
                DisplayUtils.resetOffsets();
            }
            raceViewController.redrawCourse();
            raceViewController.redrawBoatPaths();
        });
    }


    /**
     * Creates the change in width and height listeners to redraw course objects
     */
    private void createCanvasAnchorListeners(){

        final ChangeListener<Number> resizeListener = new ChangeListener<Number>()
        {
            final Timer timer = new Timer(); // uses a timer to call your resize method
            TimerTask task = null; // task to execute after defined delay
            final long delayTime = 300; // delay that has to pass in order to consider an operation done

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, final Number newValue)
            {
                if (task != null)
                {
                    task.cancel(); // cancel it, we have a new size to consider
                    //zoom and blur image

                    mapImageView.setEffect(new GaussianBlur(300));
                }

                task = new TimerTask() // create new task that calls your resize operation
                {
                    @Override
                    public void run()
                    {
                        // resize after time is waited
                        raceViewController.drawMap();
                        mapImageView.setEffect(null);
                    }
                };
                // schedule new task
                timer.schedule(task, delayTime);
            }
        };


        canvasAnchor.widthProperty().addListener(resizeListener);
        canvasAnchor.widthProperty().addListener((observable, oldValue, newValue) -> {
            canvasWidth = (double) newValue;
            anchorWidth = canvasAnchor.getWidth();
            raceViewController.redrawCourse();
            raceViewController.redrawBoatPaths();
        });
        canvasAnchor.heightProperty().addListener(resizeListener);
        canvasAnchor.heightProperty().addListener((observable, oldValue, newValue) -> {
            canvasHeight = (double) newValue;
            anchorHeight = canvasAnchor.getHeight();
            raceViewController.redrawCourse();
            raceViewController.redrawBoatPaths();
        });

    }


    /**
     * Called from the RaceViewController handle if there is a change in race status
     * Handles the starters Overlay and timing for the boats objects to be created
     */
    public void updatePreRaceScreen(){
        switch(race.getRaceStatus()){
            case WARNING:
                showStarterOverlay();
                break;
            case PREPARATORY:
                hideStarterOverlay();
                raceViewController.initializeBoats();
                break;
            case STARTED:
                if(startersOverlay.isVisible()){
                    hideStarterOverlay();
                }
                if(!raceViewController.hasInitializedBoats()){
                    raceViewController.initializeBoats();
                }
                raceViewController.initBoatPaths();
                break;
        }
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
            String displayString = "";
            if(boat.getLastRoundedMarkIndex() != -1) {
                displayString = String.format("%d : %s (%s) - ", i+1, boat.getName(), boat.getNickName());
                if (raceOrder.get(i).isFinished()) {
                    displayString += "Finished!";
                } else {
                    displayString += String.format("%.3f knots", boat.getSpeed());
                }
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
     * Updates the race clock to display the current time in race
     */
    public void updateRaceClock() {
        long secondsElapsed = (race.getCurrentTimeInEpochMs() - race.getStartTimeInEpochMs()) / 1000;
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
     * displays the current time according to the UTC offset, in the GUI on the overlay
     */
    public void setTimeZone(double UTCOffset) {
        clockString.set(TimeUtils.setTimeZone(UTCOffset, race.getCurrentTimeInEpochMs()));
    }


    public void fpsLabel(Boolean visible){fpsLabel.setVisible(visible);}

    /**
     * Causes the starters overlay to hide itself, enabling a proper view of the course and boats beneath
     */
    public void hideStarterOverlay(){
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

    public static double getCanvasHeight() {return canvasHeight;}

    public static double getCanvasWidth() {return canvasWidth;}

    public ObservableList<String> getFormattedDisplayOrder(){
        return formattedDisplayOrder;
    }

    public boolean hasRaceStatusChanged() {
        return raceStatusChanged;
    }

    public void setRaceStatusChanged(boolean raceStatusChanged) {
        this.raceStatusChanged = raceStatusChanged;
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
            }
        }
    }

    public void setUserHelpLabel(String helper){
        lblUserHelp.setOpacity(0);
        lblUserHelp.setPrefWidth(canvasWidth);
        lblUserHelp.setMaxWidth(canvasWidth);
        lblUserHelp.setMinWidth(canvasWidth);
        lblUserHelp.setText(helper);
        DisplayUtils.fadeInFadeOutNodeTransition(lblUserHelp, 1);
    }

    public static double getAnchorHeight() {
        return anchorHeight;
    }

    public static double getAnchorWidth() {
        return anchorWidth;
    }

    public void setZoomSliderValue(int level){
        zoomSlider.setValue(level);
    }

    @FXML private void zoomCursorHover(){
        DisplayUtils.fadeNodeTransition(zoomSlider, FOCUSED_ZOOMSLIDER_OPACITY);
    }

    @FXML private void zoomCursorExitHover(){
        DisplayUtils.fadeNodeTransition(zoomSlider, IDLE_ZOOMSLIDER_OPACITY);
    }
}