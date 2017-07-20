package seng302.controllers;

import seng302.data.ClientPacketBuilder;
import seng302.data.ClientSender;
import seng302.data.DataStreamReader;
import seng302.models.Boat;
import seng302.models.Race;

import java.util.*;

/**
 * Created by mjt169 on 18/07/17.
 */
public class Client implements Runnable, Observer {

    private DataStreamReader dataStreamReader;
    private ClientPacketBuilder packetBuilder;
    private ClientSender sender;
    private Map<Integer, Boat> potentialCompetitors;

    public Client(DataStreamReader dataStreamReader) {
        this.packetBuilder = new ClientPacketBuilder();
        this.dataStreamReader = dataStreamReader;
        dataStreamReader.addObserver(this);
        while(dataStreamReader.getClientSocket() == null) {}
        this.sender = new ClientSender(dataStreamReader.getClientSocket());

        this.sender.sendToServer(this.packetBuilder.createRegistrationRequestPacket(true));
    }


    @Override
    public void run() {

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
}
