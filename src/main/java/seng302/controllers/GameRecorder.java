package seng302.controllers;

import seng302.controllers.listeners.AbstractServerListener;
import seng302.controllers.listeners.ServerListener;
import seng302.data.ConnectionManager;
import seng302.data.CourseName;
import seng302.data.ServerPacketBuilder;
import seng302.data.registration.RegistrationType;
import seng302.utilities.ConnectionUtils;
import seng302.utilities.MathUtils;
import seng302.views.AvailableRace;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

import static seng302.data.registration.RegistrationType.REQUEST_RUNNING_GAMES;

import static seng302.data.AC35StreamField.HOST_GAME_CURRENT_PLAYERS;

/**
 * Created by dda40 on 11/09/17.
 *
 */
public class GameRecorder implements Observer {

    private final ServerPacketBuilder packetBuilder;
    private final ConnectionManager connectionManager;
    private ArrayList<AvailableRace> availableRaces = new ArrayList<>();
    private int nextHostID = 0;
    private Set<Socket> sockets = new HashSet<>();
    private Thread serverListenerThread = null;
    private HashMap<Integer, AvailableRace> availablePartyGames = new HashMap<>();

    public GameRecorder() throws IOException {
        packetBuilder = new ServerPacketBuilder();
        connectionManager = new ConnectionManager(ConnectionUtils.getGameRecorderPort(), false);
        connectionManager.addObserver(this);
        System.out.println("Server: Waiting for races");
        Thread managerThread = new Thread(connectionManager);
        managerThread.setName("Connection Manager");
        managerThread.start();
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (observable.equals(connectionManager)) {
            if (arg instanceof Socket) {
                Socket socket = (Socket) arg;
                sockets.add(socket);
                try {
                    AbstractServerListener serverListener = ServerListener.createServerListener((Socket) arg);
                    startServerListener(serverListener);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else if(observable instanceof AbstractServerListener){
            if (arg instanceof RegistrationType) {
                if (arg.equals(REQUEST_RUNNING_GAMES)) {
                    respondToRequestForGames((AbstractServerListener) observable);
                }
            } else if (arg instanceof AvailableRace) {
                updateAvailableRace(((AvailableRace) arg));
            }
        }
    }

    /**
     * Runs through the entire list of available races, updating the ones that have had changes
     * @param newRace the new available race
     */
    private void updateAvailableRace(AvailableRace newRace){
        if (newRace.isDeleted()) {
            removeAvailableRace(newRace);
            return;
        }
        if (newRace.isPartyGame()) {
            updatePartyGame(newRace);
        } else {
            updateNormalRace(newRace);
        }
    }

    /**
     * Adds or updates a party game to the availablePartyGames hashmap
     * @param newRace the race to add or update
     */
    private void updatePartyGame(AvailableRace newRace) {
        boolean updatedRace = updateRaceIfExists(newRace, availablePartyGames.values());
        if (!updatedRace) {
            Integer code = MathUtils.generateFourDigitPartyCode();
            while (availablePartyGames.keySet().contains(code)) {
                code = MathUtils.generateFourDigitPartyCode();
            }
            newRace.setCode(code);
            availablePartyGames.put(code, newRace);
        }

    }

    /**
     * Iterate through the provided available races and determine if it matches the given race.
     * Update if we find a match.
     * @param newRace the race to match
     * @param availableRaces the Collection to search through
     * @return true if we updated a race, false otherwise
     */
    private boolean updateRaceIfExists(AvailableRace newRace, Collection<AvailableRace> availableRaces) {
        boolean updatedRace = false;
        for (AvailableRace runningRace : availableRaces){
            if (runningRace.equals(newRace)){
                System.out.println("Game Recorder: Updating running race");
                updatedRace = true;
                updateNumberOfBoats(runningRace, newRace.getNumBoats());
            }
        }
        return updatedRace;
    }

    /**
     * Adds or updates a normal game to the availableRaces list
     * @param newRace the race to add or update
     */
    private void updateNormalRace(AvailableRace newRace) {
        boolean updatedRace = updateRaceIfExists(newRace, availableRaces);
        int raceMapIndex = CourseName.getCourseIntFromName(newRace.mapNameProperty().getValue());
        if (!updatedRace && raceMapIndex != -1) {
            System.out.println("Game Recorder: Recording new server");
            updateNumberOfBoats(newRace, newRace.getNumBoats());
            availableRaces.add(newRace);
        }
    }

    /**
     * changes the number of boats in a known race to be stored on the vm
     * @param race the new race
     * @param numBoats the new number of boats in the race
     */
    private void updateNumberOfBoats(AvailableRace race, int numBoats){
        byte[] packet = race.getPacket();
        packet[HOST_GAME_CURRENT_PLAYERS.getStartIndex()] = (byte) numBoats;
    }

    /**
     * removes a race with a specific IP address
     * @param deletedRace dummy race with matching IP and port to remove
     */
    private void removeAvailableRace(AvailableRace deletedRace){
        Boolean raceDeleted = false;
        for (AvailableRace race : availableRaces) {
            if (race.equals(deletedRace)) {
                raceDeleted = true;
                availableRaces.remove(race);
                System.out.println("Game Recorder: removed canceled race: " + race.getIpAddress());
                break;
            }
        }
        if (!raceDeleted) {
            for (AvailableRace race : availablePartyGames.values()) {
                if (race.equals(deletedRace)) {
                    availablePartyGames.remove(race.getCode());
                    System.out.println("Game Recorder: removed canceled party game race: " + race.getIpAddress());
                    break;
                }
            }
        }
    }

    /**
     * Respond to a request for running games
     * @param serverListener the serverListener with the clients socket
     */
    private void respondToRequestForGames(AbstractServerListener serverListener) {
        connectionManager.addMainMenuConnection(nextHostID, serverListener.getSocket());
        for(AvailableRace race : availableRaces) {
            byte[] racePacket = packetBuilder.createGameRegistrationPacket(race.getPacket());
            connectionManager.sendToClient(nextHostID, racePacket);
        }
        nextHostID++;
    }

    /**
     * Starts a new server listener on new thread for which listens to a client
     * @param serverListener the serverListener for the client socket
     */
    protected void startServerListener(AbstractServerListener serverListener) throws IOException {
        serverListenerThread = new Thread(serverListener);
        serverListenerThread.setName("Game Recorder Listener");
        serverListenerThread.start();
        serverListener.addObserver(this);
    }
}
