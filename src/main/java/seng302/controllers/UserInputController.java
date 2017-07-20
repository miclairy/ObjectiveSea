package seng302.controllers;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import seng302.data.BoatAction;

import java.util.Observable;

/**
 * handles user key presses.
 */
public class UserInputController extends Observable {

    private Scene scene;
    private int commandInt;

    /**
     * Sets up user key press handler.
     * @param scene The scene of the client
     */
    public UserInputController(Scene scene) {
        this.scene = scene;
        keyEventListener();
    }

    private void keyEventListener() {
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                commandInt = BoatAction.getTypeFromKeyCode(key.getCode());

                checkKeyPressed(commandInt);
            }
        });
    }

    private void checkKeyPressed(int commandInt){
        if (commandInt != -1) {
            setChanged();
            notifyObservers();
        }
    }

    public int getCommandInt() {
        return commandInt;
    }
}
