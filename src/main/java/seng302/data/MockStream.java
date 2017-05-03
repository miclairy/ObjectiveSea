package seng302.data;

import seng302.controllers.MockRaceRunner;
import seng302.models.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static seng302.data.AC35StreamField.*;
import static seng302.data.AC35StreamMessage.*;
import static seng302.data.AC35StreamXMLMessage.*;

public class MockStream implements Runnable {

    private final int SOURCE_ID = 28;
    private final int HEADER_LENGTH = 15;
    private final int ROUNDING_MARK_TYPE = 1;
    private final int GATE_TYPE = 2;

    private DataOutputStream outToServer;
    private int port;
    private Socket clientSocket;

    private Map<AC35StreamXMLMessage, Integer> xmlSequenceNumber = new HashMap<>();
    private Map<Boat, Integer> boatSequenceNumbers = new HashMap<>();
    private Map<Boat, Integer> lastMarkRoundingSent = new HashMap<>();

    private MockRaceRunner raceRunner;

    public MockStream(int port, MockRaceRunner raceRunner){
        this.port = port;
        this.raceRunner = raceRunner;
    }

    /**
     * Creates the server socket, sets up message maps and blocks until a client has connected
     * @throws IOException
     */
    private void initialize() throws IOException  {
        ServerSocket server = new ServerSocket(port);
        xmlSequenceNumber.put(REGATTA_XML_MESSAGE, 0);
        xmlSequenceNumber.put(RACE_XML_MESSAGE, 0);
        xmlSequenceNumber.put(BOAT_XML_MESSAGE, 0);

        for (Boat boat: raceRunner.getBoatsInRace()){
            boatSequenceNumbers.put(boat, boat.getId());
            lastMarkRoundingSent.put(boat, -1);
        }

        clientSocket = server.accept();
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        //System.out.println("Client accepted");
    }

    /**
     * Sends all the data to the socket while the boats have not all finished.
     */
    @Override
    public void run() {
        try {
            initialize();

            sendInitialRaceMessages();

            while (!raceRunner.raceHasEnded()) {
                sendRaceUpdates();
                try {
                    Thread.sleep((long) 0.2 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            sendRaceUpdates(); //send one last message block with ending data

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the XML messages when the client has connected
     */
    private void sendInitialRaceMessages() {
        sendXmlMessage(RACE_XML_MESSAGE, "Race.xml");
        sendXmlMessage(BOAT_XML_MESSAGE, "Boat.xml");
        sendXmlMessage(REGATTA_XML_MESSAGE, "Regatta.xml");
    }

    /**
     * Sends Race Status, Boat Location and Mark Rounding messages that are currently necessary
     * @throws IOException
     */
    private void sendRaceUpdates() throws IOException {
        RaceStatus raceStatus = raceRunner.getRaceStatus();
        List<Boat> boatsInRace = raceRunner.getBoatsInRace();
        byte[] raceStatusBody = initialiseRaceStatusMessage(boatsInRace.size());

        for (Boat boat : boatsInRace) {
            raceStatusBody = addBoatToRaceStatusMessage(boat, raceStatusBody);
            if(raceStatus.equals(RaceStatus.STARTED) && !boat.isFinished()) {
                sendBoatMessages(boat);
            }
        }

        byte[] header = createRaceStatusHeader(boatsInRace.size());
        sendPacket(header, raceStatusBody);
    }

    /**
     * Creates a header byte array for a Race Status Message
     * @param numBoatsInRace
     * @return a byte array representing a correctly formed Race Status Message header
     */
    private byte[] createRaceStatusHeader(int numBoatsInRace) {
        byte[] header = createHeader(RACE_STATUS_MESSAGE);
        addFieldToByteArray(header, MESSAGE_LENGTH, 24 + 26 * (numBoatsInRace));
        return header;
    }

    /**
     * Sends a Boat Location Message and possible a Mark Rounding message for a boat
     * @param boat the boat to send messages for
     * @throws IOException
     */
    private void sendBoatMessages(Boat boat) throws IOException {
        sendPacket(createHeader(BOAT_LOCATION_MESSAGE), createBoatLocationMessage(boat));
        if (lastMarkRoundingSent.get(boat) != boat.getLastRoundedMarkIndex()){
            lastMarkRoundingSent.put(boat, boat.getLastRoundedMarkIndex());
            sendPacket(createHeader(MARK_ROUNDING_MESSAGE), createMarkRoundingMessage(boat, raceRunner.getCourse()));
        }
    }

    /**
     * Sends a header, body then generates and sends a CRC for that header and body
     * @param header a correctly formed AC35 header
     * @param body a correctly formed AC35 body
     * @throws IOException
     */
    private void sendPacket(byte[] header, byte[] body) throws IOException {
        outToServer.write(header);
        outToServer.write(body);
        sendCRC(header, body);
    }

    /**
     * creates mark rounding message body
     * @param boat the boat that rounded
     * @param course the course which was rounded
     * @return byte array of the body of the message
     */
    private byte[] createMarkRoundingMessage(Boat boat, Course course) {
        byte[] body = new byte[MARK_ROUNDING_MESSAGE.getLength()];
        body[0] = 1;
        addFieldToByteArray(body, BOAT_TIMESTAMP,(int) Instant.now().toEpochMilli());
        addFieldToByteArray(body, MARK_ACK, 0); //todo make proper ack
        addFieldToByteArray(body, MARK_RACE_ID, Integer.parseInt(raceRunner.getRaceId()));
        addFieldToByteArray(body, MARK_SOURCE, boat.getId());
        addFieldToByteArray(body, MARK_BOAT_STATUS, boat.getStatus().getValue());
        addFieldToByteArray(body, ROUNDING_SIDE, 0); //todo present correct side
        if (course.getCourseOrder().get(boat.getLastRoundedMarkIndex()).hasTwoMarks()) {
            addFieldToByteArray(body, MARK_TYPE, GATE_TYPE);
        } else {
            addFieldToByteArray(body, MARK_TYPE, ROUNDING_MARK_TYPE);
        }
        addFieldToByteArray(body, MARK_ID, boat.getLastRoundedMarkIndex()); //todo give marks ids correctly
        return body;
    }


    /**
     * initialise Race Status Message
     * @param numBoats to send the number
     * @return byte array of body
     */
    private byte[] initialiseRaceStatusMessage(int numBoats) {
        byte[] body = new byte[24 + (26 * numBoats)];
        addFieldToByteArray(body, STATUS_MESSAGE_VERSION_NUMBER, 2);
        addFieldToByteArray(body, BOAT_TIMESTAMP, Instant.now().toEpochMilli());
        addFieldToByteArray(body, STATUS_RACE_ID, Integer.parseInt(raceRunner.getRaceId()));
        addFieldToByteArray(body, RACE_STATUS, raceRunner.getRaceStatus().getValue());
        addFieldToByteArray(body, EXPECTED_START_TIME, raceRunner.getStartTime());
        addFieldToByteArray(body, CURRENT_TIME, Instant.now().toEpochMilli());
        addFieldToByteArray(body, RACE_COURSE_WIND_DIRECTION, 0x6000); // left for now
        addFieldToByteArray(body, RACE_COURSE_WIND_SPEED, 10); //left at 10knots for now
        addFieldToByteArray(body, NUMBER_OF_BOATS_IN_RACE, numBoats);
        addFieldToByteArray(body, RACE_TYPE, 2); //fleet race

        return body;
    }

    /**
     * add Boat To Race Status Message
     * @param boat boat to add
     * @param body message to add too
     * @return new body message
     */
    private byte[] addBoatToRaceStatusMessage(Boat boat, byte[] body){
        addFieldToByteArray(body, STATUS_SOURCE_ID, boat.getId());
        addFieldToByteArray(body, BOAT_STATUS, boat.getStatus().getValue());
        addFieldToByteArray(body, LEG_NUMBER, boat.getLastRoundedMarkIndex() + 1);
        addFieldToByteArray(body, NUMBER_PENALTIES_AWARDED, 0); //todo make penalties a thing
        addFieldToByteArray(body, NUMBER_PENALTIES_SERVED, 0);
        addFieldToByteArray(body, ESTIMATED_TIME_AT_NEXT_MARK, 0); //todo time estimation til next mark/ end of race
        addFieldToByteArray(body, ESTIMATED_TIME_AT_FINISH, 0);

        return body;
    }

    /**
     * Builds a byte array representing a boat location message
     * @param boat the boat that is the subject of the message
     * @return a byte array representing the boat location message
     */
    private byte[] createBoatLocationMessage(Boat boat) {
        byte[] body = initialiseLocationPacket();
        Coordinate location = boat.getCurrentPosition();

        int currentSequenceNumber = boatSequenceNumbers.get(boat);
        boatSequenceNumbers.put(boat, currentSequenceNumber + 1); //increment sequence number

        int lat = (int) Math.round(location.getLat() * Math.pow(2, 31) / 180);
        int lon = (int) Math.round(location.getLon() * Math.pow(2, 31) / 180);
        addFieldToByteArray(body, BOAT_TIMESTAMP,(int) Instant.now().toEpochMilli());
        addFieldToByteArray(body, BOAT_SOURCE_ID, boat.getId());
        addFieldToByteArray(body, BOAT_SEQUENCE_NUM, currentSequenceNumber);
        addFieldToByteArray(body, LATITUDE, lat);
        addFieldToByteArray(body, LONGITUDE, lon);
        addFieldToByteArray(body, HEADING, (int) (boat.getHeading() * Math.pow(2, 16) / 360));
        addFieldToByteArray(body, SPEED_OVER_GROUND, boat.getSpeedInMMS());

        return body;
    }

    /**
     * Sends an xml message type to the socket including the header, body and CRC
     * @param type subtype of the xml message
     * @param fileName name of the file to send
     */
    private void sendXmlMessage(AC35StreamXMLMessage type, String fileName){
        byte[] header = createHeader(XML_MESSAGE);
        byte[] xmlBody = generateXmlBody(type, fileName);
        addFieldToByteArray(header, MESSAGE_LENGTH, xmlBody.length);
        try {
            sendPacket(header, xmlBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a file as bytes and adds the information about the version, sequence number set to 1, subtype, timestamp,
     * xml sequence number and length then
     * @param subType the integer number of the subtype of the xml message
     * @param fileName the file which to read the bytes from
     * @return a byte array which is the body of the xml message
     */
    private byte[] generateXmlBody(AC35StreamXMLMessage subType, String fileName) {
        String raceStrPath = new File("src/main/resources/defaultFiles/" + fileName).getAbsolutePath();
        Path racePath = Paths.get(raceStrPath);

        try {
            int sequenceNumber = xmlSequenceNumber.get(subType) + 1; //increment sequence number
            xmlSequenceNumber.put(subType, sequenceNumber);

            byte[] bodyContent = Files.readAllBytes(racePath);
            byte[] body = new byte[XML_BODY.getStartIndex() + bodyContent.length];

            addFieldToByteArray(body, XML_VERSION, 1);
            addFieldToByteArray(body, XML_ACK, 1);
            addFieldToByteArray(body, XML_TIMESTAMP, (int) Instant.now().toEpochMilli());
            addFieldToByteArray(body, XML_SUBTYPE, subType.getType());
            addFieldToByteArray(body, XML_SEQUENCE, sequenceNumber);
            addFieldToByteArray(body, XML_LENGTH, bodyContent.length);

            for (int i = 0; i < bodyContent.length; i++){
                body[i + XML_BODY.getStartIndex()] = bodyContent[i];
            }
            return body;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * creates a header byte array which has 2 snyc bytes, a type, timestamp, source id which is 28 at the moment and the
     * message length if it is not variable.
     * @param type the integer type of the message
     * @return a byte array of the header
     */
    private byte[] createHeader(AC35StreamMessage type) {
        byte[] header = new byte[HEADER_LENGTH];

        header[0] = (byte) 0x47; //first sync byte
        header[1] = (byte) 0x83; //second sync byte
        addFieldToByteArray(header, MESSAGE_TYPE, type.getValue());
        addFieldToByteArray(header, HEADER_TIMESTAMP, (int) Instant.now().toEpochMilli());
        addFieldToByteArray(header, HEADER_SOURCE_ID, SOURCE_ID);
        if (type.getLength() != -1) {
            addFieldToByteArray(header, MESSAGE_LENGTH, type.getLength());
        }
        return header;
    }

    /**
     * Computes and sends the CRC checksum to the socket
     * @param header The header byte array used to compute the CRC
     * @param body the body byte array used to compute the CRC
     */
    private void sendCRC(byte[] header, byte[] body){
        final int CRC_LENGTH = 4;
        Checksum crc = new CRC32();
        byte[] toCRC = new byte[header.length + body.length];
        for (int i = 0; i < toCRC.length; i ++){
            if (i < HEADER_LENGTH) {
                toCRC[i] = header[i];
            } else {
                toCRC[i] = body[i - HEADER_LENGTH];
            }
        }
        crc.update(toCRC, 0, toCRC.length);
        try {
            byte[] crcArray = new byte[CRC_LENGTH];
            addIntIntoByteArray(crcArray, 0, (int) crc.getValue(), CRC_LENGTH);
            outToServer.write(crcArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialises the static contents of the location packet body
     * @return a byte array of the version number, device type, altitude
     */
    private byte[] initialiseLocationPacket() {

        byte[] body = new byte[BOAT_LOCATION_MESSAGE.getLength()];
        body[0] = (byte) 1;
        body[15] = (byte) 1;
        body[24] = (byte) 0;
        return body;
    }

    /**
     * Simplifier function for adding stream field to byte array
     * @param array array which to add the int
     * @param field the AC35StreamField field to add
     * @param item the item to add
     */
    private void addFieldToByteArray(byte[] array, AC35StreamField field, long item){
        addIntIntoByteArray(array, field.getStartIndex(), item, field.getLength());
    }

    /**
     * Splits an integer into a few bytes and adds it to a byte array
     * @param array array which to add the int
     * @param start index it start adding
     * @param item item to add
     * @param numBytes number of bytes to split the int into
     */
    private void addIntIntoByteArray(byte[] array, int start, long item, int numBytes){
        for (int i = 0; i < numBytes; i ++) {
            array[start + i] = (byte) (item >> i * 8);
        }
    }
}
