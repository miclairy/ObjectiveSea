package seng302.controllers;

import seng302.controllers.AbstractServerListener;
import seng302.data.AC35StreamMessage;

import javax.xml.crypto.Data;
import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import static seng302.data.AC35StreamField.HEADER_SOURCE_ID;
import static seng302.data.AC35StreamField.MESSAGE_LENGTH;
import static seng302.data.AC35StreamField.MESSAGE_TYPE;

/**
 * Created by atc60 on 22/09/17.
 */
public class ServerListener extends AbstractServerListener{

    private DataInput dataInput;

    public ServerListener(Socket socket, BufferedInputStream socketData) throws IOException {
        setSocket(socket);
        dataInput = new DataInputStream(socketData);
    }

    @Override
    public void run() {
        while(clientConnected){
            try {
                byte[] header = new byte[HEADER_LENGTH];
                dataInput.readFully(header);

                int messageLength = byteArrayRangeToInt(header, MESSAGE_LENGTH.getStartIndex(), MESSAGE_LENGTH.getEndIndex());
                int messageTypeValue = byteArrayRangeToInt(header, MESSAGE_TYPE.getStartIndex(), MESSAGE_TYPE.getEndIndex());
                int sourceId = byteArrayRangeToInt(header, HEADER_SOURCE_ID.getStartIndex(), HEADER_SOURCE_ID.getEndIndex());
                AC35StreamMessage messageType = AC35StreamMessage.fromInteger(messageTypeValue);

                byte[] body = new byte[messageLength];
                dataInput.readFully(body);
                byte[] crc = new byte[CRC_LENGTH];
                dataInput.readFully(crc);
                if (checkCRC(header, body, crc)) {
                    switch (messageType) {
                        case HOST_GAME_MESSAGE:
                            recordHostGameMessage(body);
                            break;
                        case GAME_CANCEL:
                            removeHostedGame(body);
                            break;
                        case REGISTRATION_REQUEST:
                            parseRegistrationRequestMessage(body);
                            break;
                        case BOAT_ACTION_MESSAGE:
                            if (sourceId != -1) {
                                parseBoatActionMessage(body);
                            }
                            break;
                    }
                }
            } catch (SocketException e) {
                break;
            }catch (IOException e) {
                System.out.println("Server Listener: connection closed");
                clientConnected = false;
            }
        }
        System.out.println("Game Recorder ServerListener Stopped");
    }
}
