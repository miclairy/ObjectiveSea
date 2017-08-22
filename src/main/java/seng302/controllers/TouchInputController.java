package seng302.controllers;

import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.input.*;
import seng302.data.BoatAction;
import seng302.models.Boat;
import seng302.models.Race;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import static javafx.scene.input.KeyCode.*;

    /**
     * handles user key presses.
     */
    public class TouchInputController extends Observable {

        private Scene scene;
        private int commandInt;
        private int clientID;
        private Race race;
        private final Set<EventType<TouchEvent>> consumedTouchEvents = new HashSet<>(Arrays.asList(TouchEvent.TOUCH_MOVED, TouchEvent.TOUCH_PRESSED));
        private final Set<EventType<SwipeEvent>> consumedSwipeEvents = new HashSet<>(Arrays.asList(SwipeEvent.SWIPE_LEFT, SwipeEvent.SWIPE_DOWN));

        /**
         * Sets up user key press handler.
         * @param scene The scene of the client
         */
        public TouchInputController(Scene scene, Race race) {
            this.scene = scene;
            this.race = race;
            touchEventListener();
        }

        private void touchEventListener() {
            scene.addEventFilter(TouchEvent.ANY, touch -> {
                checkTouchPressed(touch.getEventType());
                if ( consumedTouchEvents.contains(touch.getEventType())) {
                    touch.consume();
                }
            });
        }

        private void checkTouchPressed(EventType<TouchEvent> touchEvent){
            commandInt = 5;
            if (commandInt != -1) {
                setChanged();
                notifyObservers();
            }
//            if (key.equals(SHIFT)){
//                Boat boat = race.getBoatById(clientID);
//                boat.changeSails();
//            }
        }

        public int getCommandInt() {
            return commandInt;
        }

        public void setClientID(int clientID) {
            this.clientID = clientID;
        }
    }

