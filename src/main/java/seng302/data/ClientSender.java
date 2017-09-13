package seng302.data;

import seng302.utilities.ConnectionUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Sends data out on the client side
 */
public class ClientSender {

    private Socket connection;
    private Socket vmConnection;

    public ClientSender(Socket socket) {
        this.connection = socket;
        try {
            this.vmConnection = new Socket(ConnectionUtils.getVmIpAddress(), ConnectionUtils.getVmPort());
        } catch (ConnectException e) {
            System.out.println("Client: Unable to reach VM server");
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
        } catch (NullPointerException e){
            System.out.println("Client: Unable to reach VM server");
        }
    }

}
