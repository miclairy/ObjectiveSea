package seng302.controllers;

import seng302.data.AC35StreamMessage;
import seng302.data.BoatAction;
import seng302.data.Receiver;
import seng302.data.registration.RegistrationType;
import seng302.models.Boat;
import seng302.models.PolarTable;
import seng302.models.Race;
import seng302.utilities.ConnectionUtils;
import seng302.utilities.PolarReader;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import static seng302.data.AC35StreamField.*;

/**
 * Created by mjt169 on 19/07/17.
 *
 */
public class ServerListener extends Receiver implements Runnable{
    
    private Socket socket;
    private Race race;
    private Integer clientId;
    private boolean clientConnected = true;

    public ServerListener(Socket socket){
        this.socket = socket;
    }

    /**
     * The main run method of the serverListener. Continuously loops listening on a socket and then decoding it
     * and calling the needed method. Deals with client registration
     */
    @Override
    public void run() {
        while(clientConnected){
            try {
                DataInput dataInput = new DataInputStream(socket.getInputStream());

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
                            parseHostGameMessage(body);
                            break;
                        case REGISTRATION_REQUEST:
                            parseRegistrationRequestMessage(body);
                            break;
                        case BOAT_ACTION_MESSAGE:
                            if (sourceId != -1) {
                                parseBoatActionMessage(body);
                            }
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

    private void parseHostGameMessage(byte[] body){
        System.out.println("Server: Recording on VM");
        long ipAddress = byteArrayRangeToLong(body, HOST_GAME_IP.getStartIndex(), HOST_GAME_IP.getEndIndex());
        String stringIpAddress = ConnectionUtils.ipLongToString(ipAddress);
    }

    /**
     * parses body of the a registration request message by extracting request type and notifying
     * @param body the body of a RegistrationRequest message
     */
    private void parseRegistrationRequestMessage(byte[] body) {
        System.out.println("Server: Received Registration Request");
        byte registrationByte = body[REGISTRATION_REQUEST_TYPE.getStartIndex()];
        setChanged();
        notifyObservers(RegistrationType.getTypeFromByte(registrationByte));
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

    public Socket getSocket() {
        return socket;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }
}
