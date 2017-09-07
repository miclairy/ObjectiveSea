package seng302.controllers;

import javafx.scene.input.KeyCode;
import seng302.data.BoatAction;
import seng302.data.BoatStatus;
import seng302.data.ClientPacketBuilder;
import seng302.data.ClientSender;
import seng302.data.ClientListener;
import seng302.data.registration.RegistrationResponse;
import seng302.data.registration.RegistrationType;
import seng302.data.registration.ServerFullException;
import seng302.models.Boat;
import seng302.models.ClientOptions;
import seng302.models.Race;
import seng302.utilities.ConnectionUtils;
import seng302.utilities.NoConnectionToServerException;
import seng302.utilities.TimeUtils;

import java.io.IOException;
import java.util.*;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by mjt169 on 18/07/17.
 *
 */
public abstract class Client implements Runnable, Observer {


    private int MAX_CONNECTION_ATTEMPTS = 200;
    private double CONNECTION_TIMEOUT = TimeUtils.secondsToMilliseconds(10.0);
    private int WAIT_MILLISECONDS = 10;

    protected ClientListener clientListener;
    protected ClientPacketBuilder packetBuilder;
    protected ClientSender sender;
    Thread dataStreamReaderThread;
    protected RegistrationResponse serverRegistrationResponse;
    protected int clientID;


    /**
     * Waits for the server to accept the socket connection
     * @throws NoConnectionToServerException if we timeout whilst waiting for the connection
     */
    protected void manageWaitingConnection() throws NoConnectionToServerException {
        int connectionAttempts = 0;
        while(clientListener.getClientSocket() == null) {
            if(clientListener.isHasConnectionFailed()){
                stopDataStreamReader();
                throw new NoConnectionToServerException(true, "Connection Failed. Port number is invalid.");
            }else if(connectionAttempts < MAX_CONNECTION_ATTEMPTS){
                try {
                    Thread.sleep(WAIT_MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connectionAttempts++;
            } else {
                stopDataStreamReader();
                throw new NoConnectionToServerException(false, "Maximum connection attempts exceeded while trying to connect to server. Port or IP may not be valid.");
            }
        }
    }

    /**
     * Waits for a RegistrationResponse to be received from the server and acts upon that response
     * @throws ServerFullException if the Response from the server was received, but the server was full
     * @throws NoConnectionToServerException if we timeout whilst waiting for the response
     */
    protected void manageServerResponse() throws ServerFullException, NoConnectionToServerException {
        double waitTime = 0;
        while (serverRegistrationResponse == null) {
            try {
                Thread.sleep(WAIT_MILLISECONDS);
                waitTime += WAIT_MILLISECONDS;
                if (waitTime > CONNECTION_TIMEOUT) {
                    throw new NoConnectionToServerException(false, "Connection to server timed out while waiting for registration response.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        switch(serverRegistrationResponse.getStatus()) {
            case PLAYER_SUCCESS:
            case SPECTATOR_SUCCESS:
                this.clientID = serverRegistrationResponse.getSourceId();
                break;
            case OUT_OF_SLOTS:
                throw new ServerFullException();
            case GENERAL_FAILURE:
            case GHOST_SUCCESS:
            case TUTORIAL_SUCCESS:
                System.out.println("Client: Server response not understood.");
        }
    }

    protected void setUpDataStreamReader(String serverAddress, int serverPort){
        this.clientListener = new ClientListener(serverAddress, serverPort);
        dataStreamReaderThread = new Thread(clientListener);
        dataStreamReaderThread.setName("ClientListener");
        dataStreamReaderThread.start();
        clientListener.addObserver(this);
    }

    protected void stopDataStreamReader(){
        if(dataStreamReaderThread != null){
            dataStreamReaderThread.stop();
            this.clientListener = null;
            System.out.println("Client: Server not found");
        }
    }

    public void updateVM(Double speedScale, Integer minParticipants, Integer serverPort, String publicIp, int currentCourseIndex){
        byte[] registerGamePacket = packetBuilder.createGameRegistrationPacket(speedScale, minParticipants, serverPort, publicIp, currentCourseIndex);
        System.out.println("Client: Updating VM");
        sender.sendToServer(registerGamePacket);
    }


}
