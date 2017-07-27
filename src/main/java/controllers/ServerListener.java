package src.main.java.controllers;

import src.main.java.data.AC35StreamMessage;
import src.main.java.data.BoatAction;
import src.main.java.data.Receiver;
import src.main.java.models.Boat;
import src.main.java.models.PolarTable;
import src.main.java.models.Race;
import src.main.java.utilities.PolarReader;
import src.main.java.data.AC35StreamField;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by mjt169 on 19/07/17.
 *
 */
public class ServerListener extends Receiver implements Runnable{
    
    private Socket socket;
    private Race race;
    private Integer clientId;

    ServerListener(Socket socket){
        this.socket = socket;
    }

    /**
     * The main run method of the serverListener. Continuously loops listening on a socket and then decoding it
     * and calling the needed method. Deals with client registration
     */
    @Override
    public void run() {
        while(true){
            try {
                DataInput dataInput = new DataInputStream(socket.getInputStream());

                byte[] header = new byte[HEADER_LENGTH];
                dataInput.readFully(header);
                int messageLength = byteArrayRangeToInt(header, AC35StreamField.MESSAGE_LENGTH.getStartIndex(), AC35StreamField.MESSAGE_LENGTH.getEndIndex());
                int messageTypeValue = byteArrayRangeToInt(header, AC35StreamField.MESSAGE_TYPE.getStartIndex(), AC35StreamField.MESSAGE_TYPE.getEndIndex());
                int sourceId = byteArrayRangeToInt(header, AC35StreamField.HEADER_SOURCE_ID.getStartIndex(), AC35StreamField.HEADER_SOURCE_ID.getEndIndex());
                AC35StreamMessage messageType = AC35StreamMessage.fromInteger(messageTypeValue);

                byte[] body = new byte[messageLength];
                dataInput.readFully(body);
                byte[] crc = new byte[CRC_LENGTH];
                dataInput.readFully(crc);
                if (checkCRC(header, body, crc)) {
                    switch (messageType) {
                        case REGISTRATION_REQUEST:
                            parseRegistrationRequestMessage(body);
                        case BOAT_ACTION_MESSAGE:
                            if (sourceId != -1) {
                                parseBoatActionMessage(body);
                            }
                    }
                }
            } catch (SocketException e) {
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseRegistrationRequestMessage(byte[] body) {
        System.out.println("Server: Received Registration Request");
        Integer registrationType = byteArrayRangeToInt(body, AC35StreamField.REGISTRATION_REQUEST_TYPE.getStartIndex(), AC35StreamField.REGISTRATION_REQUEST_TYPE.getEndIndex());
        setChanged();
        notifyObservers(registrationType);
    }

    /**
     * parses body of the boat action message that is incoming from the client.
     * @param body currently a single number that corresponds to a control from the client
     */
    private void parseBoatActionMessage(byte[] body){
        int sourceId = byteArrayRangeToInt(body, AC35StreamField.BOAT_ACTION_SOURCE_ID.getStartIndex(), AC35StreamField.BOAT_ACTION_SOURCE_ID.getEndIndex());
        if(sourceId != clientId){
            System.out.printf("Incorrect Client Id Received: Expected: %d Actual: %d\n", clientId, sourceId);
            return;
        }
        int action = byteArrayRangeToInt(body, AC35StreamField.BOAT_ACTION_BODY.getStartIndex(), AC35StreamField.BOAT_ACTION_BODY.getEndIndex());
        Boat boat = race.getBoatById(sourceId);
        PolarTable polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
        BoatAction boatAction = BoatAction.getBoatActionFromInt(action);
        switch (boatAction){
            case BOAT_VMG:
                boat.VMG(race.getCourse(), polarTable);
                break;
            case SAILS_IN:
                boat.changeSails();
                break;
            case SAILS_OUT:
                boat.changeSails();
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
