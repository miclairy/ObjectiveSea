package seng302.controllers;

import seng302.data.*;
import seng302.data.registration.RegistrationResponseStatus;
import seng302.data.registration.RegistrationType;
import seng302.models.Boat;
import seng302.models.Collision;
import seng302.models.Race;
import seng302.models.ServerOptions;
import seng302.utilities.ConnectionUtils;
import seng302.views.AvailableRace;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

import static seng302.data.AC35StreamField.*;
import static seng302.data.AC35StreamField.HOST_GAME_CURRENT_PLAYERS;
import static seng302.data.AC35StreamField.HOST_GAME_REQUIRED_PLAYERS;
import static seng302.data.AC35StreamXMLMessage.BOAT_XML_MESSAGE;
import static seng302.data.AC35StreamXMLMessage.RACE_XML_MESSAGE;
import static seng302.data.AC35StreamXMLMessage.REGATTA_XML_MESSAGE;

/**
 * Created by mjt169 on 18/07/17.
 *
 */
public abstract class Server implements Runnable, Observer {

    protected final double SECONDS_PER_UPDATE = 0.2;

    private RaceUpdater raceUpdater;
    private Thread raceUpdaterThread;
    protected ConnectionManager connectionManager;
    protected ServerPacketBuilder packetBuilder;
    protected ServerOptions options;


    /**
     * Sends a header, body then generates and sends a CRC for that header and body
     * @param packet the packet to send
     */
    protected void sendPacket(byte[] packet) {
        connectionManager.sendToClients(packet);
    }


    public void initiateServerDisconnect() {
        connectionManager.closeAllConnections();
        raceUpdater.stopRunning();
    }

    public void stop() {
    }
}
