package seng302.controllers;

/**
 * Main class. Sets up server and client and starts GUI.
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
import seng302.data.ConnectionManager;
import seng302.data.DataStreamReader;
import seng302.utilities.Config;
import seng302.models.Race;

import java.io.IOException;


public class Main extends Application {

    private static Race race;
    private static Scene scene;
    private static DataStreamReader dataStreamReader;
    private static Client client;


    @Override
    public void start(Stage primaryStage) throws Exception {
        Config.initializeConfig();
        setupServer();
        setupClient();

        Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource("main_window.fxml"));
        primaryStage.setTitle("Race Vision");
        primaryStage.getIcons().add(new Image("graphics/icon.png"));
        scene = new Scene(parent);
        primaryStage.setScene(scene);
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
        UserInputController userInputController = new UserInputController(scene);
        client.setUserInputController(userInputController);
        userInputController.addObserver(client);
    }

    public static void main( String[] args ) {launch(args); }

    /**
     * Initializes the client on it's own thread.
     */
    private static void setupClient() {
        Client client = new Client();
        Thread clientThread = new Thread(client);
        clientThread.setName("Client");
        clientThread.start();
    }

    /**
     * Creates a Server object, puts it in it's own thread and starts the thread
     */
    private static void setupServer() throws IOException {
        RaceUpdater runner = new RaceUpdater();
        runner.setScaleFactor(Config.MOCK_SPEED_SCALE);
        Thread runnerThread = new Thread(runner);
        runnerThread.setName("Race Updater");
        runnerThread.start();
        Server server;
        server = new Server(2828, runner);
        server.setScaleFactor(Config.MOCK_SPEED_SCALE);
        Thread serverThread = new Thread(server);
        serverThread.setName("Server");
        serverThread.start();
    }

    public static Race getRace() {
        return Main.race;
    }

}

