package seng302.controllers;

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

import java.util.*;

/**
 * Created by lga50 on 7/09/17.
 *
 */
public class MainMenuClient extends Client {
    private int MAX_CONNECTION_ATTEMPTS = 200;
    private double CONNECTION_TIMEOUT = TimeUtils.secondsToMilliseconds(10.0);
    private int WAIT_MILLISECONDS = 10;

    Thread dataStreamReaderThread;
    private RegistrationResponse serverRegistrationResponse;

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
    }
}
