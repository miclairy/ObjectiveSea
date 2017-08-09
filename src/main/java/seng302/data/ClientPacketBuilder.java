package seng302.data;

import static seng302.data.AC35StreamField.*;
import static seng302.data.AC35StreamMessage.BOAT_ACTION_MESSAGE;
import static seng302.data.AC35StreamMessage.REGISTRATION_REQUEST;

/**
 * Builds packets specific to the client
 */
public class ClientPacketBuilder extends PacketBuilder {

    /**
     * Builds a byte array for the registration request message.
     * @param regoType the registration type of the client (currently only SPECTATOR and PLAYER will do anything)
     * @return the registration request byte array
     */
    public byte[] createRegistrationRequestPacket(RegistrationType regoType){
        byte[] header = createHeader(REGISTRATION_REQUEST);
        byte[] body = new byte[REGISTRATION_REQUEST.getLength()];
        addFieldToByteArray(body, REGISTRATION_REQUEST_TYPE, regoType.value());
        return generatePacket(header, body);
    }

    public byte[] createBoatCommandPacket(int commandInt, int clientId) {
        byte[] header = createHeader(BOAT_ACTION_MESSAGE, clientId);
        byte[] body = new byte[5];
        addFieldToByteArray(body, BOAT_ACTION_SOURCE_ID, clientId);
        addFieldToByteArray(body, BOAT_ACTION_BODY, commandInt);
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
