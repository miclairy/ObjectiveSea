package seng302.models;

import javafx.scene.shape.Line;

/**
 * Created by swa158 on 15/05/17.
 */
public class Layline {

    private double heading1;
    private double heading2;
    private Line layline1;
    private Line layline2;


    //TODO take gemma's math from updateLocation to implement moving laylines
//    public double calculateLayLineLength(CompoundMark nextMark, CompoundMark currentMark, double alphaAngle, double twa) {
//        double lengthOfLeg = currentMark.getPosition().greaterCircleDistance(nextMark.getPosition());
//        double betaAngle = (2*twa) - alphaAngle;
//        double lengthOfTack = ((lengthOfLeg* Math.sin(Math.toRadians(betaAngle)))/Math.sin(Math.toRadians(180 - 2*twa)))/2.0;
//        return lengthOfTack;
//    }

    public double getHeading1() { return heading1; }

    public void setHeading1(double heading1) { this.heading1 = heading1; }

    public double getHeading2() { return heading2; }

    public void setHeading2(double heading2) { this.heading2 = heading2; }

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
