package seng302;

import org.junit.*;
import seng302.controllers.MockStream;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;



public class MockStreamTest {


    @Test
    public void checkUpstreamIsSending(){

        try {
            ServerSocket recieveSocket = new ServerSocket(2828);
            MockStream mockStream = new MockStream(2828);
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

    @Test
    public void sendRaceXmlTest(){
        try {
            ServerSocket recieveSocket = new ServerSocket(2829);
            MockStream mockStream = new MockStream(2829);
            Thread upStream = new Thread(mockStream);
            upStream.start();
            Socket connectionSocket = recieveSocket.accept();
            InputStream stream = connectionSocket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(stream);
            byte[] header = new byte[15];
            dataInputStream.readFully(header);
            byte[] body = new byte[5035];
            dataInputStream.readFully(body);
            FileWriter outputFileWriter = new FileWriter("testRace.xml");

            BufferedWriter bufferedWriter = new BufferedWriter(outputFileWriter);
            bufferedWriter.write(new String(Arrays.copyOfRange(body, 14, body.length)));
            bufferedWriter.close();

            String receivedStrPath = new File("testRace.xml").getAbsolutePath();
            Path receivedPath = Paths.get(receivedStrPath);
            List<String> receivedContent = Files.readAllLines(receivedPath);

            String raceStrPath = new File("src/main/resources/defaultFiles/race.xml").getAbsolutePath();
            Path racePath = Paths.get(raceStrPath);
            List<String> raceBodyContent = Files.readAllLines(racePath);
            for (int i = 0; i < receivedContent.size(); i++){
                assertEquals(receivedContent.get(i).trim(), raceBodyContent.get(i).trim());
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testMessageLengthXML(){
        try {
            ServerSocket recieveSocket = new ServerSocket(2827);
            MockStream mockStream = new MockStream(2827);
            Thread upStream = new Thread(mockStream);
            upStream.start();
            Socket connectionSocket = recieveSocket.accept();
            InputStream stream = connectionSocket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(stream);
            byte[] header = new byte[15];
            dataInputStream.readFully(header);
            int length = ((header[14] & 0xFF) << 8) + (header[13] & 0xFF);
            assertEquals(5035, length);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
