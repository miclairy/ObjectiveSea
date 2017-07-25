package seng302.controllers;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seng302.data.BoatAction;
import seng302.models.Boat;
import seng302.models.Race;

import java.util.Observable;

import static javafx.scene.input.KeyCode.SHIFT;

/**
 * handles user key presses.
 */
public class UserInputController extends Observable {

    private Scene scene;
    private int commandInt;
    private int clientID;
    private Race race;

    /**
     * Sets up user key press handler.
     * @param scene The scene of the client
     */
    public UserInputController(Scene scene, Race race) {
        this.scene = scene;
        this.race = race;
        keyEventListener();
    }

    private void keyEventListener() {
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                checkKeyPressed(key.getCode());
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
