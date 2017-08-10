package seng302.utilities;

import java.io.*;
import javax.sound.sampled.*;

/**
 * Created by Chris on 28/07/2017.
 */

public class GameSounds {

    String chrisVoiceOver = "/musicFiles/ChrisVoiceOvers/";
    String chris_boat_time = chrisVoiceOver + "boat_time.wav";
    String chris_collided = chrisVoiceOver + "collided.wav";
    String chris_holly_heck = chrisVoiceOver + "holly_heck.wav";
    String chris_list_this_race = chrisVoiceOver + "lost_this_race.wav";
    String chris_mateies = chrisVoiceOver + "maties.wav";
    String chris_ready_race = chrisVoiceOver + "ready_race.wav";
    String chris_rubber_dingy = chrisVoiceOver + "rubber_dingy.wav";
    String chris_sail_ebration = chrisVoiceOver + "sail_ebration.wav";
    String chris_scream = chrisVoiceOver + "scream.wav";





    private Clip clip;
    private AudioInputStream inputStream;

    public void startMenuMusic() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        clip = AudioSystem.getClip();
        inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResourceAsStream("/musicFiles/gameMusic/MainMenuMusic.wav"));
        clip.open(inputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void startTutorialMusic() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        clip = AudioSystem.getClip();
        inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResourceAsStream("/musicFiles/gameMusic/TutorialMusic.wav"));
        clip.open(inputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void startOceanWaves() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        clip = AudioSystem.getClip();
        inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResourceAsStream("/musicFiles/gameSounds/CrispOceanWaves.wav"));
        clip.open(inputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void flockSeagulls() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        clip = AudioSystem.getClip();
        inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResourceAsStream("/musicFiles/gameSounds/FlockOfSeagulls.wav"));
        clip.open(inputStream);
    }

    public void hitMark() {

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



    public void startMusic() {
        clip.start();
    }

    public void stopMusic() throws LineUnavailableException {
        clip.stop();
        clip.setFramePosition(0);
    }
}

