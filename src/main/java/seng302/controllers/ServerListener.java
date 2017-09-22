package seng302.controllers;

import seng302.data.AC35StreamMessage;
import seng302.data.BoatAction;
import seng302.data.CourseName;
import seng302.data.Receiver;
import seng302.data.registration.RegistrationType;
import seng302.models.Boat;
import seng302.models.PolarTable;
import seng302.models.Race;
import seng302.utilities.ConnectionUtils;
import seng302.utilities.PolarReader;
import seng302.views.AvailableRace;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static seng302.data.AC35StreamField.*;
import static seng302.data.registration.RegistrationType.REQUEST_RUNNING_GAMES;

/**
 * Created by mjt169 on 19/07/17.
 *
 */
public class ServerListener extends Receiver implements Runnable{

    private Race race;
    private Integer clientId;
    private boolean clientConnected = true;

    public ServerListener(Socket socket) throws IOException {
        setSocket(socket);
    }

    /**
     * The main run method of the serverListener. Continuously loops listening on a socket and then decoding it
     * and calling the needed method. Deals with client registration
     */
    @Override
    public void run() {
        BufferedInputStream socketData = connectToSocket();

        while(clientConnected){
            try {
                DataInput dataInput = new DataInputStream(socketData);
                byte[] header = new byte[HEADER_LENGTH];
                dataInput.readFully(header);
                System.out.println("reading in join packet");

                int messageLength = byteArrayRangeToInt(header, MESSAGE_LENGTH.getStartIndex(), MESSAGE_LENGTH.getEndIndex());
                int messageTypeValue = byteArrayRangeToInt(header, MESSAGE_TYPE.getStartIndex(), MESSAGE_TYPE.getEndIndex());
                int sourceId = byteArrayRangeToInt(header, HEADER_SOURCE_ID.getStartIndex(), HEADER_SOURCE_ID.getEndIndex());
                AC35StreamMessage messageType = AC35StreamMessage.fromInteger(messageTypeValue);

                byte[] body = new byte[messageLength];
                dataInput.readFully(body);
                byte[] crc = new byte[CRC_LENGTH];
                dataInput.readFully(crc);
                if (checkCRC(header, body, crc)) {
                    switch (messageType) {
                        case HOST_GAME_MESSAGE:
                            recordHostGameMessage(body);
                            break;
                        case GAME_CANCEL:
                            removeHostedGame(body);
                            break;
                        case REGISTRATION_REQUEST:
                            parseRegistrationRequestMessage(body);
                            break;
                        case BOAT_ACTION_MESSAGE:
                            if (sourceId != -1) {
                                parseBoatActionMessage(body);
                            }
                            break;
                    }
                }
            } catch (SocketException e) {
                break;
            } catch (EOFException e) {
                //No client input
            }catch (IOException e) {
                clientConnected = false;
            }
        }
    }

    private BufferedInputStream connectToSocket() {
        BufferedInputStream socketData = null;
        try{
            socketData = new BufferedInputStream(socket.getInputStream());
            socketData.mark(10);
            int sync1 = socketData.read();
            int sync2 = socketData.read();
            socketData.reset();
            if (sync1 != 0x47 || sync2 != 0x83) {
                connectToWebSocket(socketData);
            } else {
                System.out.println("Server: Accepted Connection");
            }
        } catch(IOException e){
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return socketData;
    }

    private void connectToWebSocket(BufferedInputStream socketData) throws NoSuchAlgorithmException, IOException {
        String data = new Scanner(socketData, "UTF-8").useDelimiter("\\r\\n\\r\\n").next();
        Matcher get = Pattern.compile("^GET").matcher(data);
        System.out.println("Server: Accepted websocket Connection");
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

    private void recordHostGameMessage(byte[] body){
        AvailableRace race = createAvailableRace(body);
        race.setPacket(body);
        setChanged();
        notifyObservers(race);
    }

    /**
     * parses body of the a registration request message by extracting request type and notifying
     * @param body the body of a RegistrationRequest message
     */
    private void parseRegistrationRequestMessage(byte[] body) {
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
    private AvailableRace createAvailableRace(byte[] body){
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
    private void parseBoatActionMessage(byte[] body){
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
    private void removeHostedGame(byte[] body){
        System.out.println("VmServer: Received remove game message");
        long serverIpLong = byteArrayRangeToLong(body, HOST_GAME_IP.getStartIndex(), HOST_GAME_IP.getEndIndex());
        String serverIP = ConnectionUtils.ipLongToString(serverIpLong);
        setChanged();
        notifyObservers(serverIP);
    }
}
