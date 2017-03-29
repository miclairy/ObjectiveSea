package seng302;

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
import javafx.scene.image.Image;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

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

    public static SimpleStringProperty fpsString = new SimpleStringProperty();
    public static SimpleStringProperty raceTimerString = new SimpleStringProperty();
    public static SimpleStringProperty clockString = new SimpleStringProperty();
    private static final long[] frameTimes = new long[100];
    private static int frameTimeIndex = 0 ;
    private static boolean arrayFilled = false ;
    private static double secondsElapsed = 0;
    private static double totalRaceTime;
    private static double secondsBeforeRace;
    private static boolean incorrectTimeZone = true;
    private static String foundId = new String();

    private static ObservableList<String> formattedDisplayOrder = observableArrayList();
    private static CartesianPoint canvasSize;
    private Display display;
    private static String timeZone;

    private boolean raceBegun;
    private final int PREP_SIGNAL_SECONDS_BEFORE_START = 120; //2 minutes

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        placings.setItems(formattedDisplayOrder);
        canvasSize = new CartesianPoint(canvas.getWidth(), canvas.getHeight());

        Race race = Main.getRace();
        raceBegun = false;
        Course course = race.getCourse();
        timeZone = race.getCourse().getTimeZone();
        course.initCourseLatLon();
        race.setTotalRaceTime();

        DisplayUtils.setMaxMinLatLon(course.getMinLat(), course.getMinLon(), course.getMaxLat(), course.getMaxLon());
        display = new Display(root, race, this);

        canvasAnchor.widthProperty().addListener((observable, oldValue, newValue) -> {
            canvasSize.setX((double) newValue);
            display.redrawCourse();
            display.redrawWindArrow();
            display.redrawBoatPaths();
        });

        canvasAnchor.heightProperty().addListener((observable, oldValue, newValue) -> {
            canvasSize.setY((double) newValue);
            display.redrawCourse();
            display.redrawWindArrow();
            display.redrawBoatPaths();
        });

        setAnnotations();
        fpsString.set("60.0");
        fpsLabel.textProperty().bind(fpsString);
        totalRaceTime = race.getTotalRaceTime();
        secondsBeforeRace = race.getSecondsBeforeRace();
        secondsElapsed -= secondsBeforeRace;
        raceTimerLabel.textProperty().bind(raceTimerString);

        displayStarters();
        display.start();
    }

    private void setAnnotations() {
        annotationsSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                display.changeAnnotations(newValue.intValue());
            }
        });
        annotationsSlider.adjustValue(annotationsSlider.getMax());
    }

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

    public void displayStarters(){
        ObservableList<String> starters = observableArrayList();
        for (Boat boat : Main.getRace().getCompetitors()){
            starters.add(String.format("%s - %s", boat.getNickName(), boat.getName()));
        }
        startersList.setItems(starters);
        //generateFrostedCourse();
    }

//    public void generateFrostedCourse(){
//        Image courseSnapshot = root.snapshot(null, null);
//        imvCourseOverlay.setImage(courseSnapshot);
//        imvCourseOverlay.setEffect(new GaussianBlur(40));
//        imvCourseOverlay.toBack();
//    }

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

    public void updateRaceClock(double now) {
        secondsElapsed += now;
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

    public static void setTimeZone() {
        String defaultTimeZone = TimeZone.getDefault().getID();

        try {
            for (String id : TimeZone.getAvailableIDs()) {

                if (id.matches("(?i).*?" + timeZone + ".*")) {
                    foundId = id;
                    incorrectTimeZone = false;
                    break;
                }
            }
            if (incorrectTimeZone) {
                throw new Exception("Incorrect TimeZone in XML file. TimeZone reset to default.");
            }
        } catch (Exception e) {
            foundId = defaultTimeZone;
            incorrectTimeZone = false;
            System.out.println(e.getMessage());
        } finally {
            Instant instant = Instant.now();
            ZoneId zone = ZoneId.of(foundId);
            ZonedDateTime zonedDateTime = instant.atZone(zone);
            int hours = zonedDateTime.getHour();
            int minutes = zonedDateTime.getMinute();
            int seconds = zonedDateTime.getSecond();
            clockString.set(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }
    }


    @FXML
    /**
     * Called from the GUI when the fpsToggle checkbox is clicked. Updates visibility of fpsLabel.
     */
    private void fpsToggle(){
        fpsLabel.setVisible(fpsToggle.isSelected());
    }

    public static CartesianPoint getCanvasSize() {
        return canvasSize;
    }

    public void hideStarterOverlay(){
        startersOverlay.setVisible(false);
    }

    public void handlePrerace(double currentTime, double raceStartTime){
        double overlayFadeTime = (raceStartTime - PREP_SIGNAL_SECONDS_BEFORE_START);
        if (currentTime > overlayFadeTime && startersOverlay.isVisible()) {
            hideStarterOverlay();
            display.initializeBoats();
        }
        if (currentTime >= raceStartTime) {
            raceBegun = true;
        }
    }

    public boolean hasRaceBegun() {
        return raceBegun;
    }
}