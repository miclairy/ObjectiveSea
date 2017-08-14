package seng302.utilities;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.util.Random;
import javax.sound.sampled.*;

/**
 * Created by Chris on 28/07/2017.
 */

public class GameSounds {

    //Chris Voice overs
    private String chrisVoiceOver = "/musicFiles/gameVoiceOvers/ChrisVoiceOvers/";
    private String chris_boat_time = chrisVoiceOver + "boat_time.mp3";
    private String chris_collided = chrisVoiceOver + "collided.mp3";
    private String chris_holly_heck = chrisVoiceOver + "holly_heck.mp3";
    private String chris_list_this_race = chrisVoiceOver + "lost_this_race.mp3";
    private String chris_mateies = chrisVoiceOver + "maties.mp3";
    private String chris_ready_race = chrisVoiceOver + "ready_race.mp3";
    private String chris_rubber_dingy = chrisVoiceOver + "rubber_dingy.mp3";
    private String chris_sail_ebration = chrisVoiceOver + "sail_ebration.mp3";
    private String chris_scream = chrisVoiceOver + "scream.mp3";

    //Louis Voice overs
    private String louisVoiceOver = "/musicFiles/gameVoiceOvers/LouisVoiceOvers/";
    private String louis_be_shot = louisVoiceOver + "be_shot.mp3";
    private String louis_been_taken = louisVoiceOver + "been_taken.mp3";
    private String louis_board = louisVoiceOver + "board.mp3";
    private String louis_boat_time = louisVoiceOver + "boat_time.mp3";
    private String louis_buoy = louisVoiceOver + "buoy.mp3";
    private String louis_cant_leave = louisVoiceOver + "cant_leave.mp3";
    private String louis_get_disqualified = louisVoiceOver + "get_disqualified.mp3";
    private String louis_live_by = louisVoiceOver + "live_by.mp3";
    private String louis_never_there = louisVoiceOver + "never_there.mp3";
    private String louis_oh_ship = louisVoiceOver + "oh_ship.mp3";
    private String louis_over_that = louisVoiceOver + "over_that.mp3";
    private String louis_over_that_matey = louisVoiceOver + "over_that_matey.mp3";
    private String louis_pointless_race = louisVoiceOver + "pointless_race.mp3";
    private String louis_ready_race = louisVoiceOver + "ready_race.mp3";
    private String louis_rubber_dingy = louisVoiceOver + "rubber_dingy.mp3";
    private String louis_sail_ebration = louisVoiceOver + "sail_ebration.mp3";
    private String louis_try_that = louisVoiceOver + "try_that.mp3";
    private String louis_water = louisVoiceOver + "water.mp3";
    private String louis_youre_last = louisVoiceOver + "youre_last.mp3";

    //Sam Voice Overs
    private String samVoiceOver = "/musicFiles/gameVoiceOvers/SamVoiceOvers/";
    private String sam_be_shot = samVoiceOver + "be_shot.mp3";
    private String sam_been_taken = samVoiceOver + "been_taken.mp3";
    private String sam_board = samVoiceOver + "board.mp3";
    private String sam_cant_leave = samVoiceOver + "cant_leave.mp3";
    private String sam_get_disqualified = samVoiceOver + "get_disqualified.mp3";
    private String sam_live_by = samVoiceOver + "live_by.mp3";
    private String sam_never_there = samVoiceOver + "never_there.mp3";
    private String sam_over_that = samVoiceOver + "over_that.mp3";
    private String sam_pointless_race = samVoiceOver + "pointless_race.mp3";
    private String sam_rubber_dingy = samVoiceOver + "rubber_dingy.mp3";
    private String sam_try_that = samVoiceOver + "try_that.mp3";
    private String sam_youre_last = samVoiceOver + "youre_last.mp3";

    private Clip clip;
    private AudioInputStream inputStream;
    private String selectedVoiceOver;
    private Random random = new Random();
    private MediaPlayer mediaPlayer;

    public void mainMenuMusic() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        clip = AudioSystem.getClip();
        inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResourceAsStream("/musicFiles/gameMusic/MainMenuMusic.wav"));
        clip.open(inputStream);
    }

    public void singlePlayerMusic() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        clip = AudioSystem.getClip();
        inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResourceAsStream("/musicFiles/gameMusic/SinglePlayerMusic.wav"));
        clip.open(inputStream);
//        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
//        gainControl.setValue(6.0f);
    }

    public void tutorialMusic() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        clip = AudioSystem.getClip();
        inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResourceAsStream("/musicFiles/gameMusic/TutorialMusic.wav"));
        clip.open(inputStream);
    }

    public void menuMusic() {
        URL resource = getClass().getResource("song name here");
        mediaPlayer = new MediaPlayer(new Media(resource.toString()));
        mediaPlayer.play();
    }

    public void oceanWaves() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        clip = AudioSystem.getClip();
        inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResourceAsStream("/musicFiles/gameSounds/CrispOceanWaves.wav"));
        clip.open(inputStream);
    }

    public void flockSeagulls() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        clip = AudioSystem.getClip();
        inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResourceAsStream("/musicFiles/gameSounds/FlockOfSeagulls.mp3"));
        clip.open(inputStream);
    }

    public void hitMark() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        clip = AudioSystem.getClip();
        int randomNumber = (int)(random.nextDouble() * 5) + 1; //1-5
        switch(randomNumber){
            case 1: selectedVoiceOver = louis_over_that;
                    break;
            case 2: selectedVoiceOver = louis_over_that_matey;
                    break;
            case 3: selectedVoiceOver = chris_holly_heck;
                    break;
            case 4: selectedVoiceOver = chris_mateies;
                    break;
            case 5: selectedVoiceOver = sam_over_that;
                    break;
        }
        inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResourceAsStream(selectedVoiceOver));
        clip.open(inputStream);
    }

    public void hitBoat() {

    }

    public void hitBoundary() {

    }

    public void firstPlace() {

    }

    public void everyoneButFirstPlace() {

    }

    public void everyone() {

    }

    public void lastPlace() {

    }

    public void boatDamage() {

    }

    public void preRace() {

    }



    public void startMusic() throws IOException, LineUnavailableException {
        clip.start();
    }

    public void startEndlessMusic() throws IOException, LineUnavailableException {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }

    public void stopMusic() throws LineUnavailableException {
        clip.stop();
        clip.setFramePosition(0);
    }
}

