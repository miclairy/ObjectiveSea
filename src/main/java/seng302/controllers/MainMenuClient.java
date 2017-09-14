package seng302.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seng302.data.*;
import seng302.data.registration.RegistrationType;
import seng302.data.registration.ServerFullException;
import seng302.utilities.ConnectionUtils;
import seng302.utilities.NoConnectionToServerException;
import seng302.views.AvailableRace;

import java.util.*;

/**
 * Created by lga50 on 7/09/17.
 *
 */
public class MainMenuClient extends Client {
    private ObservableList<AvailableRace> availableRaces = FXCollections.observableArrayList();
    private ArrayList<AvailableRace> receivedRaces = new ArrayList<>();

    public MainMenuClient() throws ServerFullException, NoConnectionToServerException {
        this.packetBuilder = new ClientPacketBuilder();
    }

    @Override
    public void run() {
        while(true){
            try {
                checkForRaces();
                Thread.sleep(5000);
                updateRaces();
                stopDataStreamReader();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof MainMenuClientListener){
            if(arg instanceof AvailableRace){
                receivedRaces.add((AvailableRace) arg);
            }
        }
    }

    /**
     * hands the updating of the available races array, removes
     * races that no longer exist, keeps races that are still running
     */
    public void updateRaces(){
        for(AvailableRace newRace : receivedRaces){
            boolean foundRace = false;
            for(AvailableRace oldRace : availableRaces){
                if(newRace.getIpAddress().equals(oldRace.getIpAddress()) && newRace.getNumBoats() == oldRace.getNumBoats()){
                    foundRace = true;
                }
            }
            if(!foundRace){
                availableRaces.add(newRace);
            }
        }
        Iterator<AvailableRace> iter = availableRaces.iterator();
        while (iter.hasNext()) {
            AvailableRace race = iter.next();
            boolean exists = false;
            for(AvailableRace newRace : receivedRaces){
                if (newRace.getIpAddress().equals(race.getIpAddress()) && newRace.getNumBoats() == race.getNumBoats()){
                    exists = true;
                }
                if(!exists){
                    iter.remove();
                }
            }
        }
    }

    public ObservableList<AvailableRace> getAvailableRaces() {
        return availableRaces;
    }

    /**
     * queries the known VM address for any running games
     */
    public void checkForRaces(){
        receivedRaces.clear();
        setUpDataStreamReader(ConnectionUtils.getVmIpAddress(), ConnectionUtils.getVmPort());
        try {
            manageWaitingConnection();
            RegistrationType regoType = RegistrationType.REQUEST_RUNNING_GAMES;
            this.sender = new ClientSender(clientListener.getSocket());
            System.out.println("Sending request games packet");
            sender.sendToServer(this.packetBuilder.createRegistrationRequestPacket(regoType));
        } catch (NoConnectionToServerException e) {
            System.out.println("Cannot reach server on current address");
        }
    }

    @Override
    protected void setUpDataStreamReader(String serverAddress, int serverPort) {
        this.clientListener = new MainMenuClientListener(serverAddress, serverPort);
        dataStreamReaderThread = new Thread(clientListener);
        dataStreamReaderThread.setName("ClientListener");
        dataStreamReaderThread.start();
        clientListener.addObserver(this);
    }

}
