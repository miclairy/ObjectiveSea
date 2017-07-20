package seng302.controllers;

import seng302.data.ClientPacketBuilder;
import seng302.data.ClientSender;
import seng302.data.DataStreamReader;
import seng302.models.Boat;
import seng302.models.Race;
import seng302.utilities.Config;

import java.util.*;

/**
 * Created by mjt169 on 18/07/17.
 */
public class Client implements Runnable, Observer {

    private static Race race;
    private DataStreamReader dataStreamReader;
    private ClientPacketBuilder packetBuilder;
    private ClientSender sender;
    private Map<Integer, Boat> potentialCompetitors;

    public Client() {
        this.packetBuilder = new ClientPacketBuilder();
        setUpDataStreamReader();
        while(dataStreamReader.getClientSocket() == null) {}
        this.sender = new ClientSender(dataStreamReader.getClientSocket());
        this.sender.sendToServer(this.packetBuilder.createRegistrationRequestPacket(true));
    }

    private void setUpDataStreamReader(){
        this.dataStreamReader = new DataStreamReader(Config.SOURCE_ADDRESS, Config.SOURCE_PORT);
        Thread dataStreamReaderThread = new Thread(dataStreamReader);
        dataStreamReaderThread.start();
        dataStreamReader.addObserver(this);
    }

    @Override
    public void run() {
        waitForRace();
    }

    /**
     * Waits for the race to be able to be read in
     */
    public void waitForRace(){
        while(dataStreamReader.getRace() == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        race = dataStreamReader.getRace();
    }


    @Override
    public void update(Observable o, Object arg) {
        if (o == dataStreamReader) {
            if (arg instanceof ArrayList) {
                ArrayList<Boat> boats = (ArrayList<Boat>) arg;
                potentialCompetitors = new HashMap<>();
                for (Boat boat : boats) {
                    potentialCompetitors.put(boat.getId(), boat);
                }
            }
            if (arg instanceof Race) {
                Race newRace = (Race) arg;
                Race oldRace = dataStreamReader.getRace();
                for (int newId : newRace.getCompetitorIds()) {
                    if (!oldRace.getCompetitorIds().contains(newId)) {
                        oldRace.addCompetitor(potentialCompetitors.get(newId));
                    }
                }
            }
        }
    }

    public static Race getRace() {
        return race;
    }


}
