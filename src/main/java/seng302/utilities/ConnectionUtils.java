package seng302.utilities;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;

public class ConnectionUtils {
    private static final String IP_REGEX = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";


    public static Boolean IPRegExMatcher(String IP){
        return IP.matches(IP_REGEX);
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
}
