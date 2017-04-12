package seng302.controllers;


import javafx.animation.AnimationTimer;
import seng302.data.RaceVisionFileReader;
import seng302.models.*;
import seng302.utilities.Config;
import seng302.utilities.TimeUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MockStream implements Runnable {

    List<Boat> boatsInRace;
    double heading = 0;
    double speed;
    Course course;
    Socket clientSocket;

    public MockStream() throws IOException {
        Config.initializeConfig();
        clientSocket = new Socket("127.0.0.1", 2828);
        boatsInRace = RaceVisionFileReader.importStarters(null);
        course = RaceVisionFileReader.importCourse(null);

    }

    @Override
    public void run() {
        double secTimePassed = TimeUtils.convertNanosecondsToSeconds(20258);
        try {
            Boolean notFinished = true;
            while (notFinished) {
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                for (Boat boat : boatsInRace) {
                    notFinished = false;
                    Coordinate location = updateLocation(boat, secTimePassed, course);
                    if (location != null) {
                        notFinished = true;
                        outToServer.writeByte(1);
                        outToServer.writeByte((int) Instant.now().toEpochMilli());
                        outToServer.writeByte(boat.getId());
                        outToServer.writeByte(1);
                        outToServer.writeByte((int) location.getLat());
                        outToServer.writeByte((int) location.getLon());
                        outToServer.writeByte(0);
                        outToServer.writeByte((int) heading);
                        outToServer.writeByte(0);
                        outToServer.writeByte(0);
                        outToServer.writeByte((int) speed);
                        for (int i = 0; i < 10; i++) {
                            outToServer.writeByte(0);
                        }
                    }
                }
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //clientSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        speed = boat.getSpeed();
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        CompoundMark nextMark = courseOrder.get(lastPassedMark+1);

        Coordinate currentPosition = boat.getCurrentPosition();
        double distanceGained = timePassed * boat.getSpeed();
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



}
