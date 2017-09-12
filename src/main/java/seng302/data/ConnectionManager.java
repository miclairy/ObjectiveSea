package seng302.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Observable;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

/**
 * Created by Gemma Lamont on 10/07/17.
 * Class to manage multiple players
 */
public class ConnectionManager extends Observable implements Runnable {

    private ServerSocket serverSocket;
    private Map<Integer, Socket> clients =  new ConcurrentHashMap<>();
    private TreeMap<AC35StreamXMLMessage, byte[]> xmlMessages = new TreeMap<>();
    private boolean running = true;
    private boolean isGameServer;


    public ConnectionManager(int port, boolean isGameServer) throws IOException {
        serverSocket = new ServerSocket(port);
        this.isGameServer = isGameServer;
    }

    /**
     * Keeps listening for new client connections and send xml data on connection
     */
    @Override
    public void run() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                if (isGameServer) {
                    System.out.println("Server: Accepted Connection");
                }
                setChanged();
                notifyObservers(socket);
            } catch (IOException e) {
                if(e instanceof SocketException){
                    System.out.println("Server: Disconnected");
                }
            }
        }
    }

    /**
     * Sends byte array information to multiple players
     * @param packet the information packet to be sent
     */
    public void sendToClients(byte[] packet) {
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
            System.out.printf("Server: Client %d Disconnected\n", id);
            setChanged();
            notifyObservers(id);
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
        sendToClients(xmlMessage);
    }

    public void addConnection(int newId, Socket socket) {
        clients.put(newId, socket);
        sendAllXMLsToClient(newId);
    }

    public void addMainMenuConnection(int newId, Socket socket) {
        clients.put(newId, socket);
    }

    /**
     * Closes the server socket and all client connections.
     */
    public void closeAllConnections() {
        running = false;
        try {
            serverSocket.close();
            closeClientConnections();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes all sockets connected to clients.
     */
    public void closeClientConnections() throws IOException {
        for (Socket socket : clients.values()){
            socket.close();
        }
    }

    private void sendAllXMLsToClient(int id) {
        for (byte[] xmlMessage : xmlMessages.values()) {
            sendToClient(id, xmlMessage);
        }
    }
}
