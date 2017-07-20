package seng302.controllers;

import seng302.data.*;
import seng302.models.Boat;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import static seng302.data.AC35StreamMessage.BOAT_ACTION_MESSAGE;
import static seng302.data.AC35StreamXMLMessage.BOAT_XML_MESSAGE;
import static seng302.data.AC35StreamXMLMessage.RACE_XML_MESSAGE;
import static seng302.data.AC35StreamXMLMessage.REGATTA_XML_MESSAGE;

/**
 * Created by mjt169 on 18/07/17.
 */
public class Server implements Runnable, Observer {

    private final double SECONDS_PER_UPDATE = 0.2;
    private double scaleFactor = 1;

    private int nextViewerID = 0;

    private Map<AC35StreamXMLMessage, Integer> xmlSequenceNumber = new HashMap<>();
    private Map<Boat, Integer> boatSequenceNumbers = new HashMap<>();
    private Map<Boat, Integer> lastMarkRoundingSent = new HashMap<>();

    private RaceUpdater raceUpdater;
    private ConnectionManager connectionManager;
    private ServerPacketBuilder packetBuilder;

    public Server(int port, RaceUpdater raceUpdater) throws IOException {
        this.raceUpdater = raceUpdater;
        this.packetBuilder = new ServerPacketBuilder();
        this.connectionManager = new ConnectionManager(port);
        this.connectionManager.addObserver(this);
    }


    /**
     *
     * @throws IOException
     */
    private void initialize() throws IOException  {

        xmlSequenceNumber.put(REGATTA_XML_MESSAGE, 0);
        xmlSequenceNumber.put(RACE_XML_MESSAGE, 0);
        xmlSequenceNumber.put(BOAT_XML_MESSAGE, 0);

        //testing
//        raceUpdater.addCompetitor();
//        raceUpdater.addCompetitor();
//        raceUpdater.addCompetitor();
//        raceUpdater.addCompetitor();

        for (Boat boat: raceUpdater.getRace().getCompetitors()){
            boatSequenceNumbers.put(boat, boat.getId());
            lastMarkRoundingSent.put(boat, -1);
        }
    }
    /**
     * Sends all the data to the socket while the boats have not all finished.
     */
    @Override
    public void run() {
        try {
            initialize();
            sendInitialRaceMessages();
            Thread managerThread = new Thread(connectionManager);
            managerThread.start();
            while (!raceUpdater.raceHasEnded()) {
                sendRaceUpdates();
                try {
                    Thread.sleep((long)(SECONDS_PER_UPDATE * 1000 / scaleFactor));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            sendRaceUpdates(); //send one last message block with ending data

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a header, body then generates and sends a CRC for that header and body
     * @param packet the packet to send
     * @throws IOException
     */
    private void sendPacket(byte[] packet) throws IOException {
        connectionManager.sendToClients(packet);
    }

    /**
     * Exit out of run
     */
    public void stop(){
        raceUpdater.getRace().updateRaceStatus(RaceStatus.FINISHED);
    }

    /**
     * Sends the XML messages when the client has connected
     */
    private void sendInitialRaceMessages() {
        sendXmlMessage(RACE_XML_MESSAGE, "Race.xml");
        sendXmlMessage(BOAT_XML_MESSAGE, "Boat.xml");
        sendXmlMessage(REGATTA_XML_MESSAGE, "Regatta.xml");
    }

    /**
     * Sends Race Status, Boat Location and Mark Rounding messages that are currently necessary
     * @throws IOException
     */
    private void sendRaceUpdates() throws IOException {
        sendPacket(packetBuilder.createRaceUpdateMessage(raceUpdater.getRace()));
        sendBoatMessagesForAllBoats();
    }

    private void sendBoatMessagesForAllBoats() throws IOException {
        for (Boat boat : raceUpdater.getRace().getCompetitors()) {
            if (!boat.isFinished()) {
                sendBoatMessages(boat);
            }
        }
    }

    /**
     * Sends a Boat Location Message and possible a Mark Rounding message for a boat
     * @param boat the boat to send messages for
     * @throws IOException
     */
    private void sendBoatMessages(Boat boat) throws IOException {
        int currentSequenceNumber = boatSequenceNumbers.get(boat);
        boatSequenceNumbers.put(boat, currentSequenceNumber + 1); //increment sequence number

        sendPacket(packetBuilder.createBoatLocationMessage(boat, raceUpdater.getRace(), currentSequenceNumber));
        if (lastMarkRoundingSent.get(boat) != boat.getLastRoundedMarkIndex()){
            lastMarkRoundingSent.put(boat, boat.getLastRoundedMarkIndex());
            sendPacket(packetBuilder.createMarkRoundingMessage(boat, raceUpdater.getRace()));
        }
    }

    /**
     * Sends an xml message type to the socket including the header, body and CRC
     * @param type subtype of the xml message
     * @param fileName name of the file to send
     */
    private void sendXmlMessage(AC35StreamXMLMessage type, String fileName){
        int sequenceNo = xmlSequenceNumber.get(type) + 1; //increment sequence number
        xmlSequenceNumber.put(type, sequenceNo);
        byte[] packet = packetBuilder.buildXmlMessage(type, fileName, sequenceNo, raceUpdater.getRace());
        connectionManager.setXmlMessage(type, packet);
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o.equals(this.connectionManager)) {
            Socket socket = (Socket) arg;
            ServerListener serverListener = new ServerListener(socket);
            serverListener.setRace(raceUpdater.getRace());
            Thread serverListenerThread = new Thread(serverListener);
            serverListenerThread.start();
            serverListener.addObserver(this);
        } else if(o instanceof ServerListener){
            ServerListener serverListener = (ServerListener) o;
            Integer registrationType = (Integer) arg;
            if(registrationType == 1){
                int newId = this.raceUpdater.addCompetitor();
                Boat boat = this.raceUpdater.getRace().getBoatById(newId);
                boatSequenceNumbers.put(boat, newId);
                lastMarkRoundingSent.put(boat, -1);
                connectionManager.addConnection(newId, serverListener.getSocket());

                byte[] packet = packetBuilder.createRegistrationAcceptancePacket(newId);
                connectionManager.sendToClient(newId, packet);
            } else{
                connectionManager.addConnection(nextViewerID, serverListener.getSocket());
                nextViewerID++;
            }
        }
    }
}
