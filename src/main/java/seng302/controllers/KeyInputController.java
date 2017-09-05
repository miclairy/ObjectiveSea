package seng302.controllers;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seng302.data.BoatAction;
import seng302.models.Boat;
import seng302.models.Race;
import seng302.utilities.DisplayUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import static javafx.scene.input.KeyCode.*;

/**
 * handles user key presses.
 */
public class KeyInputController extends Observable {

    private Scene scene;
    private int commandInt;
    private int clientID;
    private Race race;
    private final Set<KeyCode> cosumedKeyCodes = new HashSet<>(Arrays.asList(KeyCode.SPACE, KeyCode.UP, KeyCode.DOWN));

    /**
     * Sets up user key press handler.
     * @param scene The scene of the client
     */
    public KeyInputController(Scene scene, Race race) {
        this.scene = scene;
        this.race = race;
        keyEventListener();
    }

    private void keyEventListener() {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, key -> {
            checkKeyPressed(key.getCode());
            if ( cosumedKeyCodes.contains(key.getCode()) ){
                key.consume();
            }
        });
    }

    private void checkKeyPressed(KeyCode key){
        commandInt = BoatAction.getTypeFromKeyCode(key);
        if (commandInt != -1) {
            setChanged();
            notifyObservers();
        }
        if (key.equals(SHIFT)){
            Boat boat = race.getBoatById(clientID);
            boat.changeSails();
        }
    }

    public int getCommandInt() {
        return commandInt;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }
}
