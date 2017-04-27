package seng302.data;

import seng302.models.Course;
import seng302.models.Race;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.zip.CRC32;

import static seng302.data.AC35StreamField.*;

/**
 * Created on 13/04/17.
 */
public class DataStreamReader implements Runnable{

    private Socket clientSocket;
    private InputStream dataStream;
    private String sourceAddress;
    private int sourcePort;
    private Race race;

    private final int HEADER_LENGTH = 15;
    private final int CRC_LENGTH = 4;
    private final int XML_MESSAGE = 26;
    private final int BOAT_LOCATION_MESSAGE = 37;
    private final int RACE_STATUS_MESSAGE = 12;
    private final int MARK_ROUNDING_MESSAGE = 38;
    private final int REGATTA_XML_SUBTYPE = 5;
    private final int RACE_XML_SUBTYPE = 6;
    private final int BOAT_XML_SUBTYPE = 7;

    private final String DEFAULT_FILE_PATH = "src/main/resources/defaultFiles/";
    private final String REGATTA_FILE_NAME = "Regatta.xml";
    private final String RACE_FILE_NAME = "Race.xml";
    private final String BOAT_FILE_NAME = "Boat.xml";

    public DataStreamReader(String sourceAddress, int sourcePort){
        this.sourceAddress = sourceAddress;
        this.sourcePort = sourcePort;
    }

    /**
     * Runs the reader by setting up the connection and start reading in data
     */
    @Override
    public void run(){
        setUpConnection();
        readData();
    }

    /**
     * Sets up the connection to the data source by creating a socket and creates a InputStream from the socket
     */
    void setUpConnection() {
        try {
            clientSocket = new Socket(sourceAddress, sourcePort);
            dataStream = clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts a range of bytes in an array from beginIndex to endIndex - 1 to an integer in little endian order.
     * Range excludes endIndex to be consistent with similar Java methods (e.g. String.subString).
     * Range Length must be greater than 0 and less than or equal to 4 (to fit within a 4 byte int).
     * @param array The byte array containing the bytes to be converted
     * @param beginIndex The starting index of range of bytes to be converted
     * @param endIndex The ending index (exclusive) of the range of bytes to be converted
     * @return The integer converted from the range of bytes in little endian order
     */
    static int byteArrayRangeToInt(byte[] array, int beginIndex, int endIndex){
        int length = endIndex - beginIndex;
        if(length <= 0 || length > 4){
            throw new IllegalArgumentException("The length of the range must be between 1 and 4 inclusive");
        }

        int total = 0;
        for(int i = endIndex - 1; i >= beginIndex; i--){
            total = (total << 8) + (array[i] & 0xFF);
        }
        return total;
    }

    static long byteArrayRangeToLong(byte[] array, int beginIndex, int endIndex){
        int length = endIndex - beginIndex;

        long total = 0;
        for(int i = endIndex - 1; i >= beginIndex; i--){
            total = (total << 8) + (array[i] & 0xFF);
        }
        return total;
    }


    /**
     * Converts an integer to a latitude/longitude angle as per specification.
     * (-2^31 = -180 deg, 2^31 = 180 deg)
     * @param value the latitude/longitude as a scaled integer
     * @return the actual latitude/longitude angle
     */
    static double intToLatLon(int value){
        return (double)value * 180 / Math.pow(2, 31);
    }

    /**
     * Converts an integer to a heading angle as per specification.
     * @param value the heading as a scaled integer
     * @return the actual angle of the heading
     */
    static double intToHeading(int value){
        return (double)value * 360 / Math.pow(2, 16);
    }

    /**
     * Reads in a XML Message, parses the header and saves the XML payload to the corresponding file
     * @param body The byte array containing the XML Message (header + payload)
     */
    private void convertXMLMessage(byte[] body) throws IOException {
        int xmlSubtype = byteArrayRangeToInt(body, XML_SUBTYPE.getStartIndex(), XML_SUBTYPE.getEndIndex());
        int xmlLength = byteArrayRangeToInt(body, XML_LENGTH.getStartIndex(), XML_LENGTH.getEndIndex());

        String xmlBody = new String(Arrays.copyOfRange(body, XML_BODY.getStartIndex(), XML_BODY.getStartIndex()+xmlLength));
        xmlBody = xmlBody.trim();

        String outputFilePath = DEFAULT_FILE_PATH;

        if(xmlSubtype == REGATTA_XML_SUBTYPE) {
            outputFilePath += REGATTA_FILE_NAME;
        } else if(xmlSubtype == RACE_XML_SUBTYPE){
            outputFilePath += RACE_FILE_NAME;
        } else if(xmlSubtype == BOAT_XML_SUBTYPE){
            outputFilePath += BOAT_FILE_NAME;
        }

        FileWriter outputFileWriter = new FileWriter(outputFilePath);
        BufferedWriter bufferedWriter = new BufferedWriter(outputFileWriter);
        bufferedWriter.write(xmlBody);
        bufferedWriter.close();
    }

    /**
     * Parses portions of the boat location message byte array to their corresponding values.
     * @param body the byte array containing the boat location message
     */
    private void parseBoatLocationMessage(byte[] body) {
        int sourceID = byteArrayRangeToInt(body, SOURCE_ID.getStartIndex(), SOURCE_ID.getEndIndex());
        int latScaled = byteArrayRangeToInt(body, LATITUDE.getStartIndex(), LATITUDE.getEndIndex());
        int lonScaled = byteArrayRangeToInt(body, LONGITUDE.getStartIndex(), LONGITUDE.getEndIndex());
        int headingScaled = byteArrayRangeToInt(body, HEADING.getStartIndex(), HEADING.getEndIndex());
        int boatSpeed = byteArrayRangeToInt(body, SPEED_OVER_GROUND.getStartIndex(), SPEED_OVER_GROUND.getEndIndex());

        int deviceType = byteArrayRangeToInt(body, DEVICE_TYPE.getStartIndex(), DEVICE_TYPE.getEndIndex());

        double lat = intToLatLon(latScaled);
        double lon = intToLatLon(lonScaled);
        double heading = intToHeading(headingScaled);
        double speedInKnots = convertToKnots(boatSpeed);

        if(deviceType == 1){
            race.updateBoat(sourceID, lat, lon, heading, speedInKnots);
        } else if(deviceType == 3){
            race.updateMark(sourceID, lat, lon);
        }
    }

    private double convertToKnots(int boatSpeed) {
        return ((boatSpeed / 1e6) * 3600) / 1.852;
    }

    /**
     * Calculates the CRC from header + body and checks if it is equal to the value from the expected CRC byte array
     * @param header The header of the message
     * @param body The body of the message
     * @param crc The expected CRC of the header and body combined
     * @return True if the calculated CRC is equal to the expected CRC, False otherwise
     */
    private boolean checkCRC(byte[] header, byte[] body, byte[] crc) {
        CRC32 actualCRC = new CRC32();
        actualCRC.update(header);
        actualCRC.update(body);
        long expectedCRCValue = Integer.toUnsignedLong(byteArrayRangeToInt(crc, 0, 4));
        return expectedCRCValue == actualCRC.getValue();
    }

    /**
     * Keeps reading in from the data stream and parses each message header and hands off the payload to the
     * corresponding method. Ignores the message if the message type is not needed.
     */
    private void readData(){
        DataInput dataInput = new DataInputStream(dataStream);
        try{
            while(true){
                byte[] header = new byte[HEADER_LENGTH];
                dataInput.readFully(header);

                int messageLength = byteArrayRangeToInt(header, MESSAGE_LENGTH.getStartIndex(), MESSAGE_LENGTH.getEndIndex());
                int messageType = byteArrayRangeToInt(header, MESSAGE_TYPE.getStartIndex(), MESSAGE_TYPE.getEndIndex());

                byte[] body = new byte[messageLength];
                dataInput.readFully(body);
                byte[] crc = new byte[CRC_LENGTH];
                dataInput.readFully(crc);

                if(checkCRC(header, body, crc)){
                    switch(messageType){
                        case XML_MESSAGE:
                            convertXMLMessage(body);
                            break;
                        case BOAT_LOCATION_MESSAGE:
                            parseBoatLocationMessage(body);
                            break;
                        case RACE_STATUS_MESSAGE:
                            parseRaceStatusMessage(body);
                            int raceStatus = byteArrayRangeToInt(body, RACE_STATUS.getStartIndex(), RACE_STATUS.getEndIndex());
                            race.updateRaceStatus(raceStatus);
                            break;
                        case MARK_ROUNDING_MESSAGE:
                            parseMarkRoundingMessage(body);
                    }
                } else{
                    System.err.println("Incorrect CRC. Message Ignored.");
                }
            }
        } catch (IOException e){
            System.err.println("Error occurred when reading data from stream:");
            System.err.println(e);
        }
    }

    private void parseMarkRoundingMessage(byte[] body) {
        long time = byteArrayRangeToLong(body, 1, 7);
        int sourceID = byteArrayRangeToInt(body, 13, 17);
        int markID = byteArrayRangeToInt(body, 20, 21);
        race.updateMarkRounded(sourceID, markID, time);
    }

    private void parseRaceStatusMessage(byte[] body) {
        int raceCourseWindDirection = byteArrayRangeToInt(body, 18, 20);
        int raceCourseWindSpeed = byteArrayRangeToInt(body, 20, 22);
        race.getCourse().updateCourseWindValues(raceCourseWindDirection, raceCourseWindSpeed);
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setRace(Race race) {
        this.race = race;
    }

}
