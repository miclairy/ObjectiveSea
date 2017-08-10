package seng302.controllers;

import seng302.data.ClientPacketBuilder;
import seng302.data.ClientSender;
import seng302.data.DataStreamReader;
import seng302.data.registration.RegistrationResponse;
import seng302.data.registration.RegistrationType;
import seng302.data.registration.ServerFullException;
import seng302.models.Boat;
import seng302.models.Race;
import seng302.utilities.NoConnectionToServerException;
import seng302.utilities.TimeUtils;

import java.util.*;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by mjt169 on 18/07/17.
 *
 */
public class Client implements Runnable, Observer {


    private int MAX_CONNECTION_ATTEMPTS = 200;
    private double CONNECTION_TIMEOUT = TimeUtils.secondsToMilliseconds(10.0);
    private int WAIT_MILLISECONDS = 10;

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
    private RegistrationResponse serverRegistrationResponse;

    public Client(String ip, int port, boolean isParticipant) throws ServerFullException, NoConnectionToServerException {
        this.sourcePort = port;
        this.sourceAddress = ip;
        this.packetBuilder = new ClientPacketBuilder();
        this.isParticipant = isParticipant;
        setUpDataStreamReader();
        System.out.println("Client: Waiting for connection to Server");
        manageWaitingConnection();
        RegistrationType regoType = isParticipant ? RegistrationType.PLAYER : RegistrationType.SPECTATOR;
        System.out.println("Client: Connected to Server");
        this.sender = new ClientSender(dataStreamReader.getClientSocket());
        sender.sendToServer(this.packetBuilder.createRegistrationRequestPacket(regoType));
        System.out.println("Client: Sent Registration Request");
        manageServerResponse();
    }

    private void manageWaitingConnection() throws NoConnectionToServerException {
        int connectionAttempts = 0;
        while(dataStreamReader.getClientSocket() == null) {
            if(connectionAttempts < MAX_CONNECTION_ATTEMPTS){
                try {
                    Thread.sleep(WAIT_MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connectionAttempts++;
            } else {
                stopDataStreamReader();
                throw new NoConnectionToServerException("Maximum connection attempts exceeded while trying to connect to server. Port or IP may not be valid.");
            }
        }
    }

    private void manageServerResponse() throws ServerFullException, NoConnectionToServerException {
        double waitTime = 0;
        while (serverRegistrationResponse == null) {
            try {
                Thread.sleep(WAIT_MILLISECONDS);
                waitTime += WAIT_MILLISECONDS;
                if (waitTime > CONNECTION_TIMEOUT) {
                    throw new NoConnectionToServerException("Connection to server timed out while waiting for registration response.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        switch(serverRegistrationResponse.getStatus()) {
            case PLAYER_SUCCESS:
            case SPECTATOR_SUCCESS:
                this.clientID = serverRegistrationResponse.getSourceId();
                break;
            case OUT_OF_SLOTS:
                throw new ServerFullException();
            case GENERAL_FAILURE:
            case GHOST_SUCCESS:
            case TUTORIAL_SUCCESS:
                System.out.println("Client: Server response not understood.");
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
        System.out.println("Client: Successfully Joined Game");
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
            } else if (arg instanceof RegistrationResponse) {
                serverRegistrationResponse = (RegistrationResponse) arg;
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
}
