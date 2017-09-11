package seng302.controllers;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import seng302.data.BoatAction;
import seng302.models.*;
import seng302.utilities.MathUtils;
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

            touchPane.setOnScrollFinished(swipe -> {
                CanvasCoordinate swipeEnd = new CanvasCoordinate(swipe.getScreenX(), swipe.getScreenY());
                displayTouchController.displaySwipe(swipeEnd, swipeStart);
                double swipeBearing = MathUtils.getHeadingBetweenTwoCoodinates(swipeStart, swipeEnd);
                swipeAction(swipe, swipeBearing);
            });

        }

        private void swipeAction(ScrollEvent swipe, double swipeBearing){
            double boatHeading = race.getBoatById(clientID).getHeading();
            double headingDifference = abs(boatHeading - swipeBearing) % 180;
            Group root = (Group) scene.lookup("#root");
            if (root.getTransforms().size() > 1){
                headingDifference = swipeBearing;
            }
            System.out.println(boatHeading + " swipe "  + swipeBearing + " difference " + headingDifference);
            if (headingDifference <= 15 || headingDifference >= 165){
                commandInt = BoatAction.SAILS_IN.getType();
            } else {
                commandInt = BoatAction.TACK_GYBE.getType();
            }
            setChanged();
            notifyObservers();
        }

        private void checkTouchMoved(TouchEvent touchEvent) {
            Boat playersBoat = race.getBoatById(clientID);
            if (touchEvent.getTouchPoints().size() == 1 && !DisplayUtils.externalTouchEvent) {
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

                //downWind towards wind arrow - 6
                //upWind away from wind arrow - 5

                double oppositeWindAngle = windAngle - 180;
                if(oppositeWindAngle < 0) {
                    oppositeWindAngle += 360;
                }
                double newWindAngle;
                double newBoatHeading;
                double newTouchAngle;


                if (!((boatHeading > (touchAngle - 2)) && (boatHeading < (touchAngle + 2)))) {
                    if ((((boatHeading - touchAngle) < 180) && ((boatHeading - touchAngle) > 0)) ||
                            (((boatHeading - touchAngle) < -180) && ((boatHeading - touchAngle) > -360))) {             //Anti Clockwise
                        if (boatHeading > touchAngle) {
                            commandInt = changeRotationDirection(windAngle, oppositeWindAngle, boatHeading, touchAngle);
                        } else if (boatHeading < touchAngle) {
                            if (windAngle < 180) {
                                newWindAngle = windAngle + 360;
                            } else {
                                newWindAngle = windAngle;
                                oppositeWindAngle += 360;
                            }
                            newBoatHeading = boatHeading + 360;

                            commandInt = changeRotationDirection(newWindAngle, oppositeWindAngle, newBoatHeading, touchAngle);
                        }
                    } else if ((((boatHeading - touchAngle) > 180) && ((boatHeading - touchAngle) < 360)) ||
                            (((boatHeading - touchAngle) > -180) && ((boatHeading - touchAngle) < 0))) {                //Clockwise
                        if(boatHeading < touchAngle) {
                            commandInt = changeOppositeRotationDirection(windAngle, oppositeWindAngle, boatHeading, touchAngle);
                        } else if(boatHeading > touchAngle) {
                            if (windAngle < 180) {
                                newWindAngle = windAngle + 360;
                            } else {
                                newWindAngle = windAngle;
                                oppositeWindAngle += 360;
                            }

                            newTouchAngle = touchAngle + 360;

                            commandInt = changeOppositeRotationDirection(newWindAngle, oppositeWindAngle, boatHeading, newTouchAngle);
                        }
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


        public int changeRotationDirection(double windAngle, double oppositeWindAngle, double boatHeading, double touchAngle) {

            int direction = -1;

            if(boatHeading > windAngle && touchAngle < windAngle) {
                direction = 6;
            } else if(boatHeading > oppositeWindAngle && touchAngle < oppositeWindAngle) {
                direction = 5;
            } else if(((touchAngle - windAngle) < 180 && (touchAngle - windAngle) > 0) || ((touchAngle - windAngle) < -180 && (touchAngle - windAngle) > -360)) {
                direction = 6;
            } else if(((touchAngle - oppositeWindAngle) < 180 && (touchAngle - oppositeWindAngle) > 0) || ((touchAngle - oppositeWindAngle) < -180 && (touchAngle - oppositeWindAngle) > -360)) {
                direction = 5;
            }

            return direction;
        }

        public int changeOppositeRotationDirection(double windAngle, double oppositeWindAngle, double boatHeading, double touchAngle) {

            int direction = -1;

            if (boatHeading < windAngle && touchAngle > windAngle) {
                direction = 6;
            } else if (boatHeading < oppositeWindAngle && touchAngle > oppositeWindAngle) {
                direction = 5;
            } else if (((touchAngle - windAngle) < 180 && (touchAngle - windAngle) > 0) || ((touchAngle - windAngle) < -180 && (touchAngle - windAngle) > -360)) {
                direction = 5;
            } else if (((touchAngle - oppositeWindAngle) < 180 && (touchAngle - oppositeWindAngle) > 0) || ((touchAngle - oppositeWindAngle) < -180 && (touchAngle - oppositeWindAngle) > -360)) {
                direction = 6;
            }
            return direction;
        }

        public int getCommandInt() {
            return commandInt;
        }

        public void setClientID(int clientID) {
            this.clientID = clientID;
        }

}

