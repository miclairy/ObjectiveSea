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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

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
    private Label raceClockLabel;
    @FXML
    private Slider annotationsSlider;
    @FXML
    private VBox startersOverlay;
    @FXML
    private ImageView windDirectionImage;

    public static SimpleStringProperty fpsString = new SimpleStringProperty();
    public static SimpleStringProperty clockString = new SimpleStringProperty();
    private static final long[] frameTimes = new long[100];
    private static int frameTimeIndex = 0 ;
    private static boolean arrayFilled = false ;
    private static double secondsElapsed = 0;
    private static double totalRaceTime;
    private static double secondsBeforeRace;

    private static ObservableList<String> formattedDisplayOrder = observableArrayList();
    private static double canvasHeight;
    private static double canvasWidth;
    private Display display;

    private boolean raceBegun;
    private final int PREP_SIGNAL_SECONDS_BEFORE_START = 120; //2 minutes
    //number of from right edge of canvas that the wind arrow will be drawn
    private final int WIND_ARROW_OFFSET = 60;

    private Race race;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        placings.setItems(formattedDisplayOrder);
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        race = Main.getRace();
        raceBegun = false;
        Course course = race.getCourse();
        course.initCourseLatLon();
        race.setTotalRaceTime();

        DisplayUtils.setMaxMinLatLon(course.getMinLat(), course.getMinLon(), course.getMaxLat(), course.getMaxLon());
        display = new Display(root, race, this);

        canvasAnchor.widthProperty().addListener((observable, oldValue, newValue) -> {
            canvasWidth = (double) newValue;
            display.redrawCourse();
            display.redrawWindArrow();
            display.redrawBoatPaths();
        });

        canvasAnchor.heightProperty().addListener((observable, oldValue, newValue) -> {
            canvasHeight = (double) newValue;
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
        raceClockLabel.textProperty().bind(clockString);

        setWindDirection();

        displayStarters();
        display.start();
    }

    private void setWindDirection(){
        double windDirection = race.getCourse().getWindDirection();
        windDirectionImage.setX(canvasWidth - WIND_ARROW_OFFSET);
        windDirectionImage.setRotate(windDirection);
        display.setCurrentWindArrow(windDirectionImage);
    }

    private void setAnnotations() {
        annotationsSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                display.changeAnnotations(newValue.intValue(), false);
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
            clockString.set(String.format("-%02d:%02d:%02d", Math.abs(hours), Math.abs(minutes), Math.abs(seconds)));
        } else {
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

    public static double getCanvasHeight() {
        return canvasHeight;
    }

    public static double getCanvasWidth() {
        return canvasWidth;
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
            for (Boat boat : race.getCompetitors()){
                boat.maximiseSpeed();
            }
            display.changeAnnotations((int) annotationsSlider.getValue(), true);
        }
    }

    public boolean hasRaceBegun() {
        return raceBegun;
    }
}