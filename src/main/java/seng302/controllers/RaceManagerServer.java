package seng302.controllers;

import seng302.data.ConnectionManager;
import seng302.data.ServerPacketBuilder;
import seng302.data.registration.RegistrationResponseStatus;
import seng302.data.registration.RegistrationType;
import seng302.models.ServerOptions;
import seng302.views.AvailableRace;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * Created by dda40 on 11/09/17.
 *
 */
public class RaceManagerServer extends Server {

    private Map<AvailableRace, byte[]> availableRaces = new HashMap<>();
    private int nextViewerID = 0;

    public RaceManagerServer(ServerOptions options) throws IOException {
        this.options = options;
        packetBuilder = new ServerPacketBuilder();
        connectionManager = new ConnectionManager(2828);
        connectionManager.addObserver(this);
    }

    /**
     * Sends all the data to the socket while the boats have not all finished.
     */
    @Override
    public void run() throws NullPointerException {
        System.out.println("Server: Waiting for races");
        while (options.alwaysRerun()) {
            try {
                Thread managerThread = new Thread(connectionManager);
                managerThread.setName("Connection Manager");
                managerThread.start();
                Thread.sleep((long) (SECONDS_PER_UPDATE * 1000 / options.getSpeedScale()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Server: Shutting Down");
        connectionManager.closeAllConnections();
    }

    @Override
    public void update(Observable observable, Object arg) {
        if(observable instanceof ServerListener){
            if(arg instanceof String){
                AvailableRace foundRace = null;
                System.out.println("Races size: " + availableRaces.size());
                for(AvailableRace race : availableRaces.keySet()){
                    if(race.getIpAddress().equals((String) arg)){
                        foundRace = race;
                    }
                }
                if(foundRace != null){
                    availableRaces.remove(foundRace);
                    System.out.println("Server: removed canceled race: " + foundRace.getIpAddress());
                }
            }else{
                availableRaces.putAll((HashMap<AvailableRace, byte[]>) arg);
            }
        }
        if(arg instanceof RegistrationType){
            manageRegistration((ServerListener) observable, (RegistrationType) arg);
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
                System.out.println("Server: Client requesting games");
                connectionManager.addConnection(nextViewerID, serverListener.getSocket());
                for(byte[] race : availableRaces.values()){
                    byte[] racePacket = packetBuilder.createGameRegistrationPacket(race);
                    connectionManager.sendToClient(nextViewerID, racePacket);
                }
                break;
        }
    }

    /**
     * Starts a new server listener on new thread for which listens to a client
     * @param socket the socket for the client
     */
    protected void startServerListener(Socket socket){
        ServerListener serverListener = new ServerListener(socket);
        Thread serverListenerThread = new Thread(serverListener);
        serverListenerThread.setName("Server Listener");
        serverListenerThread.start();
        serverListener.addObserver(this);
    }
}
