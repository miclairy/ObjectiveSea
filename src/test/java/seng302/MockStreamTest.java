package seng302;

import org.junit.Assert;
import org.junit.Test;
import seng302.controllers.MockStream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import static junit.framework.TestCase.assertEquals;


/**
 * Created by sjh298 on 11/04/17.
 */
public class MockStreamTest {

    @Test
    public void checkUpstreamIsSending(){


        try {
            DatagramSocket recieveSocket = new DatagramSocket(5462);
            MockStream mockStream = new MockStream();
            mockStream.run();

            byte[] receiveData = new byte[1024];
            DatagramPacket received = new DatagramPacket(receiveData, receiveData.length);;
            recieveSocket.receive(received);

            assertEquals("Heeelllloooo",  new String(received.getData()).trim());

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }




    }
}
