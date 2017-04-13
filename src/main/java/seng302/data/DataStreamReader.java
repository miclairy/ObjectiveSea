package seng302.data;

import seng302.utilities.Config;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by atc60 on 13/04/17.
 */
public class DataStreamReader {

    private Socket clientSocket;
    private InputStream newStream;

    /**
     * Sets up the connection to the data source
     */
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

        //Will replace in future, to remove random zero spaced character at end of Boat.xml
        if(xmlSubType == 7){
            xmlMessage = new String(Arrays.copyOfRange(body, 14, body.length-1));
        }

        FileWriter outputFileWriter = null;

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
        int sourceID = byteArrayToInt(body, 7, 10);
        int latScaled = byteArrayToInt(body, 16, 19);
        int lonScaled = byteArrayToInt(body, 20, 23);
        int headingScaled = byteArrayToInt(body, 28, 29);
        int boatSpeed = byteArrayToInt(body, 34, 35);

        double lat = intToLatLon(latScaled);
        double lon = intToLatLon(lonScaled);
        double heading = intToHeading(headingScaled);
    }

    public void readData(){
        setUp();
        int HEADER_LENGTH = 15;
        int CRC_LENGTH = 4;
        DataInputStream dataInputStream = new DataInputStream(newStream);
        try{
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
        } catch (IOException e){
            System.out.println(e);
        }
    }

}
