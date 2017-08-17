package seng302.controllers;

import javafx.scene.input.KeyCode;
import seng302.data.BoatAction;
import seng302.data.BoatStatus;
import seng302.data.ClientPacketBuilder;
import seng302.data.ClientSender;
import seng302.data.ClientListener;
import seng302.data.registration.RegistrationResponse;
import seng302.data.registration.RegistrationType;
import seng302.data.registration.ServerFullException;
import seng302.models.Boat;
import seng302.models.ClientOptions;
import seng302.models.Race;
import seng302.utilities.NoConnectionToServerException;
import seng302.utilities.TimeUtils;

import java.io.IOException;
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
    private ClientListener clientListener;
    private ClientPacketBuilder packetBuilder;
    private ClientSender sender;
    private Map<Integer, Boat> potentialCompetitors;
    private UserInputController userInputController;
    private int clientID;
    private ClientOptions options;
    Thread dataStreamReaderThread;
    private RegistrationResponse serverRegistrationResponse;

    private static List<Integer> tutorialKeys = new ArrayList<Integer>();
    private static Runnable tutorialFunction = null;

    public Client(ClientOptions options) throws ServerFullException, NoConnectionToServerException {
        this.packetBuilder = new ClientPacketBuilder();
        this.options = options;
        setUpDataStreamReader();
        System.out.println("Client: Waiting for connection to Server");
        manageWaitingConnection();
        RegistrationType regoType = options.isParticipant() ? RegistrationType.PLAYER : RegistrationType.SPECTATOR;
        System.out.println("Client: Connected to Server");
        this.sender = new ClientSender(clientListener.getClientSocket());
        sender.sendToServer(this.packetBuilder.createRegistrationRequestPacket(regoType));
        System.out.println("Client: Sent Registration Request");
        manageServerResponse();
    }

    /**
     * Waits for the server to accept the socket connection
     * @throws NoConnectionToServerException if we timeout whilst waiting for the connection
     */
    private void manageWaitingConnection() throws NoConnectionToServerException {
        int connectionAttempts = 0;
        while(clientListener.getClientSocket() == null) {
            if(clientListener.isHasConnectionFailed()){
                stopDataStreamReader();
                throw new NoConnectionToServerException(true, "Connection Failed. Port number is invalid.");
            }else if(connectionAttempts < MAX_CONNECTION_ATTEMPTS){
                try {
                    Thread.sleep(WAIT_MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connectionAttempts++;
            } else {
                stopDataStreamReader();
                throw new NoConnectionToServerException(false, "Maximum connection attempts exceeded while trying to connect to server. Port or IP may not be valid.");
            }
        }
    }

    /**
     * Waits for a RegistrationResponse to be received from the server and acts upon that response
     * @throws ServerFullException if the Response from the server was received, but the server was full
     * @throws NoConnectionToServerException if we timeout whilst waiting for the response
     */
    private void manageServerResponse() throws ServerFullException, NoConnectionToServerException {
        double waitTime = 0;
        while (serverRegistrationResponse == null) {
            try {
                Thread.sleep(WAIT_MILLISECONDS);
                waitTime += WAIT_MILLISECONDS;
                if (waitTime > CONNECTION_TIMEOUT) {
                    throw new NoConnectionToServerException(false, "Connection to server timed out while waiting for registration response.");
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
        this.clientListener = new ClientListener(options.getServerAddress(), options.getServerPort());
        dataStreamReaderThread = new Thread(clientListener);
        dataStreamReaderThread.setName("ClientListener");
        dataStreamReaderThread.start();
        clientListener.addObserver(this);
    }

    private void stopDataStreamReader(){
        if(dataStreamReaderThread != null){
            dataStreamReaderThread.stop();
            this.clientListener = null;
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
        while(clientListener.getRace() == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        race = clientListener.getRace();
    }

    /**
     * observing UserInputController and clientListener
     * @param o
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o == clientListener) {
            if (arg instanceof ArrayList) {
                ArrayList<Boat> boats = (ArrayList<Boat>) arg;
                potentialCompetitors = new HashMap<>();
                for (Boat boat : boats) {
                    potentialCompetitors.put(boat.getId(), boat);
                }
            } else if (arg instanceof Race) {
                Race newRace = (Race) arg;
                Race oldRace = clientListener.getRace();
                for (int newId : newRace.getCompetitorIds()) {
                    if (!oldRace.getCompetitorIds().contains(newId)) {
                        oldRace.addCompetitor(potentialCompetitors.get(newId));
                    }
                }
            } else if (arg instanceof RegistrationResponse) {
                serverRegistrationResponse = (RegistrationResponse) arg;
            }
        } else if (o == userInputController){
            sendBoatCommandPacket();
        }
    }

    /**
     * sends boat command packet to server. Sends keypress and runs tutorial callback function if required.
     */
    private void sendBoatCommandPacket(){
        if(tutorialKeys.contains(userInputController.getCommandInt())) {
            tutorialFunction.run();
        }
        byte[] boatCommandPacket = packetBuilder.createBoatCommandPacket(userInputController.getCommandInt(), this.clientID);
        sender.sendToServer(boatCommandPacket);

    }

    public static void setTutorialActions(List<KeyCode> keys, Runnable callbackFunction){
        for(KeyCode key : keys){
            tutorialKeys.add(BoatAction.getTypeFromKeyCode(key));
        }
        tutorialFunction = callbackFunction;
    }

    public static void clearTutorialAction(){
        tutorialKeys.clear();
        tutorialFunction = null;
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

    public void initiateClientDisconnect() {
        clientListener.disconnectClient();
        race.getBoatById(clientID).setStatus(BoatStatus.DNF);
    }
}
