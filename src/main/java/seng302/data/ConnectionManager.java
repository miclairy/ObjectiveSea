package seng302.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.TreeMap;

/**
 * Created by Gemma Lamont on 10/07/17.
 * Class to manage multiple players
 */
public class ConnectionManager extends Observable implements Runnable {

    private ServerSocket serverSocket;
    private HashMap<Integer, Socket> clients =  new HashMap<>();
    private TreeMap<AC35StreamXMLMessage, byte[]> xmlMessages = new TreeMap<>();


    public ConnectionManager(int port) throws IOException {;
        serverSocket = new ServerSocket(port);
    }

    /**
     * Keeps listening for new client connections and send xml data on connection
     */
    @Override
    public void run() {
        Socket socket = null;
        while (true) {
            try {
                socket = serverSocket.accept();
                DataOutputStream clientOutput = new DataOutputStream(socket.getOutputStream());
                System.out.println("Server: Accepted Connection");
                setChanged();
                notifyObservers(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends byte array information to multiple players
     * @param packet the information packet to be sent
     * @throws IOException
     */
    public void sendToClients(byte[] packet) throws IOException {
        for(int id: clients.keySet()){
            sendToClient(id, packet);
        }
    }

    /**
     * Send a packet to a single client.
     * If the client is no longer connected, remove it from our map of players
     * @param id the id of the client to send to
     * @param packet the packet to send
     */
    public void sendToClient(int id, byte[] packet) {
        try{
            DataOutputStream clientOutput = new DataOutputStream(clients.get(id).getOutputStream());
            clientOutput.write(packet);
        } catch (java.net.SocketException e){
            clients.remove(id);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Sets the xml packets to be send directly after connection is made
     * @param messageType the type of message
     * @param xmlMessage the byte array to be sent
     */
    public void setXmlMessage(AC35StreamXMLMessage messageType, byte[] xmlMessage){
        xmlMessages.put(messageType,xmlMessage);
        try {
            sendToClients(xmlMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addConnection(int newId, Socket socket) {
        clients.put(newId, socket);
        sendAllXMLsToClient(newId);
    }

    private void sendAllXMLsToClient(int id) {
        for (byte[] xmlMessage : xmlMessages.values()) {
            sendToClient(id, xmlMessage);
        }
    }
}
