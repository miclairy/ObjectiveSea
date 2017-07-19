package seng302.controllers;

import seng302.data.ClientPacketBuilder;
import seng302.data.ClientSender;
import seng302.data.DataStreamReader;

/**
 * Created by mjt169 on 18/07/17.
 */
public class Client implements Runnable {

    private DataStreamReader dataStreamReader;
    private ClientPacketBuilder packetBuilder;
    private ClientSender sender;

    public Client(DataStreamReader dataStreamReader) {
        this.packetBuilder = new ClientPacketBuilder();
        while(dataStreamReader.getClientSocket() == null) {}
        this.sender = new ClientSender(dataStreamReader.getClientSocket());

        this.sender.sendToServer(this.packetBuilder.createRegistrationRequestPacket(true));
    }


    @Override
    public void run() {

    }
}
