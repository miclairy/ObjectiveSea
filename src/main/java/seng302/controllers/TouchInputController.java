package seng302.controllers;

import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.input.*;
import seng302.data.BoatAction;
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
        private CanvasCoordinate swipeStart;
        private Boat playersBoat;

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
//            scene.addEventFilter(TouchEvent.ANY, touch -> {
//                checkTouchMoved(touch);
//                if ( consumedTouchEvents.contains(touch.getEventType())) {
//                    touch.consume();
//                }
//            });

//            scene.addEventFilter(SwipeEvent.ANY, swipe -> {
//                displayTouchController.displaySwipe(swipe.getScreenX(), swipe.getScreenY());
//            });

            scene.addEventFilter(ScrollEvent.SCROLL_STARTED, swipe -> {
                swipeStart = new CanvasCoordinate(swipe.getScreenX(), swipe.getScreenY());
            });

            scene.addEventFilter(ScrollEvent.SCROLL_FINISHED, swipe -> {
                CanvasCoordinate swipeEnd = new CanvasCoordinate(swipe.getScreenX(), swipe.getScreenY());
                displayTouchController.displaySwipe(swipeEnd, swipeStart);
                double swipeBearing = MathUtils.getHeadingBetweenTwoCoodinates(swipeStart, swipeEnd);
                swipeAction(swipe, swipeBearing);
            });
        }

        private void swipeAction(ScrollEvent swipe, double swipeBearing){
            double boatHeading = race.getBoatById(clientID).getHeading();
            double headingDifference = (boatHeading - swipeBearing) % 360;
//            if (headingDifference < -180.0)
//                headingDifference += 360.0;
//            else if (headingDifference >= 180.0)
//                headingDifference -= 360.0;
            double tangentHeadingDistance = ((boatHeading + 90) % 360 - swipeBearing) % 360;
            System.out.println(boatHeading + " swipe "  + swipeBearing + " difference " + headingDifference);
            if (abs(headingDifference) <= abs(tangentHeadingDistance)){
                commandInt = BoatAction.SAILS_IN.getType();
            } else {
                commandInt = BoatAction.TACK_GYBE.getType();
            }
            setChanged();
            notifyObservers();
        }

        private void checkTouchMoved(TouchEvent touchEvent) {
            Boat playersBoat = race.getBoatById(clientID);
            if (touchEvent.getTouchPoints().size() == 1) {
                displayTouchController.displayTouch(touchEvent.getTouchPoint());
                CanvasCoordinate sceneCentreCoordinate = new CanvasCoordinate(scene.getWidth()/2, scene.getHeight()/2);
                CanvasCoordinate touchPoint = new CanvasCoordinate(touchEvent.getTouchPoint().getSceneX(), touchEvent.getTouchPoint().getSceneY());

                double angleTouch = touchPoint.getAngleFromSceneCentre(sceneCentreCoordinate);
                double boatHeading = playersBoat.getHeading() - 90;
                if (boatHeading < 0) {
                    boatHeading += 360;
                }

                if (!(boatHeading > (angleTouch - 2) && boatHeading < (angleTouch + 2))) {
                    if(calculateRotationDirection(boatHeading, angleTouch)){
                        commandInt = 5;
                    } else {
                        commandInt = 6;
                    }
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
     * @return a boolean true if upwind, boolean false if downwind
     */
    public boolean calculateRotationDirection(double boatHeading, double angleTouch){
            if(boatHeading < angleTouch) {
                if(abs(boatHeading - angleTouch)<180) {
                    return false;
                } else{
                    return true;
                }
            }
            else {
                if(abs(boatHeading - angleTouch)<180){
                    return true;
                } else{
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

