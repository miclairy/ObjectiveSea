package seng302.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static seng302.data.AC35StreamField.BOAT_ACTION_BODY;
import static seng302.data.AC35StreamField.BOAT_ACTION_SOURCE_ID;
import static seng302.data.AC35StreamField.HEADER_SOURCE_ID;
import static seng302.data.AC35StreamMessage.BOAT_ACTION_MESSAGE;
import static seng302.data.Receiver.byteArrayRangeToInt;

/**
 * Created by dhl25 on 20/07/17.
 */
public class ClientPacketTest {

    private ClientPacketBuilder clientPacketBuilder = new ClientPacketBuilder();
    private final int HEADER_LENGTH = 15;

    @Test
    public void testClientActionPacket(){
        byte[] packet = clientPacketBuilder.createBoatCommandPacket(6, 101);
        byte[] body = new byte[BOAT_ACTION_MESSAGE.getLength()];

        System.arraycopy(packet, HEADER_LENGTH, body, 0, BOAT_ACTION_MESSAGE.getLength());

        int action = byteArrayRangeToInt(body, BOAT_ACTION_BODY.getStartIndex(), BOAT_ACTION_BODY.getEndIndex());
        assertEquals(6, action);

        int sourceId = byteArrayRangeToInt(body, BOAT_ACTION_SOURCE_ID.getStartIndex(), BOAT_ACTION_SOURCE_ID.getEndIndex());
        assertEquals(101, sourceId);
    }

    @Test
    public void testClientActionPacketIsIgnored(){
        byte[] packet = clientPacketBuilder.createBoatCommandPacket(12, 101);
        byte[] body = new byte[BOAT_ACTION_MESSAGE.getLength()];
        System.arraycopy(packet, HEADER_LENGTH, body, 0, BOAT_ACTION_MESSAGE.getLength());

        int action = byteArrayRangeToInt(body, BOAT_ACTION_BODY.getStartIndex(), BOAT_ACTION_BODY.getEndIndex());
        assertEquals(12, action);
    }
}
