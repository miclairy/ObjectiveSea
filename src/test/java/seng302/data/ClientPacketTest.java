package seng302.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static seng302.data.AC35StreamField.BOAT_ACTION_BODY;
import static seng302.data.AC35StreamField.HEADER_SOURCE_ID;
import static seng302.data.Receiver.byteArrayRangeToInt;

/**
 * Created by lga50 on 20/07/17.
 */
public class ClientPacketTest {

    private ClientPacketBuilder clientPacketBuilder;


    public byte[] fillByteArray(byte[] fullPacket, byte[] packetPart, int startIndex, int endIndex){
        int packetIndex = 0;
        for (int i = startIndex; i <= endIndex; i++){
            packetPart[packetIndex] = fullPacket[i];
            packetIndex ++;
        }
        return packetPart;
    }


    @Test
    public void testClientActionPacket(){
        clientPacketBuilder = new ClientPacketBuilder();
        byte[] packet = clientPacketBuilder.createBoatCommandPacket(6, 101);
        byte[] header = new byte[15];
        byte[] body = new byte[1];

        header = fillByteArray(packet, header, 0, 14);
        body = fillByteArray(packet, header, 15, 16);

        int action = byteArrayRangeToInt(body, BOAT_ACTION_BODY.getStartIndex(), BOAT_ACTION_BODY.getEndIndex());
        assertEquals(6, action);

        int sourceId = byteArrayRangeToInt(body, HEADER_SOURCE_ID.getStartIndex(), HEADER_SOURCE_ID.getEndIndex());
        assertEquals(101, sourceId);

    }
}
