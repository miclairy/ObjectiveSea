package seng302;

/**
 * Created by cba62 on 15/03/17.
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import javafx.scene.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class Main extends Application {

    private static Race race;
    private static DisplayUtils displayUtils;

    @Override
    public void init(){
        Config.initializeConfig();

        String courseFile = getParameters().getNamed().get("course");
        String boatsFile = getParameters().getNamed().get("boats");
        ArrayList<Boat> boatsInRace = RaceVisionFileReader.importStarters(boatsFile);
        Course course = RaceVisionFileReader.importCourse(courseFile);
        //for now if we fail to read in a course or boats, then exit the program immediately
        if (boatsInRace.isEmpty() || course == null) {
            Platform.exit();
        }
        course.initCourseLatLon();

        displayUtils = new DisplayUtils();
        displayUtils.setMaxMinLatLon(course.getMinLat(), course.getMinLon(), course.getMaxLat(), course.getMaxLon());

        String name = "America's Cup Race";
        race = new Race(name, course, boatsInRace);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource("main_window.fxml"));
        displayUtils.setScreenSize(0.75);
        primaryStage.setTitle("Race Vision");
        primaryStage.getIcons().add(new Image("graphics/icon.png"));
        primaryStage.setScene(new Scene(parent, displayUtils.getWidthHeight().get(0), displayUtils.getWidthHeight().get(1)));
        primaryStage.setMaximized(false);
        primaryStage.setMinHeight(700);
        primaryStage.setMinWidth(1000);

        Group root = new Group();
        Canvas canvas = new Canvas(displayUtils.getWidthHeight().get(0), displayUtils.getWidthHeight().get(1));
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        Display display = new Display(root, race);
        root.getStylesheets().add("/style.css");
        display.start();
        primaryStage.setScene(new Scene(root, Color.web("#aae7df")));
        primaryStage.show();
    }

    public static void main (String[] args) {
        launch(args);
    }

    private static void randomizeOrder(ArrayList<Boat> boats){
        int numBoats = boats.size();
        ArrayList<Integer> places = new ArrayList<>();
        for (int i = 1; i <= numBoats; i++){
            places.add(i);
        }
        Collections.shuffle(places, new Random());
        for (int j = 0; j < numBoats; j++) {
            boats.get(j).setFinishingPlace(places.get(j));
        }
    }
}

