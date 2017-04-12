package seng302;

import com.sun.xml.internal.ws.commons.xmlutil.Converter;
import org.junit.Before;
import org.junit.Test;
import seng302.controllers.ConnectionController;
import seng302.utilities.Config;
import sun.misc.IOUtils;
import sun.nio.ch.IOUtil;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by lga50 on 12/04/17.
 */
public class ConnectionTest {

    private Socket clientSocket;
    private InputStream newStream;

    public void setUp() {
        try {
            clientSocket = new Socket("livedata.americascup.com", 4941);
            newStream = clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void connectionTest() throws IOException {
        setUp();
        boolean decoding = false;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();


        String sentence;
        FileWriter boatFileWriter = new FileWriter("src/main/resources/defaultFiles/Boat.xml");
        FileWriter raceFileWriter = new FileWriter("src/main/resources/defaultFiles/Race.xml");
        BufferedWriter bufferedWriter = null;
        boolean readingBoat = false;
        boolean readingRace = false;
        boolean finishedReading = false;
        int previous = -1;
        int current  = -1;
        while (true){
            current = newStream.read();
            System.out.println(current);
            if (previous == 71 && current == 131) {
                int type = newStream.read();
                System.out.println("type " + type);
                int time = 0;
                for (int j = 0; j < 6; j++) {
                    time += newStream.read();
                    System.out.println("time " + time);
                }

                int sourceId = 0;
                for (int k = 0; k < 4; k++) {
                    sourceId += newStream.read();
                    System.out.println("id " + sourceId);
                }

                int messageLength = newStream.read();
                System.out.println("len 1 " + messageLength);
                messageLength += newStream.read();
                System.out.println("len 2 " + messageLength);
                if (type == 26 || type == 20){
                    int read = 0;
                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    while((sentence = inFromServer.readLine()) != null) { //need to combine 2 bytes to get one int
                        System.out.println(sentence);
                        read += sentence.getBytes().length;
                        if (sentence.contains("<Race>")) {
                            readingRace = true;
                            bufferedWriter = new BufferedWriter(raceFileWriter);
                        } else if (sentence.contains("<BoatConfig>")) {
                            readingBoat = true;
                            bufferedWriter = new BufferedWriter(boatFileWriter);
                        }

                        if (readingBoat || readingRace) {
                            bufferedWriter.write(sentence + "\n");
                        }

                        if (sentence.contains("</Race>")) {
                            readingRace = false;
                            bufferedWriter.close();
                        } else if (sentence.contains("</BoatConfig>")) {
                            readingBoat = false;
                            bufferedWriter.close();
                            finishedReading = true;
                        }

                    }
                }
            }
            previous = current;
        }
        //clientSocket.close();
    }

    @Test
    public void connectionTest2() throws IOException{
        setUp();
        int HEADER_LENGTH = 15;
        int CRC_LENGTH = 4;
        DataInputStream dataInputStream = new DataInputStream(newStream);

        while(true){
            byte[] header = new byte[HEADER_LENGTH];
            dataInputStream.readFully(header);
            int messageLength = ((header[14] & 0xFF) << 8) + (header[13] & 0xFF);
            int messageType = header[2];

            System.out.println(Arrays.toString(header));
            System.out.println("Message Length: " + messageLength);
            System.out.println("Message Type: " + messageType);

            byte[] body = new byte[messageLength];
            dataInputStream.readFully(body);
            byte[] crc = new byte[CRC_LENGTH];
            dataInputStream.readFully(crc);

            if(messageType == 26){
                //XML
                System.out.println(new String(body));
            } else if(messageType == 37){
                //Boat Location
                parseBoatLocationMessage(body);
            } else if(messageType == 12){
                System.out.println("Race Status: " + byteArrayToInt(Arrays.copyOfRange(body, 11, 12)));
            }

            System.out.println();
        }
    }

    private int byteArrayToInt(byte[] array){
        int total = 0;
        for(int i = array.length - 1; i >= 0; i--){
            total = (total << 8) + (array[i] & 0xFF);
        }
        return total;
    }

    private double intToLatLon(int value){
        return (double)value * 180 / Math.pow(2, 31);
    }

    private void parseBoatLocationMessage(byte[] body) {
        int sourceID = byteArrayToInt(Arrays.copyOfRange(body, 7, 11));
        int lat = byteArrayToInt(Arrays.copyOfRange(body, 16, 20));
        int lon = byteArrayToInt(Arrays.copyOfRange(body, 20, 24));
        int boatSpeed = byteArrayToInt(Arrays.copyOfRange(body, 20, 24));
        int heading = byteArrayToInt(Arrays.copyOfRange(body, 28, 30));

        System.out.println("Source ID: " + sourceID);
        System.out.println("Latitude: " + intToLatLon(lat));
        System.out.println("Longitude: " + intToLatLon(lon));
        System.out.println("Speed: " + boatSpeed + " mm/sec");
        System.out.println("Heading: " + heading * 360 / Math.pow(2, 16));
    }
}
