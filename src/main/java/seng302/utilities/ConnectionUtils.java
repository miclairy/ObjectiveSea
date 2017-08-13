package seng302.utilities;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import seng302.controllers.Client;
import seng302.controllers.Server;

import java.io.IOException;
import java.util.Optional;

public class ConnectionUtils {
    private static Client client;
    private static Server server;
    private static final String IP_REGEX = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";


    /**
     * checks a given IP to make sure it is valid
     * @param IP the IP to be checked
     * @return a boolean of whether it is valid or not
     */
    public static Boolean IPRegExMatcher(String IP){
        if(IP.matches(IP_REGEX) || IP.equals("localhost")){
            return true;
        }
        return false;
    }

    /**
     * checks to determine whether the port number is a valid regex. Colors the port number accordingly.
     * @return whether the port is valid or not
     */
    public static boolean validatePort(int port){
        if(port > 1024 && port < 65536){
            return true;
        }
        return false;
    }

    /**
     * shows a popup informing user that connection to the server failed
     */
    public static void showServerError(){
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

    public static Client getClient() {
        return client;
    }

    public static void setClient(Client client) {
        ConnectionUtils.client = client;
    }

    public static void setServer(Server server) {
        ConnectionUtils.server = server;
    }

    public static void initiateDisconnect(boolean isHost) throws IOException {
        client.initiateClientDisconnect();
        if(isHost){
            server.initiateServerDisconnect();
        }
    }
}
