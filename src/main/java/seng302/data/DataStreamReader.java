package seng302.data;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * Created on 13/04/17.
 */
public class DataStreamReader implements Runnable{

    private Socket clientSocket;
    private InputStream dataStream;
    private String sourceAddress;
    private int sourcePort;

    private final int HEADER_LENGTH = 15;
    private final int CRC_LENGTH = 4;
    private final int XML_MESSAGE = 26;
    private final int BOAT_LOCATION_MESSAGE = 37;
    private final int RACE_STATUS_MESSAGE = 12;
    private final int REGATTA_XML_SUBTYPE = 5;
    private final int RACE_XML_SUBTYPE = 6;
    private final int BOAT_XML_SUBTYPE = 7;

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
    private int byteArrayRangeToInt(byte[] array, int beginIndex, int endIndex){
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

    /**
     * Reads in a XML Message, parses the header and saves the XML payload to the corresponding file
     * @param body The byte array containing the XML Message (header + payload)
     */
    private void convertXMLMessage(byte[] body) throws IOException {
        int xmlSubtype = byteArrayRangeToInt(body, 9, 10);
        int xmlLength = byteArrayRangeToInt(body, 12, 14);

        String xmlMessage = new String(Arrays.copyOfRange(body, 14, 14+xmlLength));
        xmlMessage = xmlMessage.trim();

        FileWriter outputFileWriter = null;
        if(xmlSubtype == REGATTA_XML_SUBTYPE) {
            outputFileWriter = new FileWriter("src/main/resources/defaultFiles/Regatta.xml");
        } else if(xmlSubtype == RACE_XML_SUBTYPE){
            outputFileWriter = new FileWriter("src/main/resources/defaultFiles/Race.xml");
        } else if(xmlSubtype == BOAT_XML_SUBTYPE){
            outputFileWriter = new FileWriter("src/main/resources/defaultFiles/Boat.xml");
        }

        BufferedWriter bufferedWriter = new BufferedWriter(outputFileWriter);
        bufferedWriter.write(xmlMessage);
        bufferedWriter.close();
    }

    /**
     * Converts an integer to a latitude/longitude angle as per specification.
     * (-2^31 = -180 deg, 2^31 = 180 deg)
     * @param value the latitude/longitude as a scaled integer
     * @return the actual latitude/longitude angle
     */
    private double intToLatLon(int value){
        return (double)value * 180 / Math.pow(2, 31);
    }

    /**
     * Converts an integer to a heading angle as per specification.
     * @param value the heading as a scaled integer
     * @return the actual angle of the heading
     */
    private double intToHeading(int value){
        return (double)value * 360 / Math.pow(2, 16);
    }

    /**
     * Parses portions of the boat location message byte array to their corresponding values
     * @param body the byte array containing the boat location message
     */
    private void parseBoatLocationMessage(byte[] body) {
        int sourceID = byteArrayRangeToInt(body, 7, 11);
        int latScaled = byteArrayRangeToInt(body, 16, 20);
        int lonScaled = byteArrayRangeToInt(body, 20, 24);
        int headingScaled = byteArrayRangeToInt(body, 28, 30);
        int boatSpeed = byteArrayRangeToInt(body, 34, 36);

        double lat = intToLatLon(latScaled);
        double lon = intToLatLon(lonScaled);
        double heading = intToHeading(headingScaled);
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

                int messageLength = byteArrayRangeToInt(header, 13, 15);
                int messageType = header[2];

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
                            System.out.println("Race Status: " + byteArrayRangeToInt(body, 11, 12));
                            break;
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

    public Socket getClientSocket() {
        return clientSocket;
    }
}
