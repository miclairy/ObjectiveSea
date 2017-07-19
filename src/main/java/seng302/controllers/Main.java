package seng302.controllers;

/**
 * Main class. Loads data and starts GUI.
 */
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.geometry.Rectangle2D;
import javafx.stage.WindowEvent;
import javafx.event.EventHandler;
import javafx.application.Platform;
import seng302.data.DataStreamReader;
import seng302.utilities.Config;
import seng302.models.Race;

import java.io.IOException;


public class Main extends Application {

    private static Race race;
    /**
     *
     */
    private static DataStreamReader dataStreamReader;


    @Override
    public void start(Stage primaryStage) throws Exception {
        Config.initializeConfig();
        setupServer();
        setUpDataStreamReader();
        waitForRace();

        Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource("main_window.fxml"));
        primaryStage.setTitle("Race Vision");
        primaryStage.getIcons().add(new Image("graphics/icon.png"));
        primaryStage.setScene(new Scene(parent));
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setHeight(primaryScreenBounds.getHeight());
        primaryStage.setWidth(primaryScreenBounds.getWidth());
        notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    /**
     * Waits for the race to be able to be read in
     */
    public void waitForRace(){
        while(dataStreamReader.getRace() == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        race = dataStreamReader.getRace();
    }

    public static void main( String[] args ) {launch(args); }

    /**
     * Creates a MockStream object, puts it in it's own thread and starts the thread
     */
    private static void setupServer() throws IOException {
        RaceUpdater runner = new RaceUpdater();
        runner.setScaleFactor(Config.MOCK_SPEED_SCALE);
        Thread runnerThread = new Thread(runner);
        runnerThread.start();
        Server server;
        server = new Server(2828, runner);
        server.setScaleFactor(Config.MOCK_SPEED_SCALE);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    private static void setUpDataStreamReader(){
        dataStreamReader = new DataStreamReader(Config.SOURCE_ADDRESS, Config.SOURCE_PORT);
        Thread dataStreamReaderThread = new Thread(dataStreamReader);
        dataStreamReaderThread.start();
        Client client = new Client(dataStreamReader);

    }

    public static Race getRace() {
        return Main.race;
    }

}

