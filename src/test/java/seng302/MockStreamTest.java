package seng302;


import org.junit.Test;
import seng302.controllers.MockStream;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static seng302.data.AC35StreamField.BOAT_SPEED;


public class MockStreamTest {


    @Test
    public void checkUpstreamIsSending(){

        try {
            MockStream mockStream = new MockStream(2827);
            Thread upStream = new Thread(mockStream);
            upStream.start();
            Socket connectionSocket = new Socket("localhost", 2827);
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
            MockStream mockStream = new MockStream(2829);
            Thread upStream = new Thread(mockStream);
            upStream.start();
            Socket connectionSocket = new Socket("localhost", 2829);
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

            String raceStrPath = new File("src/main/resources/defaultFiles/Race.xml").getAbsolutePath();
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
            MockStream mockStream = new MockStream(2825);
            Thread upStream = new Thread(mockStream);
            upStream.start();
            Socket connectionSocket = new Socket("localhost", 2825);
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

            String boatStrPath = new File("src/main/resources/defaultFiles/Boat.xml").getAbsolutePath();
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
            MockStream mockStream = new MockStream(2824);
            Thread upStream = new Thread(mockStream);
            upStream.start();
            Socket connectionSocket = new Socket("localhost", 2824);
            InputStream stream = null;
            stream = connectionSocket.getInputStream();
            int readByte = stream.read();
            while (readByte != 37){
                readByte = stream.read();
            }
            DataInputStream dataInputStream = new DataInputStream(stream);

            byte[] headerRest = new byte[12];
            dataInputStream.readFully(headerRest);
            byte[] body = new byte[56];
            dataInputStream.readFully(body);

            assertEquals(1, body[0]);
            assertEquals(1, body[15]);
            assertEquals(1, body[7]);
            assertEquals(0, body[24]);
            assertEquals(0, body[28]);
            assertEquals((int) (33.0 * 514.444), ((body[BOAT_SPEED.getEndIndex()-1] & 0xFF) << 8) + (body[BOAT_SPEED.getStartIndex()] & 0xFF));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void sendMarkRoundedTest(){
        try {
            MockStream mockStream = new MockStream(2823);
            Thread upStream = new Thread(mockStream);
            upStream.start();
            Socket connectionSocket = new Socket("localhost", 2823);
            InputStream stream = null;
            stream = connectionSocket.getInputStream();
            int readByte = stream.read();
            int previous = -1;
            int previousePrevious = -1;

            DataInputStream dataInputStream = new DataInputStream(stream);
            boolean passMarkType = false;
            byte[] header = new byte[15];
            while (!passMarkType){

                dataInputStream.readFully(header);
                if (header[2] == 38){
                    passMarkType = true;
                } else {
                    int length = ((header[14] & 0xFF) << 8) + (header[13] & 0xFF);
                    dataInputStream.readFully(new byte[length]);
                    dataInputStream.readFully(new byte[4]);
                }
            }

            //byte[] headerRest = new byte[12];
            //dataInputStream.readFully(headerRest);
            byte[] body = new byte[21];
            dataInputStream.readFully(body);

            assertEquals(1, body[0]);
            assertEquals(6, body[13]);
            assertEquals(1, body[17]);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
