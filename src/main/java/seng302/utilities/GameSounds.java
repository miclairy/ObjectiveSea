package seng302.utilities;

import java.io.*;
import javax.sound.sampled.*;

/**
 * Created by Chris on 28/07/2017.
 */

public class GameSounds {


    String chris_boat_time = "/musicFiles/ChrisVoiceOvers/boat_time.wav";
    String chris_collided = "/musicFiles/ChrisVoiceOvers/collided.wav";
    String chris_holly_heck = "/musicFiles/ChrisVoiceOvers/holly_heck.wav";
    String chris_list_this_race = "/musicFiles/ChrisVoiceOvers/lost_this_race.wav";
    String chris_mateies = "/musicFiles/ChrisVoiceOvers/maties.wav";
    String chris_ready_race = "/musicFiles/ChrisVoiceOvers/ready_race.wav";
    String chris_rubber_dingy = "/musicFiles/ChrisVoiceOvers/rubber_dingy.wav";
    String chris_sail_ebration = "/musicFiles/ChrisVoiceOvers/sail_ebration.wav";
    String chris_scream = "/musicFiles/ChrisVoiceOvers/scream.wav";





    private Clip clip;
    private AudioInputStream inputStream;

    public void startMenuMusic() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        clip = AudioSystem.getClip();
        inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResourceAsStream("/musicFiles/gameSounds/MainMenuMusic.wav"));
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



    public void startMusic() {
        clip.start();
    }

    public void stopMusic() throws LineUnavailableException {
        clip.stop();
        clip.setFramePosition(0);
    }
}

