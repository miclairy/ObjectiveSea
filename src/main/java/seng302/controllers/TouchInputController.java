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
                //displayTouchController.displayTouch(touchEvent.getTouchPoint());
                //CanvasCoordinate sceneCentreCoordinate = new CanvasCoordinate(scene.getWidth()/2, scene.getHeight()/2);
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

//                        if ((windAngle > boatHeading && windAngle - 180 < boatHeading && StrictMath.abs(windAngle - 180 - boatHeading) > DELTA) ||
//                                (windAngle < boatHeading && windAngle + 180 < boatHeading && StrictMath.abs(windAngle + 180 - boatHeading) > DELTA)) {
//                            commandInt = 5;
//                            System.out.println("up 5");
//                        } else {
//                            System.out.println("up 6");
//                            commandInt = 6;
//                        }
                    } else {
                        //downwind
                        commandInt = 6;

//                        if ((windAngle > boatHeading && windAngle - 180 > boatHeading && StrictMath.abs(windAngle - 180 - boatHeading) > DELTA) ||    //3
//                                (windAngle < boatHeading && windAngle + 180 > boatHeading && StrictMath.abs(windAngle + 180 - boatHeading) > DELTA)) {
//                            System.out.println("down 6");
//                            commandInt = 6;
//                        } else {
//                            System.out.println("down 5");
//                            commandInt = 5;
//                        }
                    }
                } else {
                    commandInt = -1;
                }

//                if (!(boatHeading > (angleTouch - 2) && boatHeading < (angleTouch + 2))) {
//                    if(checkRotation(boatHeading, angleTouch)) {
//                        //if ((boatHeading < windAngle + 90) || (boatHeading > windAngle - 90)) {
//                            commandInt = 5;
////                        } else {
////                            commandInt = 6;
////                        }
//                    } else {
//                        //if ((boatHeading > windAngle + 90) && (boatHeading < windAngle - 90)) {
//                            commandInt = 6;
////                        } else {
////                            commandInt = 5;
////                        }
//                    }
//                }
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
        double windTo = windDirection + 180;
//        if((angleTouch - boatHeading) < 0) {
//            angleTouch += 360;
//        }
        if(boatHeading + 360 < windTo + 360 && boatHeading + 360 > windTo + 180) {// on right of down wind
            System.out.println("right");
            if(angleTouch + 360 < boatHeading + 360 ) {// boat should move upwind
                System.out.println("up");
                return true;
            } else {// boat should move upwind
                System.out.println("down");
                return false;
            }
        } else { // on left
            System.out.println("left");
            if(angleTouch + 360 < boatHeading + 360) {
                System.out.println("down");
                return false;
            } else {
                System.out.println("up");
                return true;
            }
        }
//            if(boatHeading < angleTouch) {
//                if(abs(boatHeading - angleTouch)<180) {
//                    return false;
//                } else{
//                    return true;
//                }
//            }
//            else {
//                if(abs(boatHeading - angleTouch)<180){
//                    return true;
//                } else{
//                    return false;
//                }
//            }
        }

        public int getCommandInt() {
            return commandInt;
        }

        public void setClientID(int clientID) {
            this.clientID = clientID;
        }
    }

