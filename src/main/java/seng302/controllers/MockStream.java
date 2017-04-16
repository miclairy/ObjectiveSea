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
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class MockStream implements Runnable {

    private List<Boat> boatsInRace;
    private double heading = 0;
    private double speed;
    private Course course;
    private Socket clientSocket;
    private DataOutputStream outToServer;

    public MockStream(int port) throws IOException {
        Config.initializeConfig();
        clientSocket = new Socket("127.0.0.1", port);
        boatsInRace = RaceVisionFileReader.importStarters(null);
        course = RaceVisionFileReader.importCourse(null);
        setStartingPositions();
    }

    @Override
    public void run() { //TODO remember to go back and change sequence numbers appropriately
        double secTimePassed = 0;
        try {
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            byte[] raceHeader = createHeader(26);
            byte[] raceXMLBody = generateRaceBody();
            raceHeader = addIntIntoByteArray(raceHeader, 13, raceXMLBody.length,2);
            outToServer.write(raceHeader);
            outToServer.write(raceXMLBody);
            sendCRC(raceHeader, raceXMLBody);

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
                        byte[] header = createHeader(37);
                        outToServer.write(header);

                        body = addIntIntoByteArray(body, 1, (int) Instant.now().toEpochMilli(),6);
                        body = addIntIntoByteArray(body, 7, boat.getId(), 4);
                        body = addIntIntoByteArray(body, 16, lat, 4);
                        body = addIntIntoByteArray(body, 20, lon, 4);
                        body = addIntIntoByteArray(body, 28, (int) heading, 2);
                        body = addIntIntoByteArray(body, 33, (int) speed, 2); //change to 37 instead to move to SOG place?
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

    private byte[] generateRaceBody() {
        String raceStrPath = new File("src/main/resources/defaultFiles/race.xml").getAbsolutePath();
        Path racePath = Paths.get(raceStrPath);

        try {
            byte[] raceBodyContent = Files.readAllBytes(racePath);
            byte[] raceBody = new byte[14 + raceBodyContent.length];
            raceBody[0] = 1;
            raceBody = addIntIntoByteArray(raceBody, 1, 1, 2);
            raceBody = addIntIntoByteArray(raceBody, 3, (int) Instant.now().toEpochMilli(), 6);
            raceBody[10] = 6;
            raceBody[11] = 1;
            raceBody = addIntIntoByteArray(raceBody, 12, raceBodyContent.length, 2);
            for (int i = 0; i < raceBody.length - 14; i++){
                raceBody[i + 14] = raceBodyContent[i];
            }
            return raceBody;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] createHeader(int type) {
        byte[] header = new byte[15];
        header[0] = 0x47;
        header[1] = (byte) 0x83;
        header[2] = (byte) type;
        header = addIntIntoByteArray(header, 3, (int) Instant.now().toEpochMilli(),6);
        header = addIntIntoByteArray(header, 9, 28,4); //source id
        if (type == 37) {
            header = addIntIntoByteArray(header, 13, 56, 2); //message length
        }
        return header;
    }

    private void sendCRC(byte[] header, byte[] body){
        Checksum crc = new CRC32();
        byte[] toCRC = new byte[header.length + body.length];
        for (int i = 0; i < toCRC.length; i ++){
            if (i < 15) {
                toCRC[i] = header[i];
            } else {
                toCRC[i] = body[i - 15];
            }
        }
        crc.update(toCRC, 0, toCRC.length);
        try {
            outToServer.write(addIntIntoByteArray(new byte[4], 0, (int) crc.getValue(), 4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] initialiseLocationPacket() {
        byte[] body = new byte[56];
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
