package seng302.controllers;

import seng302.data.*;
import seng302.data.registration.RegistrationType;
import seng302.models.Boat;
import seng302.models.PolarTable;
import seng302.models.Race;
import seng302.utilities.ConnectionUtils;
import seng302.utilities.PolarReader;
import seng302.views.AvailableRace;

import java.io.*;
import java.net.Socket;

import static seng302.data.AC35StreamField.*;
import static seng302.data.registration.RegistrationType.REQUEST_RUNNING_GAMES;

/**
 * Created by mjt169 on 19/07/17.
 *
 */
public abstract class AbstractServerListener extends Receiver implements Runnable{

    protected Race race;
    protected Integer clientId;
    protected boolean clientConnected = true;


    /**
     * The main run method of the serverListener. Continuously loops listening on a socket and then decoding it
     * and calling the needed method. Deals with client registration
     */
    @Override
    public abstract void run();

    protected void recordHostGameMessage(byte[] body){
        AvailableRace race = createAvailableRace(body);
        race.setPacket(body);
        setChanged();
        notifyObservers(race);
    }

    /**
     * parses body of the a registration request message by extracting request type and notifying
     * @param body the body of a RegistrationRequest message
     */
    protected void parseRegistrationRequestMessage(byte[] body) {
        byte registrationByte = body[REGISTRATION_REQUEST_TYPE.getStartIndex()];
        if (registrationByte != REQUEST_RUNNING_GAMES.value()){
            System.out.println("Server: Received Registration Request");
        }
        setChanged();
        notifyObservers(RegistrationType.getTypeFromByte(registrationByte));
    }

    /**
     * Method to decode a host game packet from the server
     * @param body body of the hosted game, containing all relevant information about a game
     */
    protected AvailableRace createAvailableRace(byte[] body){
        long serverIpLong = byteArrayRangeToLong(body, HOST_GAME_IP.getStartIndex(), HOST_GAME_IP.getEndIndex());
        String serverIP = ConnectionUtils.ipLongToString(serverIpLong);
        int serverPort = byteArrayRangeToInt(body, HOST_GAME_PORT.getStartIndex(), HOST_GAME_PORT.getEndIndex());
        int courseIndex = byteArrayRangeToInt(body, HOST_GAME_MAP.getStartIndex(), HOST_GAME_MAP.getEndIndex());
        long gameSpeed = byteArrayRangeToLong(body, HOST_GAME_SPEED.getStartIndex(), HOST_GAME_SPEED.getEndIndex());
        int gameStatus = byteArrayRangeToInt(body, HOST_GAME_STATUS.getStartIndex(), HOST_GAME_STATUS.getEndIndex());
        int gameMinPlayers = byteArrayRangeToInt(body, HOST_GAME_REQUIRED_PLAYERS.getStartIndex(), HOST_GAME_REQUIRED_PLAYERS.getEndIndex());
        int gameCurrentPlayers = byteArrayRangeToInt(body, HOST_GAME_CURRENT_PLAYERS.getStartIndex(), HOST_GAME_CURRENT_PLAYERS.getEndIndex());
        return new AvailableRace(CourseName.getCourseNameFromInt(courseIndex).getText(), gameCurrentPlayers, serverPort, serverIP);
    }

    /**
     * parses body of the boat action message that is incoming from the client.
     * @param body currently a single number that corresponds to a control from the client
     */
    protected void parseBoatActionMessage(byte[] body){
        int sourceId = byteArrayRangeToInt(body, BOAT_ACTION_SOURCE_ID.getStartIndex(), BOAT_ACTION_SOURCE_ID.getEndIndex());
        if(sourceId != clientId){
            System.out.printf("Incorrect Client Id Received: Expected: %d Actual: %d\n", clientId, sourceId);
            return;
        }
        int action = byteArrayRangeToInt(body, BOAT_ACTION_BODY.getStartIndex(), BOAT_ACTION_BODY.getEndIndex());
        Boat boat = race.getBoatById(sourceId);
        PolarTable polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
        BoatAction boatAction = BoatAction.getBoatActionFromInt(action);
        switch (boatAction){
            case BOAT_VMG:
                boat.VMG(race.getCourse(), polarTable);
                break;
            case SAILS_IN:
                boat.changeSails();
                boat.setSailsNeedUpdate(true);
                break;
            case SAILS_OUT:
                boat.changeSails();
                boat.setSailsNeedUpdate(true);
                break;
            case TACK_GYBE:
                boat.tackOrGybe(race.getCourse(), polarTable);
                break;
            case UPWIND:
                boat.upWind(race.getCourse().getWindDirection());
                break;
            case DOWNWIND:
                boat.downWind(race.getCourse().getWindDirection());
                break;
            default:
                break;
        }
    }

    @Override
    public Race getRace() {
        return null;
    }

    @Override
    public void disconnectClient() {
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    /**
     * notifes observes to remove a race by its IP address
     * @param body body of the packet of the game to remove
     */
    protected void removeHostedGame(byte[] body){
        System.out.println("GameRecorder: Received remove game message");
        long serverIpLong = byteArrayRangeToLong(body, HOST_GAME_IP.getStartIndex(), HOST_GAME_IP.getEndIndex());
        String serverIP = ConnectionUtils.ipLongToString(serverIpLong);
        int port = byteArrayRangeToInt(body, HOST_GAME_PORT.getStartIndex(), HOST_GAME_PORT.getEndIndex());
        AvailableRace raceToRemove = new AvailableRace("", 0, port, serverIP);
        raceToRemove.setDeleted(true);
        setChanged();
        notifyObservers(raceToRemove);
    }

    public static AbstractServerListener createServerListener(Socket socket){
        BufferedInputStream socketData = null;
        try{
            socketData = new BufferedInputStream(socket.getInputStream());
            socketData.mark(10);
            int sync1 = socketData.read();
            int sync2 = socketData.read();
            socketData.reset();
            if (sync1 != 0x47 || sync2 != 0x83) {
                return new WebSocketServerListener(socket, socketData);
            } else {
                return new ServerListener(socket, socketData);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
