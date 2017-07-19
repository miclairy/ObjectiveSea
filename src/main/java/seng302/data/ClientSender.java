package seng302.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by mjt169 on 19/07/17.
 */
public class ClientSender {

    private Socket connection;

    public ClientSender(Socket socket) {
        this.connection = socket;
    }

    public void sendToServer(byte[] packet) {
        try {
            DataOutputStream clientOutput = new DataOutputStream(connection.getOutputStream());
            clientOutput.write(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
