package seng302.controllers;

import com.sun.javafx.UnmodifiableArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import seng302.data.*;
import seng302.data.registration.RegistrationResponse;
import seng302.data.registration.RegistrationType;
import seng302.data.registration.ServerFullException;
import seng302.models.Boat;
import seng302.models.ClientOptions;
import seng302.models.Race;
import seng302.utilities.NoConnectionToServerException;
import seng302.utilities.TimeUtils;
import seng302.views.AvailableRace;

import java.util.*;

/**
 * Created by lga50 on 7/09/17.
 *
 */
public class MainMenuClient extends Client {
    private ObservableList<AvailableRace> availableRaces = FXCollections.observableArrayList();

    public MainMenuClient() throws ServerFullException, NoConnectionToServerException {
        this.packetBuilder = new ClientPacketBuilder();
        setUpDataStreamReader("localhost", 2828);
        try {
            manageWaitingConnection();
            RegistrationType regoType = RegistrationType.REQUEST_RUNNING_GAMES;
            this.sender = new ClientSender(clientListener.getClientSocket());
            sender.sendToServer(this.packetBuilder.createRegistrationRequestPacket(regoType));
        } catch (NoConnectionToServerException e) {
            System.out.println("Cannot reach server on current address");
        }
    }

    @Override
    public void run() {
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof ClientListener){
            if(arg instanceof AvailableRace){
                if(availableRaces.size() == 0){
                    availableRaces.add((AvailableRace) arg);
                }

                boolean alreadyInList = false;
                for(AvailableRace race : availableRaces){
                    if(Objects.equals(race.getIpAddress(), ((AvailableRace) arg).getIpAddress())){
                        alreadyInList = true;
                    }
                }
                if(!alreadyInList){
                    availableRaces.add((AvailableRace) arg);
                }
            }
        }
    }

    public ObservableList<AvailableRace> getAvailableRaces() {
        ObservableList<AvailableRace> races = FXCollections.observableArrayList();
        races.addAll(availableRaces);
        return races;

    }

    public void checkForRaces(){
        RegistrationType regoType = RegistrationType.REQUEST_RUNNING_GAMES;
        try {
            this.sender = new ClientSender(clientListener.getClientSocket());
            sender.sendToServer(this.packetBuilder.createRegistrationRequestPacket(regoType));
        } catch (NullPointerException e){
            System.out.println("Server is unreachable");
        }
    }
}
