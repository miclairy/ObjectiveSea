package seng302.controllers;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import seng302.data.BoatAction;
import seng302.models.*;
import seng302.utilities.MathUtils;
import seng302.utilities.PolarReader;

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
        private PolarTable polarTable;
        private CanvasCoordinate previousCoordinate;


    /**
         * Sets up user key press handler.
     * @param scene The scene of the client
     * @param boat
     */
        public TouchInputController(Scene scene, Race race, Boat boat) {
            this.scene = scene;
            this.race = race;
            this.displayTouchController = new DisplayTouchController(scene);
            touchEventListener();
            this.polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
            this.playersBoat = boat;
        }

        private void touchEventListener() {
//            scene.addEventFilter(TouchEvent.TOUCH_STATIONARY, touch -> {
//                checkTouchMoved(touch);
//                if ( consumedTouchEvents.contains(touch.getEventType())) {
//                    touch.consume();
//                }
//            });
//

            Pane touchPane = (Pane) scene.lookup("#touchPane");
            touchPane.setOnScrollStarted(swipe -> {
                swipeStart = new CanvasCoordinate(swipe.getScreenX(), swipe.getScreenY());
            });

            touchPane.setOnScroll(new EventHandler<ScrollEvent>() {
                @Override
                public void handle(ScrollEvent swipe) {
                    CanvasCoordinate currentCoordinate = new CanvasCoordinate(swipe.getX(), swipe.getY());
                    if(previousCoordinate == null){
                        previousCoordinate = currentCoordinate;
                    }
                    if (swipe.getTouchCount() > 0){
                        displayTouchController.displaySwipe(currentCoordinate, previousCoordinate);
                        previousCoordinate = currentCoordinate;
                    }

                }
            });

            touchPane.setOnScrollFinished(swipe -> {
                CanvasCoordinate swipeEnd = new CanvasCoordinate(swipe.getScreenX(), swipe.getScreenY());
                double swipeBearing = MathUtils.getHeadingBetweenTwoCoodinates(swipeStart, swipeEnd);
                swipeAction(swipe, swipeBearing);
                previousCoordinate = null;
            });
        }

        private void swipeAction(ScrollEvent swipe, double swipeBearing){
            double boatHeading = race.getBoatById(clientID).getHeading();
            double headingDifference = abs(boatHeading - swipeBearing) % 180;
            Group root = (Group) scene.lookup("#root");
            if (root.getTransforms().size() > 1){
                headingDifference = swipeBearing;
            }
            if (headingDifference <= 15 || headingDifference >= 165){
                if(Math.abs(boatHeading - swipeBearing) < 15 || Math.abs(boatHeading - swipeBearing) > 345) {
                    if(playersBoat.isSailsIn()) {
                        commandInt = BoatAction.SAILS_IN.getType();
                        setChanged();
                        notifyObservers();
                    }
                } else {
                    if(!playersBoat.isSailsIn()) {
                        commandInt = BoatAction.SAILS_IN.getType();
                        setChanged();
                        notifyObservers();
                    }
                }
            } else {
                double optimumHeading = playersBoat.getTackOrGybeHeading(race.getCourse(), polarTable);
                if(Math.abs(optimumHeading - swipeBearing) < 50 || Math.abs(optimumHeading - swipeBearing) > 310) {
                    commandInt = BoatAction.TACK_GYBE.getType();
                    setChanged();
                    notifyObservers();
                }
            }
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

