package seng302.controllers;

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
import seng302.data.DataStreamReader;
import seng302.data.MockStream;
import seng302.utilities.Config;
import seng302.data.RaceVisionFileReader;
import seng302.models.Boat;
import seng302.models.Course;
import seng302.models.Race;
import java.util.List;


public class Main extends Application {

    private static Race race;

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
        Config.initializeConfig();
        setupMockStream();
        setUpDataStreamReader();
        while(race.getCourse() == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        launch(args);
    }

    /**
     * Creates a MockStream object, puts it in it's own thread and starts the thread
     */
    private static void setupMockStream(){
        MockRaceRunner runner = new MockRaceRunner();
        Thread runnerThread = new Thread(runner);
        runnerThread.start();
        MockStream mockStream;
        mockStream = new MockStream(2828, runner);
        Thread upStream = new Thread(mockStream);
        upStream.start();
    }

    private static void setUpDataStreamReader(){
        DataStreamReader dataStreamReader = new DataStreamReader(Config.SOURCE_ADDRESS, Config.SOURCE_PORT);
        Thread dataStreamReaderThread = new Thread(dataStreamReader);
        race = new Race();
        dataStreamReader.setRace(race);
        dataStreamReaderThread.start();

    }

    public static Race getRace() {
        return Main.race;
    }

}

