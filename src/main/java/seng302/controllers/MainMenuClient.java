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
    private ArrayList<AvailableRace> recievedRaces = new ArrayList<>();

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

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof MainMenuClientListener){
            if(arg instanceof AvailableRace){
                recievedRaces.add((AvailableRace) arg);
            }
        }
    }

    /**
     * hands the updating of the availble races array, removes
     * races that no longer exist, keeps races that are still running
     */
    public void updateRaces(){
        ArrayList<AvailableRace> racesToRemove = new ArrayList<>();
        for(AvailableRace race : availableRaces){
            boolean foundRace = false;
            for(AvailableRace newRace : recievedRaces){
                if(race.getIpAddress().equals(newRace.getIpAddress())){
                    foundRace = true;
                    racesToRemove.add(race);
                }
            }
            if(!foundRace){
                racesToRemove.add(race);
            }
        }

        for(AvailableRace race : racesToRemove){
            availableRaces.remove(race);
        }

        for(AvailableRace race : recievedRaces){
            boolean alreadyExists = false;
            for(AvailableRace oldRace : availableRaces){
                if(race.getIpAddress().equals(oldRace.getIpAddress())){
                    alreadyExists = true;
                }
            }
            if(!alreadyExists){
                availableRaces.add(race);
            }
        }
    }

    public ObservableList<AvailableRace> getAvailableRaces() {
        return availableRaces;

    }

    public void checkForRaces(){
        setUpDataStreamReader(ConnectionUtils.getVmIpAddress(), 2828);
        try {
            manageWaitingConnection();
            RegistrationType regoType = RegistrationType.REQUEST_RUNNING_GAMES;
            this.sender = new ClientSender(clientListener.getSocket());
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
