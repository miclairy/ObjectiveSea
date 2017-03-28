package seng302;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

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
    private Pane raceClockPane;
    @FXML
    private Label raceClockLabel;

    public static SimpleStringProperty fpsString = new SimpleStringProperty();
    public static SimpleStringProperty clockString = new SimpleStringProperty();
    private static final long[] frameTimes = new long[100];
    private static int frameTimeIndex = 0 ;
    private static boolean arrayFilled = false ;
    private static double secondsElapsed = 0;
    private static double totalRaceTime;
    private static double secondsBeforeRace;

    private static ObservableList<String> formattedDisplayOrder = observableArrayList();
    private static CartesianPoint canvasSize;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        placings.setItems(formattedDisplayOrder);
        canvasAnchor.widthProperty().addListener((observable, oldValue, newValue) -> canvasSize.setX((double) newValue));
        canvasAnchor.heightProperty().addListener((observable, oldValue, newValue) -> canvasSize.setY((double) newValue));
        canvasSize = new CartesianPoint(canvas.getWidth(), canvas.getHeight());

        Race race = Main.getRace();
        Course course = race.getCourse();
        course.initCourseLatLon();
        race.setTotalRaceTime();

        DisplayUtils.setMaxMinLatLon(course.getMinLat(), course.getMinLon(), course.getMaxLat(), course.getMaxLon());
        Display display = new Display(root, race);
        fpsString.set("60.0");
        fpsLabel.textProperty().bind(fpsString);
        totalRaceTime = race.getTotalRaceTime();
        secondsBeforeRace = race.getSecondsBeforeRace();
        secondsElapsed -= secondsBeforeRace;
        raceClockLabel.textProperty().bind(clockString);

        display.start();
    }

    public static void updatePlacings(){
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
    public static void updateFPSCounter(long now) {
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

    public static void updateRaceClock(double now) {
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

    public static CartesianPoint getCanvasSize() {
        return canvasSize;
    }
}