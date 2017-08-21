package seng302.controllers;

import javafx.scene.input.TouchEvent;

/**
 * Created by cjd137 on 22/08/17.
 */
public class TouchController extends Controller{

    @Override
    public void rotateBoat(TouchEvent touchEvent) {
        System.out.println(touchEvent.getTouchPoint().getSceneX() + " x " + touchEvent.getTouchPoint().getScreenY());
    }


}
