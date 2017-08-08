package seng302.models;

/**
 * Created by mjt169 on 3/08/17.
 */
public class ServerOptions {

    private Integer MAX_PARTICIPANTS = 6;

    private Double speedScale;
    private Integer minParticipants;
    private Integer port;
    private String raceXML;

    /**
     * Constructor with default options
     */
    public ServerOptions(){
        speedScale = 1.0;
        minParticipants = 1;
        port = 2828;
        raceXML = "Race.xml";
    }

    public Double getSpeedScale() {
        return speedScale;
    }

    public String getRaceXML() {
        return raceXML;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getMinParticipants() {
        return minParticipants;
    }

    public void setSpeedScale(Double speedScale) throws IllegalArgumentException {
        if (speedScale > 0) {
            this.speedScale = speedScale;
        } else {
            throw new IllegalArgumentException("Speed scale must be a positive value");
        }
    }

    public void setPort(int port) {
        //TODO use validatePort method in connectionUtils once it is merged in
        this.port = port;
    }

    public void setMinParticipants(int minParticipants) throws IllegalArgumentException {
        if (minParticipants > 0 && minParticipants <= MAX_PARTICIPANTS) {
            this.minParticipants = minParticipants;
        } else {
            throw new IllegalArgumentException(
                    String.format("Minimum number of participants must be between 1 and %d", MAX_PARTICIPANTS));
        }
    }

    public void setRaceXML(String raceXML) {
        this.raceXML = raceXML;
    }
}
