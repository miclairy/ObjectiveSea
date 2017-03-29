package seng302;

/**
 * Main class. Loads data and starts GUI.
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import javafx.scene.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class Main extends Application {

    private static Race race;

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
        String name = "America's Cup Race";
        race = new Race(name, course, boatsInRace);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource("main_window.fxml"));
        primaryStage.setTitle("Race Vision");
        primaryStage.getIcons().add(new Image("graphics/icon.png"));
        primaryStage.setScene(new Scene(parent));
        primaryStage.setMaximized(true);
        primaryStage.setMinHeight(700);
        primaryStage.setMinWidth(1000);
        primaryStage.show();
    }

    public static void main( String[] args )
    {
        launch(args);
    }

    public static Race getRace() {
        return Main.race;
    }
}

