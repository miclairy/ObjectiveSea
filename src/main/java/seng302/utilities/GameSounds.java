package seng302.utilities;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
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
    private String chris_lost_this_race = chrisVoiceOver + "lost_this_race.mp3";
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
    private String selectedMusic;
    private Random random = new Random();
    private boolean endless = false;
    private MediaPlayer mediaPlayer;
    private int randomSeaGull = (int)(random.nextDouble() * 10000) + 10000; //5-10 seconds

    public void mainMenuMusic() {
        selectedMusic = "/musicFiles/gameMusic/MainMenuMusic.wav";
    }

    public void singlePlayerMusic() {
        selectedMusic = "/musicFiles/gameMusic/SinglePlayerMusic.wav";
    }

    public void tutorialMusic() {
        selectedMusic = "/musicFiles/gameMusic/TutorialMusic.wav";
    }

    public void oceanWaves() {
        selectedMusic = "/musicFiles/gameSounds/CrispOceanWaves.wav";
    }

    public void flockSeagulls() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(endless) {
                    selectedVoiceOver = "/musicFiles/gameSounds/FlockOfSeagulls.mp3";
                    playGameSound();
                } else {
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 0, randomSeaGull);
    }

    public void hitMark() {
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
    }

    public void hitBoat() {
        int randomNumber = (int)(random.nextDouble() * 4) + 1; //1-4
        switch(randomNumber){
            case 1: selectedVoiceOver = louis_get_disqualified;
                break;
            case 2: selectedVoiceOver = louis_board;
                break;
            case 3: selectedVoiceOver = sam_get_disqualified;
                break;
            case 4: selectedVoiceOver = sam_board;
                break;
        }
    }

    public void hitBoundary() {
        int randomNumber = (int)(random.nextDouble() * 4) + 1; //1-4
        switch(randomNumber){
            case 1: selectedVoiceOver = louis_cant_leave;
                break;
            case 2: selectedVoiceOver = louis_be_shot;
                break;
            case 3: selectedVoiceOver = sam_cant_leave;
                break;
            case 4: selectedVoiceOver = sam_be_shot;
                break;
        }
    }

    public void firstPlace() {
        int randomNumber = (int)(random.nextDouble() * 2) + 1; //1-2
        switch(randomNumber){
            case 1: selectedVoiceOver = louis_pointless_race;
                break;
            case 2: selectedVoiceOver = sam_pointless_race;
                break;
        }
    }

    public void everyoneButFirstPlace() {
        int randomNumber = (int)(random.nextDouble() * 3) + 1; //1-3
        switch(randomNumber){
            case 1: selectedVoiceOver = louis_been_taken;
                break;
            case 2: selectedVoiceOver = sam_been_taken;
                break;
            case 3: selectedVoiceOver = chris_lost_this_race;
                break;
        }
    }

    public void everyone() {
        int randomNumber = (int)(random.nextDouble() * 2) + 1; //1-2
        switch(randomNumber){
            case 1: selectedVoiceOver = louis_youre_last;
                break;
            case 2: selectedVoiceOver = sam_youre_last;
                break;
        }
    }

    public void lastPlace() {
        int randomNumber = (int)(random.nextDouble() * 2) + 1; //1-2
        switch(randomNumber){
            case 1: selectedVoiceOver = louis_boat_time;
                break;
            case 2: selectedVoiceOver = chris_boat_time;
                break;
        }
    }

    public void boatDamage() {
        int randomNumber = (int)(random.nextDouble() * 6) + 1; //1-6
        switch(randomNumber){
            case 1: selectedVoiceOver = louis_rubber_dingy;
                break;
            case 2: selectedVoiceOver = sam_rubber_dingy;
                break;
            case 3: selectedVoiceOver = chris_rubber_dingy;
                break;
            case 4: selectedVoiceOver = louis_live_by;
                break;
            case 5: selectedVoiceOver = sam_live_by;
                break;
            case 6: selectedVoiceOver = louis_oh_ship;
                break;
        }
    }


    public void preRace() {
        int randomNumber = (int)(random.nextDouble() * 2) + 1; //1-2
        switch(randomNumber){
            case 1: selectedVoiceOver = louis_ready_race;
                break;
            case 2: selectedVoiceOver = chris_ready_race;
                break;
        }
    }



    public void playGameSound () {
        URL resource = getClass().getResource(selectedVoiceOver);
        mediaPlayer = new MediaPlayer(new Media(resource.toString()));
        double volume = mediaPlayer.getVolume();
        mediaPlayer.setVolume(volume - 0.1);
        mediaPlayer.play();
    }

    public void playEndlessMusic() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        clip = AudioSystem.getClip();
        inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResourceAsStream(selectedMusic));
        clip.open(inputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }

    public void startSeaGullNoise() {
        endless = true;
        flockSeagulls();
    }

    public void stopEndlessMusic() throws LineUnavailableException {
        clip.stop();
        endless = false;
        clip.setFramePosition(0);
    }
}

