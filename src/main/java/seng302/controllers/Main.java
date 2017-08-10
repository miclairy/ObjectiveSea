package seng302.controllers;

/**
 * Main class. Sets up server and client and starts GUI.
 */
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
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
import seng302.utilities.ConnectionUtils;
import seng302.utilities.DisplaySwitcher;
import seng302.utilities.NoConnectionToServerException;
import seng302.utilities.GameSounds;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main extends Application {
    private static Client client;
    private static Server server;
    private static Stage primaryStage;
    private DisplaySwitcher displaySwitcher;
    private GameSounds gameSounds = new GameSounds();


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Objective Sea");
        this.primaryStage.getIcons().add(new Image("graphics/icon.png"));
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        this.primaryStage.setHeight(primaryScreenBounds.getHeight());
        this.primaryStage.setWidth(primaryScreenBounds.getWidth());
        displaySwitcher = new DisplaySwitcher(this, primaryStage);
        loadMainMenu();
        notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
        this.primaryStage.show();
        this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public static void main( String[] args ) {launch(args); }

    /**
     * Initializes the client on it's own thread.
     */
    private static void setupClient(int port) {
        try {
            client = new Client("localhost", port, true);
            ConnectionUtils.setClient(client);
            Thread clientThread = new Thread(client);
            clientThread.setName("Client");
            clientThread.start();
        } catch (NoConnectionToServerException e) {
            ConnectionUtils.showServerError();
        }

    }

    /**
     * Creates a Server object, puts it in it's own thread and starts the thread
     */
    private static void setupServer(String course, int port) throws IOException {
        RaceUpdater runner = new RaceUpdater(course);
        runner.setScaleFactor(Config.MOCK_SPEED_SCALE);
        Thread runnerThread = new Thread(runner);
        runnerThread.setName("Race Updater");
        runnerThread.start();
        server = new Server(port, runner, course);
        server.setScaleFactor(Config.MOCK_SPEED_SCALE);
        ConnectionUtils.setServer(server);
        Thread serverThread = new Thread(server);
        serverThread.setName("Server");
        serverThread.start();
    }

    public static Client getClient() {
        return client;
    }

    public void loadMainMenu() {
        displaySwitcher.loadMainMenu();
    }

    public void loadRaceView(boolean isHost) {
        displaySwitcher.loadRaceView(isHost);
        UserInputController userInputController = new UserInputController(DisplaySwitcher.getScene(), Client.getRace());
        client.setUserInputController(userInputController);
        userInputController.addObserver(client);
    }

    public void startHostedRace(String course, int port) throws Exception{
        Config.initializeConfig();
        setupServer(course, port);
        setupClient(port);
    }

    /**
     * starts the client at the desired ip and port number
     * ensures that the client connects
     * throws error if connection fails
     * @param ip the ip for the client to connect to
     * @param port the port of the client to connect to
     * @param isParticipant whether the user is a participant or spectator
     * @return whether the connection was successful or times out
     */
    public boolean startClient(String ip, int port, boolean isParticipant){
        try {
            client = new Client(ip, port, isParticipant);
            ConnectionUtils.setClient(client);
            Thread clientThread = new Thread(client);
            clientThread.setName("Client");
            clientThread.start();
        } catch (NoConnectionToServerException e) {
            ConnectionUtils.showServerError();
            return false;
        }
        return true;
    }
}

