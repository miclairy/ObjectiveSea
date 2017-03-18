package seng302;


import javafx.collections.ListChangeListener;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

import static javafx.collections.FXCollections.observableArrayList;

public class Controller implements Initializable {

    @FXML
    private Canvas canvas;
    @FXML
    private ListView<String> placings;
    @FXML
    private Group root;

    private static ObservableList<String> finishers = observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        canvas.setWidth(DisplayUtils.getWidthHeight().get(0));
        canvas.setHeight(DisplayUtils.getWidthHeight().get(1));
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Display display = new Display(root, Main.getRace());
        placings.setItems(finishers);
        display.start();
    }

}