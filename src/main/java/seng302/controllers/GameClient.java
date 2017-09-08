package seng302.controllers;

import javafx.scene.input.KeyCode;
import seng302.data.BoatAction;
import seng302.data.BoatStatus;
import seng302.data.ClientPacketBuilder;
import seng302.data.ClientSender;
import seng302.data.registration.RegistrationResponse;
import seng302.data.registration.RegistrationType;
import seng302.data.registration.ServerFullException;
import seng302.models.Boat;
import seng302.models.ClientOptions;
import seng302.models.Race;
import seng302.utilities.NoConnectionToServerException;

import java.util.*;

/**
 * Created by lga50 on 7/09/17.
 *
 */
public class GameClient extends Client{

    private static Race race;
    private Map<Integer, Boat> potentialCompetitors;
    private UserInputController userInputController;
    private ClientOptions options;
    private static List<Integer> tutorialKeys = new ArrayList<Integer>();
    private static Runnable tutorialFunction = null;


    public GameClient(ClientOptions options) throws ServerFullException, NoConnectionToServerException {
        this.packetBuilder = new ClientPacketBuilder();
        this.options = options;
        setUpDataStreamReader(options.getServerAddress(), options.getServerPort());
        System.out.println("Client: Waiting for connection to Server");
        manageWaitingConnection();
        RegistrationType regoType = options.isParticipant() ? RegistrationType.PLAYER : RegistrationType.SPECTATOR;
        System.out.println("Client: Connected to Server");
        this.sender = new ClientSender(clientListener.getClientSocket());
        sender.sendToServer(this.packetBuilder.createRegistrationRequestPacket(regoType));
        System.out.println("Client: Sent Registration Request");
        manageServerResponse();
    }

    public void updateVM(Double speedScale, Integer minParticipants, Integer serverPort, String publicIp, int currentCourseIndex){
        byte[] registerGamePacket = this.packetBuilder.createGameRegistrationPacket(speedScale, minParticipants, serverPort, publicIp, currentCourseIndex);
        System.out.println("Client: Updating VM");
        sender.sendToServer(registerGamePacket);
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
