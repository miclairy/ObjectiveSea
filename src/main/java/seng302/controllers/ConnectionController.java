package seng302.controllers;

import seng302.utilities.Config;

import java.io.*;
import java.net.Socket;

/**
 * Created by lga50 on 6/04/17.
 *
 */
public class ConnectionController {
    private Socket clientSocket;
    private BufferedReader inFromServer;

    public void setUp(){
        try {
            clientSocket = new Socket(Config.SOURCE_ADDRESS, Config.SOURCE_PORT);
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createXMLFiles() throws Exception {
        String sentence;
        FileWriter boatFileWriter = new FileWriter("src/main/resources/defaultFiles/Boat.xml");
        FileWriter raceFileWriter = new FileWriter("src/main/resources/defaultFiles/Race.xml");
        BufferedWriter bufferedWriter = null;

        boolean readingBoat = false;
        boolean readingRace = false;
        boolean finishedReading = false;
        while((sentence = inFromServer.readLine()) != null && !finishedReading) {
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
        clientSocket.close();
    }
}
