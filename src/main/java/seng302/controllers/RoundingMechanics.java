package seng302.controllers;

import seng302.models.*;

import java.awt.geom.Line2D;

/**
 * Created by cjd137 on 2/08/17.
 */
public class RoundingMechanics {



    public static boolean boatPassedThroughCompoundMark(Boat boat, CompoundMark compoundMark, Coordinate previousMarkCoordinate) {
        Coordinate boatPrevious = boat.getPreviousPosition();
        Coordinate boatCurrent = boat.getCurrentPosition();
        Coordinate lineMark1 = compoundMark.getMark1().getPosition();
        Coordinate lineMark2 = compoundMark.getMark2().getPosition();

        Line2D markLine = new Line2D.Double(lineMark1.getLon(), lineMark1.getLat(), lineMark2.getLon(), lineMark2.getLat());
        Line2D boatLine = new Line2D.Double(boatPrevious.getLon(), boatPrevious.getLat(), boatCurrent.getLon(), boatCurrent.getLat());

        Integer markPreviousDir = markLine.relativeCCW(previousMarkCoordinate.getLon(), previousMarkCoordinate.getLat());
        Integer boatPreviousDir = markLine.relativeCCW(boatPrevious.getLon(), boatPrevious.getLat());

        return boatLine.intersectsLine(markLine) && markPreviousDir.equals(boatPreviousDir);
    }

}
