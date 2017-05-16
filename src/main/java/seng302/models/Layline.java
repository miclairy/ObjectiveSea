package seng302.models;

import javafx.scene.shape.Line;

/**
 * Created by swa158 on 15/05/17.
 */
public class Layline {

    private double angle1;
    private double angle2;
    private Line layline1;
    private Line layline2;


    //TODO take gemma's math from updateLocation to implement moving laylines
//    public double calculateLayLineLength(CompoundMark nextMark, CompoundMark currentMark, double alphaAngle, double twa) {
//        double lengthOfLeg = currentMark.getPosition().greaterCircleDistance(nextMark.getPosition());
//        double betaAngle = (2*twa) - alphaAngle;
//        double lengthOfTack = ((lengthOfLeg* Math.sin(Math.toRadians(betaAngle)))/Math.sin(Math.toRadians(180 - 2*twa)))/2.0;
//        return lengthOfTack;
//    }

    public double getAngle1() { return angle1; }

    public void setAngle1(double angle1) { this.angle1 = angle1; }

    public double getAngle2() { return angle2; }

    public void setAngle2(double angle2) { this.angle2 = angle2; }

    public Line getLayline1() {
        return layline1;
    }

    public void setLayline1(Line layline1) {
        this.layline1 = layline1;
    }

    public Line getLayline2() {
        return layline2;
    }

    public void setLayline2(Line layline2) {
        this.layline2 = layline2;
    }


}
