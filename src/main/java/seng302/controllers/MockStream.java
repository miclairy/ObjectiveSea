package seng302.controllers;

import seng302.data.AC35StreamField;
import seng302.data.RaceVisionFileReader;
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

public class MockStream implements Runnable {

    private final int SOURCE_ID = 28;
    private final int HEADER_LENGTH = 15;
    private Map<Integer, Integer> messageLengths = new HashMap<>();

    private final double KNOTS_TO_MMS_MULTIPLIER = 514.444;

    private DataOutputStream outToServer;
    private int port;
    private Socket clientSocket;

    private Map<String, Integer> messageTypes = new HashMap<>();
    private Map<String, Integer> xmlMessageTypes = new HashMap<>();
    private Map<Integer, Integer> xmlSequenceNumber = new HashMap<>();
    private Map<Boat, Integer> boatSequenceNumbers = new HashMap<>();
    private List<Boat> boatsInRace;
    private double heading = 0;
    private double speed;
    private Course course;
    private Boolean sendPassMark;
    private int raceId;

    private long startTime;

    RaceStatus raceStatus = RaceStatus.NOT_ACTIVE;
    private enum RaceStatus {
        NOT_ACTIVE(0), WARNING(1), PREPARATORY(2), STARTED(3), FINISHED(4), RETIRED(5),
        ABANDONED(6), TERMINATED(8), RACE_START_TIME_NOT_SET(9), PRESTART(10);
        private final int value;
        RaceStatus(int value){ this.value = value; }
        public int getValue() {return value;}
    }


    public MockStream(int port){
        this.port = port;
    }

    /**
     * Creates the server socket, sets up message maps and blocks until a client has connected
     * @throws IOException
     */
    private void initialize() throws IOException  {
        ServerSocket server = new ServerSocket(port);
        boatsInRace = RaceVisionFileReader.importStarters(null);
        course = RaceVisionFileReader.importCourse(null);
        setStartingPositions();
        messageTypes.put("XmlMessage", 26);
        messageTypes.put("BoatLocation", 37);
        messageTypes.put("markRounding", 38);
        messageTypes.put("raceStatus", 12);
        xmlMessageTypes.put("Race", 6);
        xmlMessageTypes.put("Boat", 7);
        xmlSequenceNumber.put(6, 0);
        xmlSequenceNumber.put(7, 0);
        messageLengths.put(38, 21);
        messageLengths.put(37, 56);
        messageLengths.put(12, 24+(26*boatsInRace.size()));

        for (Boat boat: boatsInRace){
            boatSequenceNumbers.put(boat, boat.getId());
        }

        Random random = new Random();
        raceId = random.nextInt(Integer.MAX_VALUE);

        clientSocket = server.accept();
        System.out.println("Client accepted");


        startTime = Instant.now().toEpochMilli() + 5000; //5 seconds from now
        raceStatus = raceStatus.PRESTART;
        boatsInRace.forEach(b -> b.setStatus(1)); //set status to prerace


    }

    /**
     * Sends all the data to the socket while the boats have not all finished.
     */
    @Override
    public void run() {

        try {
            initialize();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        double secTimePassed = 0;
        try {
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            sendXmlMessage("Race", "Race.xml");
            sendXmlMessage("Boat", "Boat.xml");

            Boolean notFinished = true;
            while (notFinished) {
                byte[] raceStatusBody = initiliseRaceStatusMessage();
                for (Boat boat : boatsInRace) {

                    raceStatusBody = addBoatToRaceStatusMessage(boat, raceStatusBody);

                    notFinished = false;
                    sendPassMark = false;

                    if(!raceStatus.equals(RaceStatus.STARTED)){
                        Coordinate location = updateLocation(boat, secTimePassed, course);
                        if (location != null) {
                            notFinished = true;
                            byte[] header = createHeader(messageTypes.get("BoatLocation"));
                            outToServer.write(header);
                            byte[] body = createBoatLocationMessage(boat, location);
                            outToServer.write(body);
                            sendCRC(header, body);
                        }
                        if (sendPassMark){
                            byte[] header = createHeader(messageTypes.get("markRounding"));
                            byte[] body = createMarkRoundingMessage(boat);
                            outToServer.write(header);
                            outToServer.write(body);
                            sendCRC(header, body);
                        }
                    }else{
                        long millisBeforeStart = startTime - Instant.now().toEpochMilli();
                        if(millisBeforeStart < 3000 && millisBeforeStart > 1000){
                            raceStatus = RaceStatus.WARNING;
                        }else if(millisBeforeStart < 1000  && millisBeforeStart >0){
                            raceStatus = RaceStatus.PREPARATORY;
                        }else if (millisBeforeStart < 0){
                            raceStatus = RaceStatus.STARTED;
                            boatsInRace.forEach(b -> b.setStatus(2)); //set status to Racing

                        }
                    }

                }
                byte[] header = createHeader(messageTypes.get("raceStatus"));
                outToServer.write(header);
                outToServer.write(raceStatusBody);
                sendCRC(header, raceStatusBody);
                try {
                    Thread.sleep((long) 0.2 * 1000);
                    secTimePassed = secTimePassed + 0.2 / 1000;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private byte[] createMarkRoundingMessage(Boat boat) {
        byte[] body = new byte[messageLengths.get(messageTypes.get("markRounding"))];
        body[0] = 1;
        addFieldToByteArray(body, BOAT_TIMESTAMP,(int) Instant.now().toEpochMilli());
        addFieldToByteArray(body, MARK_ACK, 0); //todo make proper ack
        addFieldToByteArray(body, MARK_RACE_ID, raceId);
        addFieldToByteArray(body, MARK_SOURCE, boat.getId());
        addFieldToByteArray(body, MARK_BOAT_STATUS, boat.getStatus());
        addFieldToByteArray(body, ROUNDING_SIDE, 0); //todo present correct side
        if (course.getCourseOrder().get(boat.getLastPassedMark()).getClass() == Gate.class) {
            addFieldToByteArray(body, MARK_TYPE, 2);
        } else {
            addFieldToByteArray(body, MARK_TYPE, 1);
        }
        addFieldToByteArray(body, MARK_ID, boat.getLastPassedMark()); //todo give marks ids correctly
        return body;
    }

    private byte[] initiliseRaceStatusMessage() {
        byte[] body = new byte[messageLengths.get(messageTypes.get("raceStatus"))];
        addFieldToByteArray(body, STATUS_MESSAGE_VERSION_NUMBER, 2);
        addFieldToByteArray(body, BOAT_TIMESTAMP,(int) Instant.now().toEpochMilli());
        addFieldToByteArray(body, STATUS_RACE_ID, raceId);
        addFieldToByteArray(body, RACE_STATUS, raceStatus.getValue());
        addFieldToByteArray(body, EXPECTED_START_TIME, (int)startTime); //todo, import from race xml
        addFieldToByteArray(body, RACE_COURSE_WIND_DIRECTION, 0x6000); // left for now
        addFieldToByteArray(body, RACE_COURSE_WIND_SPEED, 10); //left at 10knots for now
        addFieldToByteArray(body, NUMBER_OF_BOATS_IN_RACE, boatsInRace.size());
        addFieldToByteArray(body, RACE_TYPE, 2); //fleet race

        return body;


    }

    private byte[] addBoatToRaceStatusMessage(Boat boat, byte[] body){
        addFieldToByteArray(body, STATUS_SOURCE_ID, boat.getId());
        addFieldToByteArray(body, BOAT_STATUS, boat.getStatus());
        addFieldToByteArray(body, LEG_NUMBER, boat.getLastPassedMark() + 1);
        addFieldToByteArray(body, NUMBER_PENALTIES_AWARDED, 0); //todo make penalties a thing
        addFieldToByteArray(body, NUMBER_PENALTIES_SERVED, 0);
        addFieldToByteArray(body, ESTIMATED_TIME_AT_NEXT_MARK, 0); //todo time estimation til next mark/ end of race
        addFieldToByteArray(body, ESTIMATED_TIME_AT_FINISH, 0);

        return body;
    }

    /**
     * Builds a byte array representing a boat location message
     * @param boat the boat that is the subject of the message
     * @param location the boats current location
     * @return a byte array representing the boat location message
     */
    private byte[] createBoatLocationMessage(Boat boat, Coordinate location) {
        byte[] body = initialiseLocationPacket();

        int currentSequenceNumber = boatSequenceNumbers.get(boat);
        boatSequenceNumbers.put(boat, currentSequenceNumber + 1); //increment sequence number

        int lat = (int) Math.round(location.getLat() * Math.pow(2, 31) / 180);
        int lon = (int) Math.round(location.getLon() * Math.pow(2, 31) / 180);
        addFieldToByteArray(body, BOAT_TIMESTAMP,(int) Instant.now().toEpochMilli());
        addFieldToByteArray(body, BOAT_SOURCE_ID, boat.getId());
        addFieldToByteArray(body, BOAT_SEQUENCE_NUM, currentSequenceNumber);
        addFieldToByteArray(body, LATITUDE, lat);
        addFieldToByteArray(body, LONGITUDE, lon);
        addFieldToByteArray(body, HEADING, (int) (heading * Math.pow(2, 16) / 360));
        addFieldToByteArray(body, BOAT_SPEED, (int) (speed * KNOTS_TO_MMS_MULTIPLIER)); //BOAT_SPEED may need to be changed to use SOG
        return body;
    }

    /**
     * Sends an xml message type to the socket including the header, body and CRC
     * @param type subtype of the xml message
     * @param fileName name of the file to send
     */
    private void sendXmlMessage(String type, String fileName){
        byte[] header = createHeader(messageTypes.get("XmlMessage"));
        byte[] xmlBody = generateXmlBody(xmlMessageTypes.get(type), fileName);
        addIntIntoByteArray(header, 13, xmlBody.length,2);
        try {
            outToServer.write(header);
            outToServer.write(xmlBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendCRC(header, xmlBody);
    }

    /**
     * Reads a file as bytes and adds the information about the version, sequence number set to 1, subtype, timestamp,
     * xml sequence number and length then
     * @param subType the integer number of the subtype of the xml message
     * @param fileName the file which to read the bytes from
     * @return a byte array which is the body of the xml message
     */
    private byte[] generateXmlBody(int subType, String fileName) {
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
            addFieldToByteArray(body, XML_SUBTYPE, subType);
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
    private byte[] createHeader(int type) {
        byte[] header = new byte[HEADER_LENGTH];

        header[0] = (byte) 0x47; //first sync byte
        header[1] = (byte) 0x83; //second sync byte
        addFieldToByteArray(header, MESSAGE_TYPE, type);
        addFieldToByteArray(header, HEADER_TIMESTAMP, (int) Instant.now().toEpochMilli());
        addFieldToByteArray(header, HEADER_SOURCE_ID, SOURCE_ID);
        if (type != messageTypes.get("XmlMessage")) {
            addFieldToByteArray(header, MESSAGE_LENGTH, messageLengths.get(type));
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

        byte[] body = new byte[messageLengths.get(messageTypes.get("BoatLocation"))];
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
    private void addFieldToByteArray(byte[] array, AC35StreamField field, int item){
        addIntIntoByteArray(array, field.getStartIndex(), item, field.getLength());
    }

    /**
     * Splits an integer into a few bytes and adds it to a byte array
     * @param array array which to add the int
     * @param start index it start adding
     * @param item item to add
     * @param numBytes number of bytes to split the int into
     */
    private void addIntIntoByteArray(byte[] array, int start, int item, int numBytes){
        for (int i = 0; i < numBytes; i ++) {
            array[start + i] = (byte) (item >> i * 8);
        }
    }

    /**
     * Updates the boat's coordinates by how much it moved in timePassed hours on the course
     * @param timePassed the amount of race hours since the last update
     * @param course the course the boat is racing on
     */
    private Coordinate updateLocation(Boat boat, double timePassed, Course course) {
        if(boat.isFinished()){
            return null;
        }
        boolean finished = false;
        int lastPassedMark = boat.getLastPassedMark();
        speed = boat.getMaxSpeed();
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        CompoundMark nextMark = courseOrder.get(lastPassedMark+1);

        Coordinate currentPosition = boat.getCurrentPosition();
        double distanceGained = timePassed * speed;
        double distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());

        //If boat moves more than the remaining distance in the leg
        while(distanceGained > distanceLeftInLeg && lastPassedMark < courseOrder.size()-1){
            distanceGained -= distanceLeftInLeg;
            //Set boat position to next mark
            currentPosition.setLat(nextMark.getLat());
            currentPosition.setLon(nextMark.getLon());
            lastPassedMark++;
            sendPassMark = true;

            if(lastPassedMark < courseOrder.size()-1){
                heading = course.headingsBetweenMarks(lastPassedMark, lastPassedMark + 1);
                nextMark = courseOrder.get(lastPassedMark+1);
                distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());
            }
        }

        //Check if boat has finished
        if(lastPassedMark == courseOrder.size()-1){
            finished = true;
            boat.setStatus(3); //   finished
            speed = 0;
        } else{
            //Move the remaining distance in leg
            double percentGained = (distanceGained / distanceLeftInLeg);
            double newLat = currentPosition.getLat() + percentGained * (nextMark.getLat() - currentPosition.getLat());
            double newLon = currentPosition.getLon() + percentGained * (nextMark.getLon() - currentPosition.getLon());
            currentPosition.update(newLat, newLon);
        }

        if (finished){
            return null;
        }
        return currentPosition;
    }

    /**
     * Spreads the starting positions of the boats over the start line
     */
    private void setStartingPositions(){
        RaceLine startingLine = course.getStartingLine();
        int spaces = boatsInRace.size();
        double dLat = (startingLine.getEnd2Lat() - startingLine.getEnd1Lat()) / spaces;
        double dLon = (startingLine.getEnd2Lon() - startingLine.getEnd1Lon()) / spaces;
        double curLat = startingLine.getEnd1Lat() + dLat;
        double curLon = startingLine.getEnd1Lon() + dLon;
        for (Boat boat : boatsInRace){
            boat.setPosition(curLat, curLon);
            boat.setHeading(course.headingsBetweenMarks(0, 1));
            boat.getPathCoords().add(new Coordinate(curLat, curLon));
            curLat += dLat;
            curLon += dLon;
        }
    }


}
