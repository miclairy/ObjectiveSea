package seng302.controllers;

import seng302.data.ConnectionManager;
import seng302.data.CourseName;
import seng302.data.ServerPacketBuilder;
import seng302.data.registration.RegistrationType;
import seng302.utilities.ConnectionUtils;
import seng302.views.AvailableRace;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

import static seng302.data.registration.RegistrationType.REQUEST_RUNNING_GAMES;

import static seng302.data.AC35StreamField.HOST_GAME_CURRENT_PLAYERS;

/**
 * Created by dda40 on 11/09/17.
 *
 */
public class RaceManagerServer implements Observer {

    private final ServerPacketBuilder packetBuilder;
    private final ConnectionManager connectionManager;
    private ArrayList<AvailableRace> availableRaces = new ArrayList<>();
    private int nextHostID = 0;

    public RaceManagerServer() throws IOException {
        packetBuilder = new ServerPacketBuilder();
        connectionManager = new ConnectionManager(ConnectionUtils.getVmPort(), false);
        connectionManager.addObserver(this);
        System.out.println("_Server: Waiting for races");
        Thread managerThread = new Thread(connectionManager);
        managerThread.setName("Connection Manager");
        managerThread.start();
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
                if (arg.equals(REQUEST_RUNNING_GAMES)) {
                    manageRegistration((ServerListener) observable);
                }
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
                incrementNumberOfBoats(runningRace, race.getNumBoats());
            }
        }
        int raceMapIndex = CourseName.getCourseIntFromName(race.mapNameProperty().getValue());
        if (!updatedRace && raceMapIndex != -1) {
            availableRaces.add(race);
        }
    }

    private void incrementNumberOfBoats(AvailableRace race, int numBoats){
        byte[] packet = race.getPacket();
        for (int i = 0; i < 1; i ++) {
            packet[HOST_GAME_CURRENT_PLAYERS.getStartIndex() + i] = (byte) (numBoats >> i * 8);
        }
    }

    private void removeAvailableRace(Object ipAddress){
        AvailableRace foundRace = null;
        System.out.println("_Server: Number of races " + availableRaces.size());
        for (AvailableRace race : availableRaces) {
            if (race.getIpAddress().equals(ipAddress)) {
                foundRace = race;
            }
        }
        if (foundRace != null) {
            availableRaces.remove(foundRace);
            System.out.println("_Server: removed canceled race: " + foundRace.getIpAddress());
        }
    }

    /**
     * Deal with the different types of registrations clients can attempt to connect with
     * Currently ignores GHOST or TUTORIAL connection attempts.
     * @param serverListener the serverListener with the clients socket
     */
    private void manageRegistration(ServerListener serverListener) {
        connectionManager.addMainMenuConnection(nextHostID, serverListener.getSocket());
        for(AvailableRace race : availableRaces){
            byte[] racePacket = packetBuilder.createGameRegistrationPacket(race.getPacket());
            connectionManager.sendToClient(nextHostID, racePacket);
        }
        nextHostID++;
    }

    /**
     * Starts a new server listener on new thread for which listens to a client
     * @param socket the socket for the client
     */
    protected void startServerListener(Socket socket) throws IOException {
        ServerListener serverListener = new ServerListener(socket);
        Thread serverListenerThread = new Thread(serverListener);
        serverListenerThread.setName("_Server Listener");
        serverListenerThread.start();
        serverListener.addObserver(this);
    }
}
