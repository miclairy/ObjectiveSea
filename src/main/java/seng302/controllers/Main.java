package seng302.controllers;

/**
 * Main class. Loads data and starts GUI.
 */
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.geometry.Rectangle2D;
import seng302.data.DataStreamReader;
import seng302.data.MockStream;
import seng302.utilities.Config;
import seng302.models.Race;
import seng302.utilities.PolarReader;


public class Main extends Application {

    private static Race race;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource("main_window.fxml"));
        primaryStage.setTitle("Race Vision");
        primaryStage.getIcons().add(new Image("graphics/icon.png"));
        primaryStage.setScene(new Scene(parent));
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setHeight(primaryScreenBounds.getHeight());
        primaryStage.setWidth(primaryScreenBounds.getWidth());
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
    private static void setupMockStream() {
        MockRaceRunner runner = new MockRaceRunner();
        runner.setScaleFactor(Config.MOCK_SPEED_SCALE);
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

