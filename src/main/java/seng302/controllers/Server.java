package seng302.controllers;

import seng302.data.*;
import seng302.models.Boat;
import seng302.models.Collision;
import seng302.models.Race;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

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
    private CollisionManager collisionManager;

    public Server(int port, RaceUpdater raceUpdater) throws IOException {
        this.raceUpdater = raceUpdater;
        this.collisionManager = raceUpdater.getCollisionManager();
        this.packetBuilder = new ServerPacketBuilder();
        this.connectionManager = new ConnectionManager(port);
        this.connectionManager.addObserver(this);
    }

    /**
     * Initializes the sequence numbers for the boats and xml messages
     * @throws IOException
     */
    private void initialize() throws IOException  {

        xmlSequenceNumber.put(REGATTA_XML_MESSAGE, 0);
        xmlSequenceNumber.put(RACE_XML_MESSAGE, 0);
        xmlSequenceNumber.put(BOAT_XML_MESSAGE, 0);
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
            managerThread.setName("Connection Manager");
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
        try {
            byte[] raceUpdateMessage = packetBuilder.createRaceUpdateMessage(raceUpdater.getRace());
            sendPacket(raceUpdateMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendBoatMessagesForAllBoats();
        sendYachtEventMessages();
    }

    private void sendYachtEventMessages() throws IOException {
        for (Collision collision : collisionManager.getCollisions()) {
            for (Integer boatId : collision.getInvolvedBoats()) {
                Boat boat = raceUpdater.getRace().getBoatById(boatId);
                sendYachtEventMessage(boat, raceUpdater.getRace(), collision.getIncidentId(), YachtEventCode.COLLISION);
                if (collision.boatIsAtFault(boatId)) {
                    sendYachtEventMessage(boat, raceUpdater.getRace(), collision.getIncidentId(), YachtEventCode.COLLISION_PENALTY);
                }
            }
            collisionManager.removeCollision(collision);
        }
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
        Integer currentSequenceNumber = boatSequenceNumbers.get(boat);
        if (currentSequenceNumber != null) { //check required as a race condition can sometimes cause a NullPointerException
            boatSequenceNumbers.put(boat, currentSequenceNumber + 1);

            sendPacket(packetBuilder.createBoatLocationMessage(boat, raceUpdater.getRace(), currentSequenceNumber));
            if (lastMarkRoundingSent.get(boat) != boat.getLastRoundedMarkIndex()) {
                lastMarkRoundingSent.put(boat, boat.getLastRoundedMarkIndex());
                sendPacket(packetBuilder.createMarkRoundingMessage(boat, raceUpdater.getRace()));
            }
        }
    }

    /**
     * sends a Yacht Event message
     * @param boat
     * @param race
     * @throws IOException
     */
    private void sendYachtEventMessage(Boat boat, Race race, int incidentID, YachtEventCode eventCode) throws IOException {
        sendPacket(packetBuilder.createYachtEventMessage(boat, race, incidentID, eventCode));
    }

    /**
     * Sends an xml message type to the socket including the header, body and CRC
     * @param type subtype of the xml message
     * @param fileName name of the file to send
     */
    private void sendXmlMessage(AC35StreamXMLMessage type, String fileName){
        int sequenceNo = xmlSequenceNumber.get(type) + 1;
        xmlSequenceNumber.put(type, sequenceNo);
        byte[] packet = packetBuilder.buildXmlMessage(type, fileName, sequenceNo, raceUpdater.getRace());
        connectionManager.setXmlMessage(type, packet);
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    /**
     * Starts a new server listener on new thread for which listens to a client
     * @param socket the socket for the client
     */
    private void startServerListener(Socket socket){
        ServerListener serverListener = new ServerListener(socket);
        serverListener.setRace(raceUpdater.getRace());
        Thread serverListenerThread = new Thread(serverListener);
        serverListenerThread.setName("Server Listener");
        serverListenerThread.start();
        serverListener.addObserver(this);
    }

    /**
     * Adds a competing client to the race model and sends new xml messages out to all clients
     * @param serverListener the listener for the client
     */
    private void addClientToRace(ServerListener serverListener){
        int newId = raceUpdater.addCompetitor();
        Boat boat = raceUpdater.getRace().getBoatById(newId);
        boatSequenceNumbers.put(boat, newId);
        lastMarkRoundingSent.put(boat, -1);
        connectionManager.addConnection(newId, serverListener.getSocket());
        serverListener.setClientId(newId);

        byte[] packet = packetBuilder.createRegistrationAcceptancePacket(newId);
        connectionManager.sendToClient(newId, packet);
        sendXmlMessage(RACE_XML_MESSAGE, "Race.xml");
    }

    /**
     * Method that gets called when Server is notified as an observer
     * If the observable is a ConnectionManager then a new client has connected and a server listener
     * is started for the client
     * If the observable is a ServerListener then a registration message is received
     * @param observable The observable either a ConnectionManager or ServerListener
     * @param arg A Socket if observable is a ConnectionManager else it is the registration type of the client
     */
    @Override
    public void update(Observable observable, Object arg) {
        if (observable.equals(connectionManager)) {
            startServerListener((Socket) arg);
        } else if(observable instanceof ServerListener){
            ServerListener serverListener = (ServerListener) observable;
            Integer registrationType = (Integer) arg;
            if(registrationType == 1){
                addClientToRace(serverListener);
            } else{
                connectionManager.addConnection(nextViewerID, serverListener.getSocket());
                nextViewerID++;
            }
        }
    }
}