package seng302.controllers.listeners;


import seng302.controllers.listeners.AbstractServerListener;
import seng302.data.AC35StreamMessage;
import seng302.data.CourseName;
import seng302.utilities.ConnectionUtils;
import seng302.views.AvailableRace;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static seng302.data.AC35StreamField.*;
import static seng302.data.AC35StreamField.HOST_GAME_CURRENT_PLAYERS;

/**
 * Created by atc60 on 22/09/17.
 */
public class ServerListener extends AbstractServerListener {

    private DataInput dataInput;

    public ServerListener(Socket socket, BufferedInputStream socketData) throws IOException {
        setSocket(socket);
        dataInput = new DataInputStream(socketData);
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
                    }
                } else{
                    System.out.println("Incorrect CRC");
                }
            } catch (SocketException e) {
                break;
            }catch (IOException e) {
                System.out.println("Server Listener: connection closed");
                clientConnected = false;
            }
        }
        System.out.println("ServerListener Stopped");
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
        boolean isPartyGame = true; //TODO this is for you Ray
        return new AvailableRace(CourseName.getCourseNameFromInt(courseIndex).getText(), gameCurrentPlayers, serverPort, serverIP, isPartyGame);
    }

    /**
     * notifes observes to remove a race by its IP address
     * @param body body of the packet of the game to remove
     */
    private void removeHostedGame(byte[] body){
        System.out.println("GameRecorder: Received remove game message");
        long serverIpLong = byteArrayRangeToLong(body, HOST_GAME_IP.getStartIndex(), HOST_GAME_IP.getEndIndex());
        String serverIP = ConnectionUtils.ipLongToString(serverIpLong);
        int port = byteArrayRangeToInt(body, HOST_GAME_PORT.getStartIndex(), HOST_GAME_PORT.getEndIndex());
        AvailableRace raceToRemove = new AvailableRace("", 0, port, serverIP, false);
        raceToRemove.setDeleted(true);
        setChanged();
        notifyObservers(raceToRemove);
    }

}
