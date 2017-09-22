package seng302.controllers;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by atc60 on 22/09/17.
 */
public class WebSocketServerListener extends AbstractServerListener {

    private BufferedInputStream socketData;

    public WebSocketServerListener(Socket socket, BufferedInputStream socketData) throws IOException {
        setSocket(socket);
        this.socketData = socketData;
        sendWebSocketResponse();
    }

    @Override
    public void run() {
        while(true){
            try {
                byte[] packet = readPacket();
                System.out.println(Arrays.toString(packet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    protected void sendWebSocketResponse() {
        try{
            String data = new Scanner(socketData, "UTF-8").useDelimiter("\\r\\n\\r\\n").next();
            Matcher get = Pattern.compile("^GET").matcher(data);
            System.out.println("Server: Accepted websocket Connection");
            if (get.find()) {
                Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                match.find();
                byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Sec-WebSocket-Accept: "
                        + DatatypeConverter
                        .printBase64Binary(
                                MessageDigest.getInstance("SHA-1")
                                        .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                                .getBytes("UTF-8")))
                        + "\r\n\r\n")
                        .getBytes("UTF-8");

                socket.getOutputStream().write(response, 0, response.length);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readPacket() throws IOException{
        byte[] key = new byte[4];
        int type = socketData.read();
        int length = socketData.read() - 128;
        System.out.println(length);
        for(int i = 0; i < 4; i++){
            key[i] = (byte)socketData.read();
        }
        byte[] packet = new byte[length];
        int c = 0;
        for(int i = 0; i < length; i++) {
            packet[i] = (byte) (socketData.read() ^ key[c & 0x3]);
            c++;
        }
        return packet;
    }

}
