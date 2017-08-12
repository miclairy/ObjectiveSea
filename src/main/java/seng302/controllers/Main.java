package seng302.controllers;

/**
 * Main class. Sets up server and client and starts GUI.
 */
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.geometry.Rectangle2D;
import javafx.stage.WindowEvent;
import javafx.event.EventHandler;
import javafx.application.Platform;
import seng302.data.registration.ServerFullException;
import seng302.utilities.Config;
import seng302.utilities.ConnectionUtils;
import seng302.utilities.DisplaySwitcher;
import seng302.utilities.NoConnectionToServerException;
import java.io.IOException;


public class Main extends Application {
    private static Client client;
    private static Server server;
    private static Stage primaryStage;
    private DisplaySwitcher displaySwitcher;

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

    public void loadRaceView(boolean isHost, boolean isParticipant) {
        displaySwitcher.loadRaceView(isHost);
        if (isParticipant) {
            UserInputController userInputController = new UserInputController(DisplaySwitcher.getScene(), Client.getRace());
            client.setUserInputController(userInputController);
            userInputController.addObserver(client);
        }
    }

    public void startHostedRace(String course, int port) throws Exception{
        Config.initializeConfig();
        setupServer(course, port);
        startClient("localhost", port, true);
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
            showServerConnectionError();
            return false;
        } catch (ServerFullException e) {
            showServerJoinError(isParticipant);
            return false;
        }
        return true;
    }

    /**
     * Shows a popup informing user that they were unable to connect to the server
     */
    private static void showServerConnectionError(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add("style/menuStyle.css");
        dialogPane.getStyleClass().add("myDialog");
        alert.setTitle("Cannot Connect to Server");
        alert.setHeaderText("Cannot Connect to Server");
        alert.setContentText("This server may not be running.\n\n" +
                "Please ensure that the IP and Port numbers \n" +
                "you have entered are correct.");
        alert.showAndWait();
    }

    /**
     * Shows a popup informing user that they were not allowed to join the server
     * If they attempted to join as a participant, suggests they try joining as a spectator
     * @param isParticipant whether or not an attempt was made to participate in the race
     */
    private void showServerJoinError(boolean isParticipant) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add("style/menuStyle.css");
        dialogPane.getStyleClass().add("myDialog");
        alert.setTitle("Failed to Join Server");
        alert.setHeaderText("Failed to Join Server");
        String message = "There was not a free slot for you to join the server.\n\n";
        if (isParticipant) message += "You may be able to join as a spectator instead.";
        alert.setContentText(message);
        alert.showAndWait();
    }
}

