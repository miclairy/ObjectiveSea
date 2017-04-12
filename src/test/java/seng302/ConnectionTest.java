package seng302;

import com.sun.xml.internal.ws.commons.xmlutil.Converter;
import org.junit.Before;
import org.junit.Test;
import seng302.controllers.ConnectionController;
import seng302.utilities.Config;
import sun.misc.IOUtils;
import sun.nio.ch.IOUtil;

import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.Base64;

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

    @Test
    public void connectionTest() throws IOException {
        setUp();
        boolean decoding = false;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();


        String sentence;
        FileWriter boatFileWriter = new FileWriter("src/main/resources/defaultFiles/Boat.xml");
        FileWriter raceFileWriter = new FileWriter("src/main/resources/defaultFiles/Race.xml");
        BufferedWriter bufferedWriter = null;
        boolean readingBoat = false;
        boolean readingRace = false;
        boolean finishedReading = false;
        int previous = -1;
        int current  = -1;
        while (true){
            current = newStream.read();
            System.out.println(current);
            if (previous == 71 && current == 131) {
                int type = newStream.read();
                System.out.println("type " + type);
                int time = 0;
                for (int j = 0; j < 6; j++) {
                    time += newStream.read();
                    System.out.println("time " + time);
                }

                int sourceId = 0;
                for (int k = 0; k < 4; k++) {
                    sourceId += newStream.read();
                    System.out.println("id " + sourceId);
                }

                int messageLength = newStream.read();
                System.out.println("len 1 " + messageLength);
                messageLength += newStream.read();
                System.out.println("len 2 " + messageLength);
                if (type == 26 || type == 20){
                    int read = 0;
                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    while((sentence = inFromServer.readLine()) != null) { //need to combine 2 bytes to get one int
                        System.out.println(sentence);
                        read += sentence.getBytes().length;
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
                }
            }
            previous = current;
        }






        //clientSocket.close();
    }

}
