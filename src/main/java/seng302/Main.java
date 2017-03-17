package seng302;

/**
 * Created by cba62 on 15/03/17.
 */
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class Main extends Application {

    private static ArrayList<Boat> boatsInRace;


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource("main_window.fxml"));
        DisplayUtils displayUtils = new DisplayUtils();
        displayUtils.setScreenSize(0.75);
        primaryStage.setTitle("Sail Fast");
        primaryStage.setScene(new Scene(parent, displayUtils.getWidthHeight().get(0), displayUtils.getWidthHeight().get(1)));
        primaryStage.setMaximized(false);
        primaryStage.setMinHeight(700);
        primaryStage.setMinWidth(1000);

        String name = "America's Cup Race";
        boatsInRace = RaceVisionFileReader.importStarters();
        Course course = RaceVisionFileReader.importCourse();

        course.getCourseSize();
        displayUtils.setMaxMinLatLon(course.getBoundaries());

        // This is an example
        /**
        ArrayList<Double> tester;
        tester = displayUtils.convertFromLatLon(32.295783, -64.855621);
        double x = tester.get(0);
        double y = tester.get(1);
        System.out.printf("X Coord = %f     Y Coord = %f\n", x, y);
        */

        Race race = new Race(name, course, boatsInRace);


        Group root = new Group();
        Canvas canvas = new Canvas(displayUtils.getWidthHeight().get(0), displayUtils.getWidthHeight().get(1));
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        Display display = new Display(root, race);
        display.start();
        primaryStage.setScene(new Scene(root, Color.web("#aae7df")));
        primaryStage.show();
    }


    public static void main( String[] args )
    {
        Config.initializeConfig();
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

