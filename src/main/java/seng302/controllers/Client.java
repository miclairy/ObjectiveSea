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
    private int clientID;

    public Client(DataStreamReader dataStreamReader) {
        packetBuilder = new ClientPacketBuilder();
        dataStreamReader.addObserver(this);
        while(dataStreamReader.getClientSocket() == null) {}
        sender = new ClientSender(dataStreamReader.getClientSocket());
        sender.sendToServer(packetBuilder.createRegistrationRequestPacket(true));
    }


    @Override
    public void run() {

    }


    /**
     * observing UserInputController and dataStreamReader
     * @param o
     * @param clientID clients id
     */
    @Override
    public void update(Observable o, Object clientID) {
        if (clientID == null){
            sender.sendToServer(packetBuilder.createBoatCommandPacket(userInputController.getCommandInt(), this.clientID));
        } else {
            this.clientID = (int) clientID;
        }
    }





    public void setUserInputController(UserInputController userInputController) {
        this.userInputController = userInputController;
    }
}
