package seng302.data;

import static seng302.data.AC35StreamField.REGISTRATION_REQUEST_TYPE;
import static seng302.data.AC35StreamMessage.REGISTRATION_REQUEST;

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
        byte[] header = createHeader(REGISTRATION_REQUEST);
        byte[] body = new byte[REGISTRATION_REQUEST.getLength()];
        addFieldToByteArray(body, REGISTRATION_REQUEST_TYPE, participate ? 1 : 0);
        return generatePacket(header, body);
    }

}
