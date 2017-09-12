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
    private boolean joinPaneVisible = false;

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
                availableRaces.add((AvailableRace) arg);
            }
        }
    }

    public void updateRaces(){
        ArrayList<AvailableRace> racesToRemove = new ArrayList<>();
        for(AvailableRace race : availableRaces){
            if(!recievedRaces.contains(race)){
                racesToRemove.add(race);
            }
        }

        for(AvailableRace race : racesToRemove){
            availableRaces.remove(race);
        }

        for(AvailableRace race : recievedRaces){
            if(!availableRaces.contains(race)){
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

    public void setJoinPaneVisible(boolean isVisible){
        joinPaneVisible = isVisible;
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
