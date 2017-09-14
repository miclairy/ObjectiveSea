package seng302.views;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * class to handle the displaying of an available race in the menu
 */
public class AvailableRace {

    private StringProperty mapName;
    private IntegerProperty numBoats;
    private int port;
    private String ipAddress;
    private byte[] packet;

    public AvailableRace(String mapName, Integer numBoats, int port, String ipAddress){
        this.mapName = new SimpleStringProperty(mapName);
        this.numBoats = new SimpleIntegerProperty(numBoats);
        this.port = port;
        this.ipAddress = ipAddress;
    }

    public StringProperty mapNameProperty() { return mapName; }

    public IntegerProperty numBoatsProperty() { return numBoats; }

    public int getPort() { return port; }

    public String getIpAddress() { return ipAddress; }

    public byte[] getPacket() {
        return packet;
    }

    public void setPacket(byte[] packet) {
        this.packet = packet;
    }


    public int getNumBoats() {
        return numBoats.get();
    }

    public void setNumBoats(int numBoats) {
        this.numBoats.set(numBoats);
    }
}
