package seng302;

/**
 * Created by cba62 on 15/03/17.
 */
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class Main extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main_window.fxml"));
            primaryStage.setTitle("Sail Fast");
            primaryStage.setScene(new Scene(root, 1000, 700));
            primaryStage.setMaximized(true);
            primaryStage.setMinHeight(700);
            primaryStage.setMinWidth(1000);
            primaryStage.show();

        }


        public static void main( String[] args )
        {
            launch(args);

            Config.initializeConfig();
            String name = "America's Cup Race";

            ArrayList<Boat> boatsInRace = RaceVisionFileReader.importStarters();
            Course course = RaceVisionFileReader.importCourse();
            Race race = new Race(name, course, boatsInRace);
            Display.displayRace(race);
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

