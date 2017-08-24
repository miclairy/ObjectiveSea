package seng302.controllers;

import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.input.*;
import seng302.models.*;
import seng302.utilities.DisplayUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

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
                checkTouchMoved(touch);
                if ( consumedTouchEvents.contains(touch.getEventType())) {
                    touch.consume();
                }
            });
        }

        private void checkTouchMoved(TouchEvent touchEvent) {
            Boat playersBoat = race.getBoatById(clientID);
            //double heading = playersBoat.getHeading();
            if (touchEvent.getTouchPoints().size() == 1) {

                CanvasCoordinate boatPosition = DisplayUtils.convertFromLatLon(playersBoat.getCurrentPosition());
                CanvasCoordinate sceneCentreCoordinate = new CanvasCoordinate(scene.getWidth()/2, scene.getHeight()/2);
                CanvasCoordinate touchPoint = new CanvasCoordinate(touchEvent.getTouchPoint().getSceneX(), touchEvent.getTouchPoint().getSceneY());

                System.out.println("Angle of Boat = " + boatPosition.getAngleFromSceneCentre(sceneCentreCoordinate));
                System.out.println("Angle of touch = " + touchPoint.getAngleFromSceneCentre(sceneCentreCoordinate));

                
//                if (touchEvent.getTouchPoint().getSceneX() < scene.getWidth() / 2) {
                //(heading < 180 && heading > 0) {
//                    commandInt = 5;
//                    //}
//                } else {
//                    commandInt = 6;
//                }
//                if (commandInt != -1) {
//                    setChanged();
//                    notifyObservers();
//                }
//            if (key.equals(SHIFT)){
//                Boat boat = race.getBoatById(clientID);
//                boat.changeSails();
//            }
                //}
            }
        }

        public int getCommandInt() {
            return commandInt;
        }

        public void setClientID(int clientID) {
            this.clientID = clientID;
        }
    }

