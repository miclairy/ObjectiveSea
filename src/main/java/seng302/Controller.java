package seng302;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;

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
    private Group root;
    @FXML
    private AnchorPane canvasAnchor;

    private static ObservableList<String> formattedDisplayOrder = observableArrayList();

    private static CartesianPoint canvasSize;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        placings.setItems(formattedDisplayOrder);
        canvasAnchor.widthProperty().addListener((observable, oldValue, newValue) -> canvasSize.setX((double) newValue));
        canvasAnchor.heightProperty().addListener((observable, oldValue, newValue) -> canvasSize.setY((double) newValue));
        canvasSize = new CartesianPoint(canvas.getWidth(), canvas.getHeight());
        Course course = Main.getRace().getCourse();
        course.initCourseLatLon();
        DisplayUtils.setMaxMinLatLon(course.getMinLat(), course.getMinLon(), course.getMaxLat(), course.getMaxLon());
        Display display = new Display(root, Main.getRace());
        display.start();

    }

    public static void updatePlacings(){
        ArrayList<Boat> raceOrder = Main.getRace().getRaceOrder();
        Collections.sort(raceOrder);
        formattedDisplayOrder.clear();
        for (int i = 0; i < raceOrder.size(); i++){
            String boatName = raceOrder.get(i).getName();
            formattedDisplayOrder.add(String.format("%d : %s", i+1, boatName));
        }

    }

    public static CartesianPoint getCanvasSize() {
        return canvasSize;
    }
}