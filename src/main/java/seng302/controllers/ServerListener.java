package seng302.controllers;

import seng302.data.AC35StreamMessage;
import seng302.data.BoatAction;
import seng302.data.Receiver;
import seng302.models.Boat;
import seng302.models.PolarTable;
import seng302.models.Race;
import seng302.utilities.PolarReader;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import static seng302.data.AC35StreamField.*;

/**
 * Created by mjt169 on 19/07/17.
 */
public class ServerListener extends Receiver implements Runnable{
    
    private Socket socket;
    private Race race;

    ServerListener(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        while(true){
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
                        case REGISTRATION_REQUEST:
                            parseRegistrationRequestMessage(body);
                        case BOAT_ACTION_MESSAGE:
                            if (sourceId != -1) {
                                parseBoatActionMessage(body, sourceId);
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
        Integer registrationType = byteArrayRangeToInt(body, REGISTRATION_REQUEST_TYPE.getStartIndex(), REGISTRATION_REQUEST_TYPE.getEndIndex());
        setChanged();
        notifyObservers(registrationType);
    }

    /**
     * parses body of the boat action message that is incoming from the client.
     * @param body currently a single number that corresponds to a control from the client
     */
    private void parseBoatActionMessage(byte[] body, int sourceId){
        int action = byteArrayRangeToInt(body, BOAT_ACTION_BODY.getStartIndex(), BOAT_ACTION_BODY.getEndIndex());
        Boat boat = race.getBoatById(sourceId); // Assuming this field has been set and can be used to distinguish a boat
        //for now we assume all boats racing are AC35 class yachts such that we can use the polars we have for them
        PolarTable polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
        BoatAction boatAction = BoatAction.getBoatActionFromInt(action);
        //System.out.println("Boat doing a " + action);
        switch (boatAction){
            case BOAT_VMG:
                boat.VMG(race.getCourse(), polarTable);
                break;
            case SAILS_IN:
                boat.sailsIn();
                break;
            case SAILS_OUT:
                boat.sailsOut();
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
}
