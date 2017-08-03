package seng302.controllers;

import seng302.models.*;
import seng302.utilities.MathUtils;

import java.awt.geom.Line2D;

/**
 * Created by cjd137 on 2/08/17.
 *
 */
public class RoundingMechanics {


    /**
     *
     * @param boat
     * @param compoundMark
     * @param previousMarkCoordinate
     * @return
     */
    public static boolean boatPassedThroughCompoundMark(Boat boat, CompoundMark compoundMark, Coordinate previousMarkCoordinate) {
        Coordinate boatPrevious = boat.getPreviousPosition();
        Coordinate boatCurrent = boat.getCurrentPosition();
        Coordinate lineMark1 = compoundMark.getMark1().getPosition();
        Coordinate lineMark2 = compoundMark.getMark2().getPosition();

        //Creates two lines using the boats current and previous positions, plus the passed in compound mark's two marks.
        Line2D markLine = new Line2D.Double(lineMark1.getLon(), lineMark1.getLat(), lineMark2.getLon(), lineMark2.getLat());
        Line2D boatLine = new Line2D.Double(boatPrevious.getLon(), boatPrevious.getLat(), boatCurrent.getLon(), boatCurrent.getLat());

        Integer markPreviousDir = markLine.relativeCCW(previousMarkCoordinate.getLon(), previousMarkCoordinate.getLat());
        Integer boatPreviousDir = markLine.relativeCCW(boatPrevious.getLon(), boatPrevious.getLat());

        return boatLine.intersectsLine(markLine) && markPreviousDir.equals(boatPreviousDir);
    }

    public static boolean boatPassedMark(Boat boat, CompoundMark mark, String roundingSide, Coordinate previousMarkCoordinate, Coordinate nextMarkCoordinate) {
        Coordinate boatPrevious = boat.getPreviousPosition();
        Coordinate boatCurrent = boat.getCurrentPosition();
        Line2D boatLine = new Line2D.Double(boatPrevious.getLon(), boatPrevious.getLat(), boatCurrent.getLon(), boatCurrent.getLat());



//        Double angle1 = previousMarkCoordinate.headingToCoordinate(mark.getPosition());
        Double angle2 = mark.getPosition().headingToCoordinate(nextMarkCoordinate);
        Double angle1 = mark.getPosition().headingToCoordinate(previousMarkCoordinate);
        Double midway1 = (angle1 + angle2) / 2;
        Double midway2 = (midway1 + 180) % 360;
        Double midway = midway1;
        if(Math.abs(midway2 - angle1) > Math.abs(midway1 - angle1)){
            midway = midway2;
        }
        Coordinate otherPoint = mark.getPosition().coordAt(1000, midway);

        Line2D testingLine = new Line2D.Double(mark.getPosition().getLon(), mark.getPosition().getLat(), otherPoint.getLon(), otherPoint.getLat());

        Integer markPreviousDir = testingLine.relativeCCW(previousMarkCoordinate.getLon(), previousMarkCoordinate.getLat());
        Integer boatPreviousDir = testingLine.relativeCCW(boatPrevious.getLon(), boatPrevious.getLat());

        //        Line2D lineBetweenCurrentAndNextMark = new Line2D.Double(mark.getPosition().getLon(), mark.getPosition().getLat(), nextMarkCoordinate.getLon(), nextMarkCoordinate.getLat());
//        Line2D lineBetweenPreviousAndCurrent = new Line2D.Double(mark.getPosition().getLon(), mark.getPosition().getLat(), previousMarkCoordinate.getLon(), previousMarkCoordinate.getLat());

//        System.out.println(lineBetweenCurrentAndNextMark.getP1()); // Point at the mark that we need to use
        // TODO create a perpendicular line to this line created above to check if the boat crosses over it


        //System.out.println("This mark was passed");

        return boatLine.intersectsLine(testingLine) && markPreviousDir.equals(boatPreviousDir);
    }


}
