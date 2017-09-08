package seng302.controllers;

import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.input.*;
import seng302.models.*;
import seng302.utilities.DisplayUtils;
import seng302.utilities.MathUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import static java.lang.Math.abs;

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
        private DisplayTouchController displayTouchController;

        /**
         * Sets up user key press handler.
         * @param scene The scene of the client
         */
        public TouchInputController(Scene scene, Race race) {
            this.scene = scene;
            this.race = race;
            this.displayTouchController = new DisplayTouchController(scene);
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
            if (touchEvent.getTouchPoints().size() == 1) {
                CanvasCoordinate touchPoint = new CanvasCoordinate(touchEvent.getTouchPoint().getSceneX(), touchEvent.getTouchPoint().getSceneY());
                CanvasCoordinate boatPosition = DisplayUtils.convertFromLatLon(playersBoat.getCurrentPosition());
                double windAngle = race.getCourse().getWindDirection();
                double angleTouch = touchPoint.getAngleFromSceneCentre(boatPosition) - 270;
                if (angleTouch < 0) {
                    angleTouch += 360;
                }
                double boatHeading = playersBoat.getHeading();

                // 5 = UpWind
                // 6 = DownWind
                double DELTA = 0.00001;

                System.out.println("Boat heading: " + boatHeading);
                System.out.println("angle of touch: " + angleTouch);
                double angleToRotate = MathUtils.getAngleBetweenTwoHeadings(boatHeading, angleTouch);
                System.out.println("angle to rotate " + angleToRotate);

                if(angleToRotate > 6) {
                    System.out.println(checkRotation(boatHeading, angleTouch, windAngle));
                    if (checkRotation(boatHeading, angleTouch, windAngle)) { // checks which action to take (upwind or downwind)
                        //upwind
                        commandInt = 5;
                    } else {
                        //downwind
                        commandInt = 6;
                    }
                } else {
                    commandInt = -1;
                }

                if (commandInt != -1) {
                    setChanged();
                    notifyObservers();
                }
            }
        }

    /**
     * calculates the optimal rotation for the boat to move towards finger
     * @param boatHeading the current heading of the boat
     * @param angleTouch the angle of the touch input from 0
     * @param windDirection direction of wind from
     * @return a boolean true if action should be upwind, boolean false if action should be downwind
     */
    public static boolean checkRotation(double boatHeading, double angleTouch, double windDirection){
//        System.out.println((windDirection + 180) % 360);
        double windTo = (windDirection + 180) % 360;
        if(boatHeading <= windTo && boatHeading >= windDirection) {// on right of wind coming from east
            System.out.println("right");
            if(angleTouch + 360 < boatHeading + 360 ) {// boat should move upwind
                return true;
            } else {// boat should move downwind
                return false;
            }
        }
        if(windTo < windDirection && (boatHeading + 360) > windTo) { // on right side of wind coming from west
//            System.out.println("right");
            double copyBoatHeading = boatHeading;
            if((boatHeading - angleTouch) < 0) {
                boatHeading += 360;
            }
            if((angleTouch - copyBoatHeading) < 0) {
                angleTouch += 360;
            }
            if(angleTouch + 360 < boatHeading + 360 ) {// boat should move upwind
                return true;
            } else {// boat should move downwind
                return false;
            }
        }
        else { // on left
            System.out.println("left");
            if(boatHeading < windDirection) {
                boatHeading += 360;
            }
            if(angleTouch < windDirection) {
                angleTouch += 360;
            }
            if(angleTouch > boatHeading) {
                return true;
            } else {
                return false;
            }
        }
    }

        public int getCommandInt() {
            return commandInt;
        }

        public void setClientID(int clientID) {
            this.clientID = clientID;
        }
    }

