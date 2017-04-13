package seng302;

import com.sun.xml.internal.ws.commons.xmlutil.Converter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import seng302.data.DataStreamReader;
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

    private void parseXML(byte[] body) throws IOException {
        int xmlSubType = (body[9] & 0xFF);
        String xmlMessage = new String(Arrays.copyOfRange(body, 14, body.length));
        xmlMessage.replaceAll("\\p{C}", "?");

        FileWriter outputFileWriter = null;
        System.out.println(xmlSubType);

        if(xmlSubType == 5) {
            outputFileWriter = new FileWriter("src/main/resources/defaultFiles/Regatta.xml");
        } else if(xmlSubType == 6){
            outputFileWriter = new FileWriter("src/main/resources/defaultFiles/Race.xml");
        } else if(xmlSubType == 7){
            outputFileWriter = new FileWriter("src/main/resources/defaultFiles/Boat.xml");
        }

        BufferedWriter bufferedWriter = new BufferedWriter(outputFileWriter);
        bufferedWriter.write(xmlMessage);

        bufferedWriter.close();
    }

    @Ignore
    @Test
    public void readData() throws IOException{
        setUp();
        int HEADER_LENGTH = 15;
        int CRC_LENGTH = 4;
        DataInputStream dataInputStream = new DataInputStream(newStream);

        while(true){
            byte[] header = new byte[HEADER_LENGTH];
            dataInputStream.readFully(header);
            int messageLength = byteArrayToInt(header, 13, 14);
            int messageType = header[2];

            byte[] body = new byte[messageLength];
            dataInputStream.readFully(body);
            byte[] crc = new byte[CRC_LENGTH];
            dataInputStream.readFully(crc);

            if(messageType == 26){
                //XML
                parseXML(body);
            } else if(messageType == 37){
                //Boat Location
                parseBoatLocationMessage(body);
            } else if(messageType == 12){
                System.out.println("Race Status: " + byteArrayToInt(body, 11, 11));
            }
        }
    }

    private int byteArrayToInt(byte[] array, int start, int end){
        int total = 0;
        for(int i = end; i >= start; i--){
            total = (total << 8) + (array[i] & 0xFF);
        }
        return total;
    }

    private double intToLatLon(int value){
        return (double)value * 180 / Math.pow(2, 31);
    }

    private double intToHeading(int value){
        return value * 360 / Math.pow(2, 16);
    }

    private void parseBoatLocationMessage(byte[] body) {
        System.out.println(Arrays.toString(body));
        System.out.println(body[33]);
        System.out.println(body[34]);
        System.out.println(body[35]);
        System.out.println(body[36]);
        int sourceID = byteArrayToInt(body, 7, 10);
        int latScaled = byteArrayToInt(body, 16, 19);
        int lonScaled = byteArrayToInt(body, 20, 23);
        int headingScaled = byteArrayToInt(body, 28, 29);
        int boatSpeed = byteArrayToInt(body, 34, 35);
        int speedOverGround = byteArrayToInt(body, 38, 39);

        int deviceType = byteArrayToInt(body, 15, 15);


        double lat = intToLatLon(latScaled);
        double lon = intToLatLon(lonScaled);
        double heading = intToHeading(headingScaled);

        System.out.println("Device Type: " + deviceType);
        System.out.println("Latitude: " + lat);
        System.out.println("Longitude: " + lon);
        System.out.println("Heading: " + heading);
        System.out.println("Speed: " + (double)boatSpeed * 3600 / 1000);
        System.out.println("SpeedOverGround: " + (double)speedOverGround * 3600 / 1000);


        System.out.println();
        System.out.println();

    }

    @Ignore
    @Test
    public void intToLatLonTest(){
        byte[] test = new byte[4];
        test[0] = (byte) 201;
        test[1] = (byte) 83;
        test[2] = (byte) 247;
        test[3] = (byte) 22;

        int n = byteArrayToInt(test, 0, 3);
        System.out.println(n + " " + intToLatLon(n));

    }
}
