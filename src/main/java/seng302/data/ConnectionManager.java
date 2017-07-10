package seng302.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * Created by Gemma Lamont on 10/07/17.
 * Class to manage multiple clients
 */
public class ConnectionManager implements Runnable {

    private ServerSocket serverSocket;
    private HashSet<Socket> clients =  new HashSet<>();
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
                for(AC35StreamXMLMessage type: xmlMessages.keySet()){
                    clientOutput.write(xmlMessages.get(type));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            clients.add(socket);
        }
    }

    /**
     * Sends byte array information to multiple clients
     * @param byteArray the information packet to be sent
     * @throws IOException
     */
    public void sendToClients(byte[] byteArray) throws IOException {
        for(Socket client: clients){
            try{
                DataOutputStream clientOutput = new DataOutputStream(client.getOutputStream());
                clientOutput.write(byteArray);
            } catch (java.net.SocketException e){
                clients.remove(client);
            }
        }

    }

    /**
     * Sets the xml packets to be send directly after connection is made
     * @param messageType the type of message
     * @param xmlMessage the byte array to be sent
     */
    public void setXmlMessage(AC35StreamXMLMessage messageType, byte[] xmlMessage){
        xmlMessages.put(messageType,xmlMessage);
    }
}
