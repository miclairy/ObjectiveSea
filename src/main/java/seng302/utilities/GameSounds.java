package seng302.utilities;

import java.io.*;
import javax.sound.sampled.*;

/**
 * Created by Chris on 28/07/2017.
 */

public class GameSounds {

    public void startLobbyMusic() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        Clip clip = AudioSystem.getClip();
        AudioInputStream inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResourceAsStream("/musicFiles/StringBeat.wav"));
        clip.open(inputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }

}

