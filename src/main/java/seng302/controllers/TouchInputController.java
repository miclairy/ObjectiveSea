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
import static java.lang.Math.hypot;

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
                double windAngle = race.getCourse().getWindDirection() + 180;
                double touchAngle = touchPoint.getAngleFromSceneCentre(boatPosition) - 270;

                if (touchAngle < 0) {
                    touchAngle += 360;
                }

                if (windAngle > 360) {
                    windAngle -= 360;
                }


                double boatHeading = playersBoat.getHeading();

                System.out.println("Touch angle: " + touchAngle);
                System.out.println("Boat heading: " + boatHeading);
                System.out.println("Wind angle: " + windAngle);


                //downWind towards wind arrow - 6
                //upWind away from wind arrow - 5


                double DELTA = 0.00001;
                if (!((boatHeading > (touchAngle - 2)) && (boatHeading < (touchAngle + 2)))) {
                    if ((((boatHeading - touchAngle) < 180) && ((boatHeading - touchAngle) > 0)) ||
                            (((boatHeading - touchAngle) < -180) && ((boatHeading - touchAngle) > -360))) {
                        if (boatHeading > touchAngle) {
                            commandInt = changeRotationDirection(windAngle, boatHeading, touchAngle);
                        } else if (boatHeading < touchAngle) {
                            //boat heading between 0-180
                            //touch angle between 180-360
                            //touch - heading > 180
                            double newWindAngle;
                            if (windAngle < 180) {
                                newWindAngle = windAngle + 360;
                            } else {
                                newWindAngle = windAngle;
                            }
                            commandInt = changeRotationDirection(newWindAngle, boatHeading + 360, touchAngle);
                        }
                        //Anti Clockwise

                    } else if ((((boatHeading - touchAngle) > 180) && ((boatHeading - touchAngle) < 360)) ||
                            (((boatHeading - touchAngle) > -180) && ((boatHeading - touchAngle) < 0))) {

                        //Clockwise

                    }
                } else {
                    commandInt = -1;
                }

                if (commandInt != -1) {
                    setChanged();
                    notifyObservers();
//                    System.out.println("Notified Observers");
//                    System.out.println("Command Int: " + commandInt);
                }
            }
        }


        public int changeRotationDirection(double windAngle, double boatHeading, double touchAngle) {

            int direction = -1;
            double oppositeWindAngle = windAngle - 180;
            if(oppositeWindAngle < 0) {
                oppositeWindAngle += 360;
            }

            if ((((touchAngle - windAngle) < -180 && (touchAngle - windAngle) > -360)) ||
                    (((touchAngle - windAngle) > 0) && (touchAngle - windAngle) < 180) ||
                    (windAngle < boatHeading && windAngle > touchAngle)) {
                direction = 6;
            } else if ((((touchAngle - windAngle) < 0 && (touchAngle - windAngle) > -180)) ||
                    (((touchAngle - windAngle) > 180) && (touchAngle - windAngle) < 360) ||
                    (oppositeWindAngle < boatHeading && oppositeWindAngle > touchAngle)) {
                direction = 5;
            }

            return direction;
        }


/*    //**
      calculates the optimal rotation for the boat to move towards finger
     * @param boatHeading the current heading of the boat
     * @param touchAngle the angle of the touch input from 0
     * @param windDirection direction of wind from
     * @return a boolean true if action should be upwind, boolean false if action should be downwind
     *//*



    public static boolean checkRotation(double boatHeading, double touchAngle, double windDirection){
//        System.out.println((windDirection + 180) % 360);
        double windTo = (windDirection + 180) % 360;
        if(boatHeading <= windTo && boatHeading >= windDirection) {// on right of wind coming from east
            System.out.println("right");
            if(touchAngle + 360 < boatHeading + 360 ) {// boat should move upwind
                return true;
            } else {// boat should move downwind
                return false;
            }
        } else { // on left
            if(touchAngle + 360 < boatHeading + 360) {
                return false;
            } else {}
        }
        if(windTo < windDirection && (boatHeading + 360) > windTo) { // on right side of wind coming from west
//            System.out.println("right");
            double copyBoatHeading = boatHeading;
            if((boatHeading - touchAngle) < 0) {
                boatHeading += 360;
            }
            if((touchAngle - copyBoatHeading) < 0) {
                touchAngle += 360;
            }
            if(touchAngle + 360 < boatHeading + 360 ) {// boat should move upwind
                return true;
            } else {// boat should move downwind
                return false;
            }
        }
//            if(boatHeading < touchAngle) {
//                if(abs(boatHeading - touchAngle)<180) {
//                    return false;
//                } else{
//                    return true;
//                }
//            }
//            else {
//                if(abs(boatHeading - touchAngle)<180){
//                    return true;
//                } else{
//                    return false;
//                }
//            }
        else { // on left
            System.out.println("left");
            if(boatHeading < windDirection) {
                boatHeading += 360;
            }
            if(touchAngle < windDirection) {
                touchAngle += 360;
            }
            if(touchAngle > boatHeading) {
                return true;
            } else {
                return false;
            }
        }
    }*/

        public int getCommandInt() {
            return commandInt;
        }

        public void setClientID(int clientID) {
            this.clientID = clientID;
        }
    }

