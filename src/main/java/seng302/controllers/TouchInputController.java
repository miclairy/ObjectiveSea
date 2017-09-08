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
                double windAngle = race.getCourse().getWindDirection() + 180;
                double touchAngle = touchPoint.getAngleFromSceneCentre(boatPosition) - 270;

                if (touchAngle < 0) {
                    touchAngle += 360;
                }

                if (windAngle > 360) {
                    windAngle -= 360;
                }

                double oppositeWindAngle = windAngle - 180;
                if(oppositeWindAngle < 0) {
                    oppositeWindAngle += 360;
                }

                double boatHeading = playersBoat.getHeading();

                System.out.println("Touch angle: " + touchAngle);
                System.out.println("Boat heading: " + boatHeading);
                System.out.println("Wind angle: " + windAngle);


                double DELTA = 0.00001;
                if (!((boatHeading > (touchAngle-2)) && (boatHeading < (touchAngle+2)))) {
                    if (boatHeading > touchAngle && (boatHeading - touchAngle) < 180) {
                        if (windAngle < boatHeading && windAngle > touchAngle) {
                            commandInt = 6;
                        } else if ((windAngle + DELTA) > boatHeading && windAngle > touchAngle && oppositeWindAngle < touchAngle) {
                            commandInt = 5;
                        } else if (windAngle > boatHeading && oppositeWindAngle < touchAngle) {
                            commandInt = 5;
                        } else if (windAngle > boatHeading && oppositeWindAngle > touchAngle && windAngle > touchAngle ) {
                            commandInt = 5;
                        } else if ((oppositeWindAngle + DELTA) > boatHeading && oppositeWindAngle > touchAngle) {
                            commandInt = 6;
                        } else if (windAngle < boatHeading && windAngle < touchAngle && boatHeading > oppositeWindAngle) {
                            commandInt = 6;
                        }
                    }
                } else {
                    commandInt = -1;
                }

                /*} else if (boatHeading < touchAngle && Math.abs(boatHeading - touchAngle) > 180) {
                        if(windAngle < boatHeading && boatHeading < oppositeWindAngle && windAngle < touchAngle) {
                            commandInt = 6;
                        } else if ((windAngle + DELTA) > boatHeading && windAngle < touchAngle && (boatHeading + 180) < (windAngle + DELTA) ) {
                            commandInt = 5;
                        } else if (windAngle > boatHeading && windAngle < touchAngle) {
                            commandInt = 6;
                        } else if (windAngle > boatHeading && windAngle > touchAngle) {
                            commandInt = 6;
                        }
                    } else if (boatHeading > touchAngle && (boatHeading - touchAngle) > 180) {
                        if (windAngle > boatHeading && windAngle > touchAngle) {
                            commandInt = 6;
                        } else if ((windAngle - DELTA) < boatHeading && windAngle > touchAngle && (boatHeading - 180) > (windAngle - DELTA)) {
                            commandInt = 5;
                        } else if (windAngle < boatHeading && windAngle > touchAngle) {
                            commandInt = 6;
                        } else if (windAngle < boatHeading && windAngle < touchAngle) {
                            commandInt = 6;
                        }
                    } else if (boatHeading < touchAngle && Math.abs(boatHeading - touchAngle) < 180) {
                        if (windAngle > boatHeading && windAngle < touchAngle) {
                            commandInt = 6;
                        } else if ((windAngle - DELTA) < boatHeading && windAngle < touchAngle) {
                            commandInt = 5;
                        } else if (windAngle > boatHeading && windAngle > touchAngle) {
                            commandInt = 6;
                        }
                    }*/


                //downWind towards wind arrow - 6
                //upWind away from wind arrow - 5

/*                if (!((boatHeading >= touchAngle-3) && (boatHeading<= touchAngle+3))) {
                    if (((boatHeading - touchAngle) > 0) && ((boatHeading - touchAngle) < 180)) {
                        if (windAngle > boatHeading) {
                            commandInt = 5;
                            System.out.println("UpWind");
                        } else {
                            commandInt = 6;
                        }
                        //-3
                    } else if (((boatHeading - touchAngle) < 0) && ((boatHeading - touchAngle) > -180)) {
                        if (windAngle > boatHeading) {
                            commandInt = 6;
                            System.out.println("DownWind");
                        } else {
                            commandInt = 5;
                        }
                        //+3
                    }
                }*/
/*                if (!((boatHeading > (touchAngle-3)) && (boatHeading < (touchAngle+3)))) {
                    if ((boatHeading > 180 && boatHeading < 360) && (boatHeading > touchAngle) && ((boatHeading - 180) < touchAngle)) {
                        if (boatHeading > windAngle && (boatHeading - 180) < windAngle) {
                            commandInt = 6;
                            System.out.println("Not here");
                        }*//* else if (!(boatHeading > oppositeWindAngle && (boatHeading + 180) > oppositeWindAngle)) {
                            commandInt = 6;
                            System.out.println("Working1");
                        } *//* else {
                            commandInt = 5;
                            System.out.println("Not here");
                        }
                    } else if ((boatHeading > 180 && boatHeading < 360) && (boatHeading < touchAngle) && ((boatHeading + 180) > touchAngle)) {
                        if (boatHeading < windAngle && (boatHeading + 180) > windAngle) {
                            commandInt = 6;
                            System.out.println("Going down wind - no");
                        } *//*else if (!(boatHeading < oppositeWindAngle && (boatHeading - 180) < oppositeWindAngle)) {
                            commandInt = 6;
                            System.out.println("Working2");
                        } *//* else {
                            commandInt = 5;
                            System.out.println("Going up wind - no");
                        }
                    } else if ((boatHeading < 180 && boatHeading > 0) && (boatHeading < touchAngle) && ((boatHeading + 180) > touchAngle)) {
                        if (boatHeading < windAngle && (boatHeading + 180) > windAngle) {
                            commandInt = 6;
                            System.out.println("Going down wind");
                        } *//*else if (!(boatHeading < oppositeWindAngle && (boatHeading - 180) < oppositeWindAngle)) {
                            commandInt = 6;
                            System.out.println("Working3");
                        } *//* else {
                            commandInt = 5;
                            System.out.println("Going up wind");
                        }
                    } else if ((boatHeading < 180 && boatHeading > 0) && (boatHeading > touchAngle) && ((boatHeading - 180) < touchAngle)) {// ||
                        if ((boatHeading > windAngle && (boatHeading - 180) < windAngle)) {
                            commandInt = 6; //down
                            System.out.println("Please no");
                        }*//* else if (!(boatHeading > oppositeWindAngle && (boatHeading + 180) > oppositeWindAngle)) {
                            commandInt = 6;
                            System.out.println("Working4");
                        }*//* else {
                            commandInt = 5; //up
                            System.out.println("I shouldn't be trying this still");
                        }
                    } else if ((boatHeading < 180 && boatHeading > 0) && (boatHeading > (touchAngle - 360)) && ((boatHeading - 180) < (touchAngle - 360))) {
                        if (boatHeading > oppositeWindAngle && (boatHeading + 180) > oppositeWindAngle) {
                            commandInt = 5;
                            System.out.println("1");
                        } else {
                            commandInt = 6;
                            System.out.println("1");
                        }
                    } else if ((boatHeading < 180 && boatHeading > 0) && (boatHeading < (touchAngle - 360)) && ((boatHeading + 180) > (touchAngle - 360))) {
                        if (boatHeading < oppositeWindAngle && (boatHeading - 180) < oppositeWindAngle) {
                            commandInt = 5;
                            System.out.println("2");
                        } else {
                            commandInt = 6;
                            System.out.println("2");
                        }*/
                    }/* else if((boatHeading > 180 && boatHeading < 360) && (boatHeading > (touchAngle-360)) && ((boatHeading - 180) < (touchAngle-360))) {
                        if (boatHeading > windCheck && (boatHeading + 180) > windCheck) {
                            commandInt = 5;
                            System.out.println("3");
                        } else {
                            commandInt = 6;
                            System.out.println("3");
                        }
                    } else if((boatHeading > 180 && boatHeading < 360) && (boatHeading < (touchAngle-360)) && ((boatHeading + 180) > (touchAngle-360))) {
                        if (boatHeading < windCheck && (boatHeading - 180) < windCheck) {
                            commandInt = 5;
                            System.out.println("4");
                        } else {
                            commandInt = 6;
                            System.out.println("4");
                        }
                    }*/


                //}

/*                // 5 = UpWind
                // 6 = DownWind
                double DELTA = 0.00001;

//                System.out.println("Boat heading: " + boatHeading);
//                System.out.println("angle of touch: " + angleTouch);
//                System.out.println(boatHeading > (angleTouch - 3));
//                System.out.println(boatHeading < (angleTouch + 3));
                double angleToRotate = MathUtils.getAngleBetweenTwoHeadings(boatHeading, touchAngle);
//                System.out.println("angle to rotate " + angleToRotate);
//                if(!(boatHeading > (angleTouch - 3)) && !(boatHeading < (angleTouch + 3))) {

                System.out.println("Boat heading: " + boatHeading);
                System.out.println("angle of touch: " + angleTouch);
                double angleToRotate = MathUtils.getAngleBetweenTwoHeadings(boatHeading, angleTouch);
                System.out.println("angle to rotate " + angleToRotate);

                if(angleToRotate > 6) {
                    System.out.println(checkRotation(boatHeading, touchAngle, windAngle));
                    if (checkRotation(boatHeading, touchAngle, windAngle)) { // checks which action to take (upwind or downwind)
                        //upwind
                        commandInt = 5;
                    } else {
                        //downwind
                        commandInt = 6;
                    }
                } else {
                    commandInt = -1;
                }*/


                if (commandInt != -1) {
                    setChanged();
                    notifyObservers();
//                    System.out.println("Notified Observers");
//                    System.out.println("Command Int: " + commandInt);
                }

        }

    /**
     * calculates the optimal rotation for the boat to move towards finger
     * @param boatHeading the current heading of the boat
     * @param touchAngle the angle of the touch input from 0
     * @param windDirection direction of wind from
     * @return a boolean true if action should be upwind, boolean false if action should be downwind
     */

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
    }

        public int getCommandInt() {
            return commandInt;
        }

        public void setClientID(int clientID) {
            this.clientID = clientID;
        }
    }

