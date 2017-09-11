package seng302.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Sends data out on the client side
 */
public class ClientSender {

    private Socket connection;
    private Socket vmConnection;

    public ClientSender(Socket socket) {
        this.connection = socket;
        try {
            this.vmConnection = new Socket("132.181.14.110", 2828);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToServer(byte[] packet) {
        sendPacket(packet, connection);
    }

    public void sendToVM(byte[] packet){
        sendPacket(packet, vmConnection);
    }

    private void sendPacket(byte[] packet, Socket socket){
        try {
            DataOutputStream clientOutput = new DataOutputStream(socket.getOutputStream());
            clientOutput.write(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
