package seng302.data;

import javax.xml.bind.DatatypeConverter;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Observable;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.out;

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
                    String data = new Scanner(socket.getInputStream(),"UTF-8").useDelimiter("\\r\\n\\r\\n").next();
                    Matcher get = Pattern.compile("^GET").matcher(data);
                    out.println("Server: Accepted Connection");
                    if (get.find()) {
                        Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                        match.find();
                        byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                                + "Connection: Upgrade\r\n"
                                + "Upgrade: websocket\r\n"
                                + "Sec-WebSocket-Accept: "
                                + DatatypeConverter
                                .printBase64Binary(
                                        MessageDigest.getInstance("SHA-1")
                                                .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                                        .getBytes("UTF-8")))
                                + "\r\n\r\n")
                                .getBytes("UTF-8");

                        socket.getOutputStream().write(response, 0, response.length);
                    }
                }
                setChanged();
                notifyObservers(socket);
            } catch (IOException e) {
                if(e instanceof SocketException){
                    out.println("Server: Disconnected");
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
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
            out.printf("Server: Client %d Disconnected\n", id);
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

    /**
     * Closes and removes the given connection
     * @param connectionID
     */
    public void removeConnection(int connectionID) {
        try {
            Socket socket= clients.get(connectionID);
            socket.close();
            clients.remove(connectionID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
