package seng302.controllers;

import seng302.data.ClientPacketBuilder;
import seng302.data.ClientSender;
import seng302.data.DataStreamReader;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by mjt169 on 18/07/17.
 * 
 */
public class Client implements Runnable, Observer {

    private DataStreamReader dataStreamReader;
    private ClientPacketBuilder packetBuilder;
    private ClientSender sender;
    private UserInputController userInputController;

    public Client(DataStreamReader dataStreamReader) {
        packetBuilder = new ClientPacketBuilder();
        while(dataStreamReader.getClientSocket() == null) {}
        sender = new ClientSender(dataStreamReader.getClientSocket());
        sender.sendToServer(packetBuilder.createRegistrationRequestPacket(true));
    }


    @Override
    public void run() {

    }

    @Override
    public void update(Observable o, Object arg) {
        sender.sendToServer(packetBuilder.createBoatCommandPacket(userInputController.getCommandInt()));
    }

    public void setUserInputController(UserInputController userInputController) {
        this.userInputController = userInputController;
    }
}
