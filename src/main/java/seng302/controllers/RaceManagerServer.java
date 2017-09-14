package seng302.controllers;

import seng302.data.ConnectionManager;
import seng302.data.CourseName;
import seng302.data.ServerPacketBuilder;
import seng302.data.registration.RegistrationType;
import seng302.models.ServerOptions;
import seng302.utilities.ConnectionUtils;
import seng302.views.AvailableRace;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Observable;

import static seng302.data.AC35StreamField.HOST_GAME_CURRENT_PLAYERS;

/**
 * Created by dda40 on 11/09/17.
 *
 */
public class RaceManagerServer extends Server {

    private ArrayList<AvailableRace> availableRaces = new ArrayList();
    private int nextViewerID = 0;

    public RaceManagerServer(ServerOptions options) throws IOException {
        this.options = options;
        packetBuilder = new ServerPacketBuilder();
        connectionManager = new ConnectionManager(ConnectionUtils.getVmPort(), false);
        connectionManager.addObserver(this);
    }

    /**
     * Sends all the data to the socket while the boats have not all finished.
     */
    @Override
    public void run() throws NullPointerException {
        System.out.println("Server: Waiting for races");
        Thread managerThread = new Thread(connectionManager);
        managerThread.setName("Connection Manager");
        managerThread.start();
        while (options.alwaysRerun()) {
            try {
                Thread.sleep((long) (SECONDS_PER_UPDATE * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Server: Shutting Down");
        connectionManager.closeAllConnections();
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (observable.equals(connectionManager)) {
            if (arg instanceof Socket) {
                try {
                    startServerListener((Socket) arg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if(observable instanceof ServerListener){
            if(arg instanceof String) {
                removeAvailableRace(arg);
            } else if (arg instanceof RegistrationType) {
                RegistrationType rego = (RegistrationType) arg;
                manageRegistration((ServerListener) observable, rego);
            } else if (arg instanceof AvailableRace) {
                updateAvailableRace(((AvailableRace) arg));
            }
        }
    }

    private void updateAvailableRace(AvailableRace race){
        boolean updatedRace = false;
        for (AvailableRace runningRace : availableRaces){
            if (Objects.equals(runningRace.getIpAddress(), race.getIpAddress())){
                System.out.println("Updating running race");
                updatedRace = true;
                runningRace.setNumBoats(runningRace.getNumBoats() + 1);
                incrementNumberOfBoats(runningRace);
            }
        }
        int raceMapIndex = CourseName.getCourseIntFromName(race.mapNameProperty().getValue());
        if (!updatedRace && raceMapIndex != -1) {
            availableRaces.add(race);
        }
    }

    private void incrementNumberOfBoats(AvailableRace race){
        byte[] packet = race.getPacket();
        System.out.println(Arrays.toString(packet));
        for (int i = 0; i < 1; i ++) {
            packet[HOST_GAME_CURRENT_PLAYERS.getStartIndex() + i] = (byte) (race.getNumBoats() >> i * 8);
        }
    }

    private void removeAvailableRace(Object ipAddress){
        AvailableRace foundRace = null;
        System.out.println("Races size: " + availableRaces.size());
        for (AvailableRace race : availableRaces) {
            if (race.getIpAddress().equals(ipAddress)) {
                foundRace = race;
            }
        }
        if (foundRace != null) {
            availableRaces.remove(foundRace);
            System.out.println("Server: removed canceled race: " + foundRace.getIpAddress());
        }
    }

    /**
     * Deal with the different types of registrations clients can attempt to connect with
     * Currently ignores GHOST or TUTORIAL connection attempts.
     * @param serverListener the serverListener with the clients socket
     * @param registrationType the type of registration being attempted
     */
    private void manageRegistration(ServerListener serverListener, RegistrationType registrationType) {
        switch (registrationType) {
            case REQUEST_RUNNING_GAMES:
                connectionManager.addMainMenuConnection(nextViewerID, serverListener.getSocket());
                for(AvailableRace race : availableRaces){
                    byte[] racePacket = packetBuilder.createGameRegistrationPacket(race.getPacket());
                    connectionManager.sendToClient(nextViewerID, racePacket);
                }
                nextViewerID++;
                break;
        }
    }

    /**
     * Starts a new server listener on new thread for which listens to a client
     * @param socket the socket for the client
     */
    protected void startServerListener(Socket socket) throws IOException {
        ServerListener serverListener = new ServerListener(socket);
        Thread serverListenerThread = new Thread(serverListener);
        serverListenerThread.setName("Server Listener");
        serverListenerThread.start();
        serverListener.addObserver(this);
    }
}
