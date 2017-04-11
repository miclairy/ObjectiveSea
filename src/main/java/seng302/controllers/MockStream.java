package seng302.controllers;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

public class MockStream implements Runnable {


    @Override
    public void run() {

        try {
            int i = 0;
                Socket clientSocket = new Socket("localhost", 2828);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                while(i < 10000) {
                    outToServer.writeBytes("Heeelllloooo" + i + '\n');
                    i++;
                }
            //clientSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
