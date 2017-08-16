package seng302.utilities;

import seng302.controllers.Client;
import seng302.controllers.Server;

import java.io.IOException;

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
        return(IP.matches(IP_REGEX) || IP.equals("localhost"));
    }

    /**
     * checks to determine whether the port number is a valid regex. Colors the port number accordingly.
     * @return whether the port is valid or not
     */
    public static boolean validatePort(int port){
        return port > 1024 && port < 65536;
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
