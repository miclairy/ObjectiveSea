package seng302;

import com.sun.corba.se.spi.activation.Server;
import org.junit.Assert;
import org.junit.Test;
import seng302.controllers.MockStream;
import seng302.controllers.RaceViewController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

import static junit.framework.TestCase.assertEquals;



public class MockStreamTest {

    @Test
    public void checkUpstreamIsSending(){

        try {
            ServerSocket recieveSocket = new ServerSocket(2828);
            MockStream mockStream = null;
            mockStream = new MockStream();
            Thread upStream = new Thread(mockStream);
            upStream.start();
            Socket connectionSocket = recieveSocket.accept();

            assertEquals(71, connectionSocket.getInputStream().read());

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    @Test
//    public void printOutRaceData() throws UnknownHostException {
//
//        try {
//            Socket recieveSocket = new Socket("livedata.americascup.com", 4941);
//            while(true){
//
//                System.out.println(recieveSocket.getInputStream().read());
//            }
//        } catch (SocketException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
