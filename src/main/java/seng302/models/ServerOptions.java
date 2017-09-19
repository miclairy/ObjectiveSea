package seng302.models;

import seng302.data.RaceVisionXMLParser;
import seng302.utilities.ConnectionUtils;

/**
 * Created by mjt169 on 3/08/17.
 *
 */
public class ServerOptions {

    private final String DEFAULT_COURSE = "AC35-course.xml";
    private final Double DEFAULT_SPEED = 15.0;
    private final Integer DEFAULT_PORT = 2828;
    private Double speedScale;

    private Integer minParticipants;
    private Integer maxParticipants = 6;
    private Integer port;
    private String raceXML;
    private boolean isTutorial;
    private Integer numRacesToRun;
    private AIDifficulty aiDifficulty = AIDifficulty.NO_AI;
    private boolean runRaceManager;

    /**
     * Constructor with default options
     */
    public ServerOptions(){
        speedScale = DEFAULT_SPEED;
        minParticipants = 1;
        port = DEFAULT_PORT;
        raceXML = DEFAULT_COURSE;
        isTutorial = false;
        numRacesToRun = 1;
        runRaceManager = false;
    }

    /**
     * Constructor for custom host game
     */
    public ServerOptions(Double speedScale, int minParticipants){
        this.speedScale = speedScale;
        this.minParticipants = minParticipants;
        port = DEFAULT_PORT;
        raceXML = DEFAULT_COURSE;
        isTutorial = false;
        numRacesToRun = 1;
        runRaceManager = false;
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
        if (ConnectionUtils.validatePort(port)) {
            this.port = port;
        } else {
             throw new IllegalArgumentException("Invalid port number");
        }
    }

    public void setMinParticipants(int minParticipants) throws IllegalArgumentException {
        if (minParticipants > 0 && minParticipants <= maxParticipants) {
            this.minParticipants = minParticipants;
        } else {
            throw new IllegalArgumentException(
                    String.format("Minimum number of participants must be between 1 and %d", maxParticipants));
        }
    }

    public void setRaceXML(String raceXML) {
        RaceVisionXMLParser parser = new RaceVisionXMLParser();
        if (parser.checkFileExists(raceXML)) {
            this.raceXML = raceXML;
        } else {
            throw new IllegalArgumentException("Could not find a map that matches that name");
        }
    }

    public void setTutorial(boolean tutorial) {
        this.isTutorial = tutorial;
    }

    public boolean isTutorial() {
        return this.isTutorial;
    }

    public Integer getNumRacesToRun() {
        return numRacesToRun;
    }

    public void setNumRacesToRun(Integer numRacesToRun) {
        this.numRacesToRun = numRacesToRun;
    }

    /**
     * @return true if the server has been configured to always restart at the end of a race
     */
    public Boolean alwaysRerun() {
        return numRacesToRun == -1;
    }

    public boolean isRunRaceManager() {
        return runRaceManager;
    }

    public void setRunRaceManager(boolean runRaceManager) {
        this.runRaceManager = runRaceManager;
    }

    public void setAiDifficulty(AIDifficulty AIDifficulty) {
        this.aiDifficulty = AIDifficulty;
    }

    public AIDifficulty getAIDifficulty() {
        return this.aiDifficulty;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Boolean isMultiplayer() {
        return maxParticipants > 1;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }
}
