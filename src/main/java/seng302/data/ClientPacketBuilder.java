package seng302.data;

import java.io.IOException;
import java.time.Instant;

import static seng302.data.AC35StreamField.*;
import static seng302.data.AC35StreamField.MESSAGE_LENGTH;
import static seng302.data.AC35StreamMessage.BOAT_ACTION_MESSAGE;
import static seng302.data.AC35StreamMessage.REGISTRATION_REQUEST;

/**
 * Created by mjt169 on 19/07/17.
 *
 */
public class ClientPacketBuilder extends PacketBuilder {

    public byte[] createRegistrationRequestPacket(boolean participate){
        byte[] header = createHeader(REGISTRATION_REQUEST);
        byte[] body = new byte[REGISTRATION_REQUEST.getLength()];
        addFieldToByteArray(body, REGISTRATION_REQUEST_TYPE, participate ? 1 : 0);
        return generatePacket(header, body);
    }

    public byte[] createBoatCommandPacket(int commandInt, int clientId) {
        byte[] header = createHeader(BOAT_ACTION_MESSAGE, clientId);
        byte[] body = new byte[1];
        body[0] = (byte) commandInt;
        return generatePacket(header, body);
    }

    /**
     * creates a header byte array which has 2 snyc bytes, a type, timestamp, source id which is an identifier for who
     * is sending the message
     * message length if it is not variable.
     * @param type the integer type of the message
     * @return a byte array of the header
     */
    protected byte[] createHeader(AC35StreamMessage type, int sourceId) {
        byte[] header = super.createHeader(type);
        addFieldToByteArray(header, HEADER_SOURCE_ID, sourceId);
        return header;
    }
}
