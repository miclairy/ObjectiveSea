package seng302.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import seng302.utilities.DisplayUtils;
import seng302.models.Boat;
import seng302.models.Course;
import seng302.models.Race;
import seng302.utilities.TimeUtils;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static javafx.collections.FXCollections.observableArrayList;

public class Controller implements Initializable {

    @FXML
    public Canvas canvas;
    @FXML
    private Group root;
    @FXML
    private AnchorPane canvasAnchor;
    @FXML
    private Label fpsLabel;
    @FXML
    private ListView<String> startersList;
    @FXML
    private Label clockLabel;
    @FXML
    public VBox startersOverlay;
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
    private static double secondsBeforeRace;

    private static ObservableList<String> formattedDisplayOrder = observableArrayList();
    private static double canvasHeight;
    private static double canvasWidth;
    private static String timeZone;

    // Controllers
    @FXML
    private RaceViewController raceViewController;
    @FXML
    private ScoreBoardController scoreBoardController = new ScoreBoardController();

    public boolean raceBegun;
    private double secondsElapsed = 0;
    private Race race;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        race = Main.getRace();
        raceBegun = false;
        Course course = race.getCourse();
        timeZone = race.getCourse().getTimeZone();
        course.initCourseLatLon();
        race.setTotalRaceTime();

        DisplayUtils.setMaxMinLatLon(course.getMinLat(), course.getMinLon(), course.getMaxLat(), course.getMaxLon());
        raceViewController = new RaceViewController(root, race, this, scoreBoardController);

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


        scoreBoardController.setControllers(this, raceViewController, race);
        scoreBoardController.setUp();

        fpsString.set("..."); //set to "..." while fps count loads
        fpsLabel.textProperty().bind(fpsString);
        totalRaceTime = race.getTotalRaceTime();
        secondsBeforeRace = race.getSecondsBeforeRace();
        secondsElapsed -= secondsBeforeRace;
        clockLabel.textProperty().bind(clockString);

        setWindDirection();
        startersOverlay.toFront();
        displayStarters();
        raceViewController.start();
    }

    /**
     * Sets the wind direction image to the correct rotation and position
     */
    private void setWindDirection(){
        double windDirection = race.getCourse().getWindDirection();
        windDirectionImage.setX(canvasWidth - WIND_ARROW_OFFSET);
        windDirectionImage.setRotate(windDirection);
        raceViewController.setCurrentWindArrow(windDirectionImage);
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
        ArrayList<Boat> raceOrder = Main.getRace().getRaceOrder();
        Collections.sort(raceOrder);
        formattedDisplayOrder.clear();
        for (int i = 0; i < raceOrder.size(); i++){
            Boat boat = raceOrder.get(i);
            String displayString = String.format("%d : %s (%s) - ", i+1, boat.getName(), boat.getNickName());
            if(raceOrder.get(i).isFinished()){
                displayString += "Finished!";
            } else{
                displayString += boat.getSpeed() + " knots";
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
     * displays the current tie zone in the GUI on the overlay
     */
    public static void setTimeZone() {
        clockString.set(TimeUtils.setTimeZone(timeZone));
    }


    public void fpsLabel(Boolean visible){
        fpsLabel.setVisible(visible);
    }

    /**
     * Causes the starters overlay to hide itself, enabling a proper view of the course and boats beneath
     */
    public void hideStarterOverlay(){
        startersOverlay.setVisible(false);
    }

    public boolean hasRaceBegun() {
        return raceBegun;
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

    public ObservableList<String> getFormattedDisplayOrder(){
        return formattedDisplayOrder;
    }
}