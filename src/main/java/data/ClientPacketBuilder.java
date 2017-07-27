package src.main.java.data;

/**
 * Builds packets specific to the client
 */
public class ClientPacketBuilder extends PacketBuilder {

    /**
     * Builds a byte array for the registration request message.
     * @param participate 1 if client wants to compete in the race. 0 if client just wants to observe
     * @return the registration request byte array
     */
    public byte[] createRegistrationRequestPacket(boolean participate){
        byte[] header = createHeader(AC35StreamMessage.REGISTRATION_REQUEST);
        byte[] body = new byte[AC35StreamMessage.REGISTRATION_REQUEST.getLength()];
        addFieldToByteArray(body, AC35StreamField.REGISTRATION_REQUEST_TYPE, participate ? 1 : 0);
        return generatePacket(header, body);
    }

    public byte[] createBoatCommandPacket(int commandInt, int clientId) {
        byte[] header = createHeader(AC35StreamMessage.BOAT_ACTION_MESSAGE, clientId);
        byte[] body = new byte[5];
        addFieldToByteArray(body, AC35StreamField.BOAT_ACTION_SOURCE_ID, clientId);
        addFieldToByteArray(body, AC35StreamField.BOAT_ACTION_BODY, commandInt);
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
        addFieldToByteArray(header, AC35StreamField.HEADER_SOURCE_ID, sourceId);
        return header;
    }
}
