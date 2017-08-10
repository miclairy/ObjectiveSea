package seng302.controllers;

/**
 * Main class. Sets up server and client and starts GUI.
 */
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import seng302.utilities.NoConnectionToServerException;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main extends Application {

    private static Scene scene;
    private static Client client;
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Objective Sea");
        this.primaryStage.getIcons().add(new Image("graphics/icon.png"));
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        this.primaryStage.setHeight(primaryScreenBounds.getHeight());
        this.primaryStage.setWidth(primaryScreenBounds.getWidth());
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
    private static void setupServer(int port) throws IOException {
        RaceUpdater runner = new RaceUpdater();
        runner.setScaleFactor(Config.MOCK_SPEED_SCALE);
        Thread runnerThread = new Thread(runner);
        runnerThread.setName("Race Updater");
        runnerThread.start();
        Server server;
        server = new Server(port, runner);
        server.setScaleFactor(Config.MOCK_SPEED_SCALE);
        Thread serverThread = new Thread(server);
        serverThread.setName("Server");
        serverThread.start();
    }

    public static Client getClient() {
        return client;
    }

    private void loadMainMenu() {
        try {
            MainMenuController mainMenu = (MainMenuController) replaceSceneContent("main_menu.fxml");
            mainMenu.setApp(this);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadRaceView(boolean isHost, boolean isParticipant) {
        try {
            Controller race = (Controller) replaceSceneContent("main_window.fxml");
            race.setApp(isHost);
            if (isParticipant) {
                UserInputController userInputController = new UserInputController(scene, Client.getRace());
                client.setUserInputController(userInputController);
                userInputController.addObserver(client);
            }
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * takes an fxml file and replaces the current screen with it
     * @param fxml an FXML file
     * @return a display
     * @throws Exception if can't find FXML
     */
    public Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        URL fxmlLocation = getClass().getClassLoader().getResource(fxml);
        loader.setLocation(fxmlLocation);
        Parent root = loader.load();
        scene = new Scene(root);
        setScene(scene);
        primaryStage.setScene(scene);

        return (Initializable) loader.getController();
    }

    public void startHostedRace(int port) throws Exception{
        Config.initializeConfig();
        setupServer(port);
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

    private void setScene(Scene newScene){
        scene = newScene;
    }
}

