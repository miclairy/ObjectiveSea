package seng302.controllers;


import seng302.data.RaceVisionFileReader;
import seng302.models.*;
import seng302.utilities.Config;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class MockStream implements Runnable {

    private List<Boat> boatsInRace;
    private double heading = 0;
    private double speed;
    private Course course;
    private Socket clientSocket;
    private DataOutputStream outToServer;
    private final int SOURCE_ID = 28;
    private final int HEADER_LENGTH = 15;
    private final int BOAT_LOCATION_LENGTH = 56;
    private Map<String, Integer> messageTypes = new HashMap<>();
    private Map<String, Integer> xmlMessageTypes = new HashMap<>();
    private Map<Integer, Integer> xmlSequenceNumber = new HashMap<>();
    private Map<Boat, Integer> boatSequenceNumbers = new HashMap<>();

    public MockStream(int port) throws IOException {
        Config.initializeConfig();
        clientSocket = new Socket("127.0.0.1", port);
        boatsInRace = RaceVisionFileReader.importStarters(null);
        course = RaceVisionFileReader.importCourse(null);
        setStartingPositions();
        messageTypes.put("XmlMessage", 26);
        messageTypes.put("BoatLocation", 37);
        xmlMessageTypes.put("Race", 6);
        xmlMessageTypes.put("Boat", 7);
        xmlSequenceNumber.put(6, 0);
        xmlSequenceNumber.put(7, 0);

        for (Boat boat: boatsInRace){
            boatSequenceNumbers.put(boat, boat.getId());
        }
    }

    @Override
    public void run() {
        double secTimePassed = 0;
        try {
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            sendXmlMessage("Race", "race.xml");
            sendXmlMessage("Boat", "boat.xml");

            Boolean notFinished = true;
            byte[] body = initialiseLocationPacket();
            while (notFinished) {
                for (Boat boat : boatsInRace) {
                    notFinished = false;
                    Coordinate location = updateLocation(boat, secTimePassed, course);
                    int lat = (int) Math.round(location.getLat() * Math.pow(2, 31) / 180);
                    int lon = (int) Math.round(location.getLon() * Math.pow(2, 31) / 180);
                    if (location != null) {
                        notFinished = true;
                        byte[] header = createHeader(messageTypes.get("BoatLocation"));
                        outToServer.write(header);

                        body = addIntIntoByteArray(body, 1, (int) Instant.now().toEpochMilli(),6);
                        body = addIntIntoByteArray(body, 7, boat.getId(), 4);
                        body = addIntIntoByteArray(body, 10, boatSequenceNumbers.get(boat),4);
                        boatSequenceNumbers.put(boat, boatSequenceNumbers.get(boat) + 1);
                        body = addIntIntoByteArray(body, 16, lat, 4);
                        body = addIntIntoByteArray(body, 20, lon, 4);
                        body = addIntIntoByteArray(body, 28, (int) heading, 2);
                        // multiplied by 514.444 to convert knots to mm/s
                        body = addIntIntoByteArray(body, 33, (int) (speed * 514.444), 2); //change start to 37 instead to move to SOG place?
                        outToServer.write(body);
                        sendCRC(header, body);
                    }
                }
                try {
                    Thread.sleep((long) 0.2 * 1000);
                    secTimePassed = secTimePassed + 0.2 / 1000;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            clientSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendXmlMessage(String type, String fileName){
        byte[] header = createHeader(messageTypes.get("XmlMessage"));
        byte[] xmlBody = generateXmlBody(xmlMessageTypes.get(type), fileName);
        header = addIntIntoByteArray(header, 13, xmlBody.length,2);
        try {
            outToServer.write(header);
            outToServer.write(xmlBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendCRC(header, xmlBody);
    }

    private byte[] generateXmlBody(int subType, String fileName) {
        String raceStrPath = new File("src/main/resources/defaultFiles/" + fileName).getAbsolutePath();
        Path racePath = Paths.get(raceStrPath);

        try {
            byte[] bodyContent = Files.readAllBytes(racePath);
            byte[] body = new byte[14 + bodyContent.length];
            body[0] = 1;
            body = addIntIntoByteArray(body, 1, 1, 2);
            body = addIntIntoByteArray(body, 3, (int) Instant.now().toEpochMilli(), 6);
            body[10] = (byte) subType;
            xmlSequenceNumber.put(subType, xmlSequenceNumber.get(subType) + 1);
            body = addIntIntoByteArray(body, 11, xmlSequenceNumber.get(subType), 2);
            body = addIntIntoByteArray(body, 12, bodyContent.length, 2);
            for (int i = 0; i < body.length - 14; i++){
                body[i + 14] = bodyContent[i];
            }
            return body;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private byte[] createHeader(int type) {

        byte[] header = new byte[HEADER_LENGTH];
        header[0] = 0x47;
        header[1] = (byte) 0x83; //comes out as -125 as java has signed bytes of the range -127 to 127 todo: change so it is within the range
        header[2] = (byte) type;
        header = addIntIntoByteArray(header, 3, (int) Instant.now().toEpochMilli(),6);
        header = addIntIntoByteArray(header, 9, SOURCE_ID,4); //source id
        if (type == messageTypes.get("BoatLocation")) {
            header = addIntIntoByteArray(header, 13, BOAT_LOCATION_LENGTH, 2); //message length
        }
        return header;
    }

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
            outToServer.write(addIntIntoByteArray(new byte[CRC_LENGTH], 0, (int) crc.getValue(), CRC_LENGTH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] initialiseLocationPacket() {

        byte[] body = new byte[BOAT_LOCATION_LENGTH];
        body[0] = (byte) 1;
        body[11] = (byte) 1;
        body[15] = (byte) 1;
        body[24] = (byte) 0;
        return body;
    }

    private byte[] addIntIntoByteArray(byte[] array, int start, int item, int numBytes){
        for (int i = 0; i < numBytes; i ++) {
            array[start + i] = (byte) (item >> i * 8);
        }

        return array;
    }


    /**
     * Updates the boat's coordinates by how much it moved in timePassed hours on the course
     * @param timePassed the amount of race hours since the last update
     * @param course the course the boat is racing on
     */
    public Coordinate updateLocation(Boat boat, double timePassed, Course course) {
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

            if(lastPassedMark < courseOrder.size()-1){
                heading = course.headingsBetweenMarks(lastPassedMark, lastPassedMark + 1);
                nextMark = courseOrder.get(lastPassedMark+1);
                distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());
            }
        }

        //Check if boat has finished
        if(lastPassedMark == courseOrder.size()-1){
            finished = true;
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
    public void setStartingPositions(){
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
