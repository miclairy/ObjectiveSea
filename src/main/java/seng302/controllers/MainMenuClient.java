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

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof ClientListener){
            if(arg instanceof AvailableRace){
                availableRaces.add((AvailableRace) arg);
            }
        }
    }

    public ObservableList<AvailableRace> getAvailableRaces() {
        return availableRaces;

    }

    public void checkForRaces(){
        availableRaces.clear();
        setUpDataStreamReader("132.181.14.110", 2828);
        try {
            manageWaitingConnection();
            RegistrationType regoType = RegistrationType.REQUEST_RUNNING_GAMES;
            this.sender = new ClientSender(clientListener.getClientSocket());
            sender.sendToServer(this.packetBuilder.createRegistrationRequestPacket(regoType));
        } catch (NoConnectionToServerException e) {
            System.out.println("Cannot reach server on current address");
        }
    }

    public void setJoinPaneVisible(boolean isVisible){
        joinPaneVisible = isVisible;
    }
}
