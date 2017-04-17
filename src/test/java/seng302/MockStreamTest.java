package seng302;

import org.junit.*;
import seng302.controllers.MockStream;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static junit.framework.TestCase.assertEquals;



public class MockStreamTest {


    @Test
    public void checkUpstreamIsSending(){

        try {
            ServerSocket recieveSocket = new ServerSocket(2827);
            MockStream mockStream = new MockStream(2827);
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
            int length = ((header[14] & 0xFF) << 8) + (header[13] & 0xFF);
            byte[] body = new byte[length];
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
    public void sendBodyXmlTest(){
        try {
            ServerSocket recieveSocket = new ServerSocket(2825);
            MockStream mockStream = new MockStream(2825);
            Thread upStream = new Thread(mockStream);
            upStream.start();
            Socket connectionSocket = recieveSocket.accept();
            InputStream stream = connectionSocket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(stream);

            byte[] header = new byte[15];
            dataInputStream.readFully(header);
            int length = ((header[14] & 0xFF) << 8) + (header[13] & 0xFF);
            dataInputStream.readFully(new byte[length]);
            dataInputStream.readFully(new byte[4]); //read crc

            byte[] boatHeader = new byte[15];
            dataInputStream.readFully(boatHeader);
            int boatLength = ((boatHeader[14] & 0xFF) << 8) + (boatHeader[13] & 0xFF);
            byte[] body = new byte[boatLength];
            dataInputStream.readFully(body);
            FileWriter outputFileWriter = new FileWriter("testBoat.xml");

            BufferedWriter bufferedWriter = new BufferedWriter(outputFileWriter);
            bufferedWriter.write(new String(Arrays.copyOfRange(body, 14, body.length)));
            bufferedWriter.close();

            String receivedStrPath = new File("testBoat.xml").getAbsolutePath();
            Path receivedPath = Paths.get(receivedStrPath);
            List<String> receivedContent = Files.readAllLines(receivedPath);

            String boatStrPath = new File("src/main/resources/defaultFiles/boat.xml").getAbsolutePath();
            Path boatPath = Paths.get(boatStrPath);
            List<String> boatBodyContent = Files.readAllLines(boatPath);
            for (int i = 0; i < receivedContent.size(); i++){
                assertEquals(boatBodyContent.get(i).trim(), receivedContent.get(i).trim());
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void sendBoatLocationTest(){

        try {
            ServerSocket recieveSocket = new ServerSocket(2825);
            MockStream mockStream = new MockStream(2825);
            Thread upStream = new Thread(mockStream);
            upStream.start();
            Socket connectionSocket = recieveSocket.accept();
            InputStream stream = null;
            stream = connectionSocket.getInputStream();
            int readByte = stream.read();
            byte[] read = new byte[18641]; //clear xml messages
            DataInputStream dataInputStream = new DataInputStream(stream);
            dataInputStream.readFully(read);

            byte[] headerRest = new byte[15];
            dataInputStream.readFully(headerRest);
            byte[] body = new byte[56];
            dataInputStream.readFully(body);

            assertEquals(1, body[0]);
            assertEquals(1, body[15]);
            assertEquals(1, body[7]);
            assertEquals(0, body[24]);
            assertEquals(0, body[28]);
            assertEquals((int) (33.0 * 514.444), ((body[34] & 0xFF) << 8) + (body[33] & 0xFF));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
