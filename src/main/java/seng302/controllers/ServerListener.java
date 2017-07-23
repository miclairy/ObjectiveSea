package seng302.controllers;

import seng302.data.AC35StreamMessage;
import seng302.data.Receiver;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import static seng302.data.AC35StreamField.*;

/**
 * Created by mjt169 on 19/07/17.
 */
public class ServerListener extends Receiver implements Runnable{
    private Socket socket;

    ServerListener(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        while(true){
            try {
                DataInput dataInput = new DataInputStream(socket.getInputStream());

                byte[] header = new byte[HEADER_LENGTH];
                dataInput.readFully(header);
                int messageLength = byteArrayRangeToInt(header, MESSAGE_LENGTH.getStartIndex(), MESSAGE_LENGTH.getEndIndex());
                int messageTypeValue = byteArrayRangeToInt(header, MESSAGE_TYPE.getStartIndex(), MESSAGE_TYPE.getEndIndex());
                AC35StreamMessage messageType = AC35StreamMessage.fromInteger(messageTypeValue);

                byte[] body = new byte[messageLength];
                dataInput.readFully(body);
                byte[] crc = new byte[CRC_LENGTH];
                dataInput.readFully(crc);
                if (checkCRC(header, body, crc)) {
                    switch (messageType) {
                        case REGISTRATION_REQUEST:
                            parseRegistrationRequestMessage(body);
                    }
                }
            } catch (SocketException e) {
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseRegistrationRequestMessage(byte[] body) {
        System.out.println("Server: Received Registration Request");
        Integer registrationType = byteArrayRangeToInt(body, REGISTRATION_REQUEST_TYPE.getStartIndex(), REGISTRATION_REQUEST_TYPE.getEndIndex());
        setChanged();
        notifyObservers(registrationType);
    }

    public Socket getSocket() {
        return socket;
    }
}
