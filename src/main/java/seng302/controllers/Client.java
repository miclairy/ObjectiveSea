package seng302.controllers;

import seng302.data.ClientPacketBuilder;
import seng302.data.ClientSender;
import seng302.data.DataStreamReader;
import seng302.models.Boat;
import seng302.models.Race;
import seng302.utilities.Config;

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
    private static boolean connected = false;
    private String sourceAddress;
    private int sourcePort;
    private boolean isParticipant;

    public Client(String ip, int port, boolean isParticipant) {
        this.sourcePort = port;
        this.sourceAddress = ip;
        this.packetBuilder = new ClientPacketBuilder();
        this.isParticipant = isParticipant;
        setUpDataStreamReader();

        System.out.println("Client: Waiting for connection to Server");
        while(dataStreamReader.getClientSocket() == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Client: Connected to Server");
        connected = true;
        this.sender = new ClientSender(dataStreamReader.getClientSocket());
    }

    public Client() {
        this.sourceAddress = Config.SOURCE_ADDRESS;
        this.sourcePort = Config.SOURCE_PORT;
        this.packetBuilder = new ClientPacketBuilder();
        setUpDataStreamReader();

        System.out.println("Client: Waiting for connection to Server");
        while(dataStreamReader.getClientSocket() == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Client: Connected to Server");
        connected = true;
        this.sender = new ClientSender(dataStreamReader.getClientSocket());
    }

    private void setUpDataStreamReader(){
        this.dataStreamReader = new DataStreamReader(sourceAddress, sourcePort);
        Thread dataStreamReaderThread = new Thread(dataStreamReader);
        dataStreamReaderThread.setName("DataStreamReader");
        dataStreamReaderThread.start();
        dataStreamReader.addObserver(this);
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

    public static boolean isConnected(){
        return connected;
    }
}
