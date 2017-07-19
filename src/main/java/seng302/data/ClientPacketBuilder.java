package seng302.data;

import static seng302.data.AC35StreamField.REGISTRATION_REQUEST_TYPE;
import static seng302.data.AC35StreamMessage.REGISTRATION_REQUEST;

/**
 * Created by mjt169 on 19/07/17.
 */
public class ClientPacketBuilder extends PacketBuilder {

    public byte[] createRegistrationRequestPacket(boolean participate){
        byte[] header = createHeader(REGISTRATION_REQUEST);
        byte[] body = new byte[REGISTRATION_REQUEST.getLength()];
        addFieldToByteArray(body, REGISTRATION_REQUEST_TYPE, participate ? 1 : 0);
        return generatePacket(header, body);
    }

}
