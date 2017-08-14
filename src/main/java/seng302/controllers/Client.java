package seng302.controllers;

import seng302.data.BoatStatus;
import seng302.data.ClientPacketBuilder;
import seng302.data.ClientSender;
import seng302.data.DataStreamReader;
import seng302.models.Boat;
import seng302.models.Race;
import seng302.utilities.NoConnectionToServerException;

import java.io.IOException;
import java.util.*;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by mjt169 on 18/07/17.
 *
 */
public class Client implements Runnable, Observer {

    private static Race race;
    private DataStreamReader dataStreamReader;
    private ClientPacketBuilder packetBuilder;
    private ClientSender sender;
    private Map<Integer, Boat> potentialCompetitors;
    private UserInputController userInputController;
    private int clientID;
    private String sourceAddress;
    private int sourcePort;
    private boolean isParticipant;
    Thread dataStreamReaderThread;

    private int connectionAttempts = 0;
    private int MAX_CONNECTION_ATTEMPTS = 200;

    public Client(String ip, int port, boolean isParticipant) throws NoConnectionToServerException {
        this.sourcePort = port;
        this.sourceAddress = ip;
        this.packetBuilder = new ClientPacketBuilder();
        this.isParticipant = isParticipant;
        setUpDataStreamReader();
        System.out.println("Client: Waiting for connection to Server");
        manageWaitingConnection();
        System.out.println("Client: Connected to Server");
        this.sender = new ClientSender(dataStreamReader.getClientSocket());
    }

    private void manageWaitingConnection() throws NoConnectionToServerException {
        while(dataStreamReader.getClientSocket() == null) {
            if(connectionAttempts < MAX_CONNECTION_ATTEMPTS){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connectionAttempts ++;
            }else{
                stopDataStreamReader();
                throw new NoConnectionToServerException("Maximum connection attempts exceeded while trying to connect to server. Port or IP may not be valid.");
            }
        }
    }

    private void setUpDataStreamReader(){
        this.dataStreamReader = new DataStreamReader(sourceAddress, sourcePort);
        dataStreamReaderThread = new Thread(dataStreamReader);
        dataStreamReaderThread.setName("DataStreamReader");
        dataStreamReaderThread.start();
        dataStreamReader.addObserver(this);
    }

    private void stopDataStreamReader(){
        if(dataStreamReaderThread != null){
            dataStreamReaderThread.stop();
            this.dataStreamReader = null;
            System.out.println("Client: Server not found");
        }
    }

    @Override
    public void run() {
        sender.sendToServer(this.packetBuilder.createRegistrationRequestPacket(isParticipant));
        System.out.println("Client: Sent Registration Request");
        waitForRace();
    }

    /**
     * Waits for the race to be able to be read in
     */
    public void waitForRace(){
        while(dataStreamReader.getRace() == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        race = dataStreamReader.getRace();
    }

    /**
     * observing UserInputController and dataStreamReader
     * @param o
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o == dataStreamReader) {
            if (arg instanceof ArrayList) {
                ArrayList<Boat> boats = (ArrayList<Boat>) arg;
                potentialCompetitors = new HashMap<>();
                for (Boat boat : boats) {
                    potentialCompetitors.put(boat.getId(), boat);
                }
            } else if (arg instanceof Race) {
                Race newRace = (Race) arg;
                Race oldRace = dataStreamReader.getRace();
                for (int newId : newRace.getCompetitorIds()) {
                    if (!oldRace.getCompetitorIds().contains(newId)) {
                        oldRace.addCompetitor(potentialCompetitors.get(newId));
                    }
                }
            } else if (arg instanceof Integer){
                this.clientID = (Integer) arg;
            }
        } else if (o == userInputController){
            byte[] boatCommandPacket = packetBuilder.createBoatCommandPacket(userInputController.getCommandInt(), this.clientID);
            sender.sendToServer(boatCommandPacket);
        }
    }

    public void setUserInputController(UserInputController userInputController) {
        this.userInputController = userInputController;
        userInputController.setClientID(clientID);
    }

    public static Race getRace() {
        return race;
    }

    public int getClientID() {
        return clientID;
    }

    public void initiateClientDisconnect() throws IOException {
        dataStreamReader.disconnectClient();
        race.getBoatById(clientID).setStatus(BoatStatus.DNF);
    }

    public boolean isParticipant() {
        return isParticipant;
    }
}
