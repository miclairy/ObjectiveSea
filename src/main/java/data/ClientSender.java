package src.main.java.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Sends data out on the client side
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
