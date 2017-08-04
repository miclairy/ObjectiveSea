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

    /**
     *
     * @param boat
     * @param mark
     * @param roundingSide
     * @param previousMarkCoordinate
     * @param nextMarkCoordinate
     * @return
     */
    public static boolean boatPassedMark(Boat boat, CompoundMark mark, String roundingSide, Coordinate previousMarkCoordinate, Coordinate nextMarkCoordinate) {
        Coordinate boatPrevious = boat.getPreviousPosition();
        Coordinate boatCurrent = boat.getCurrentPosition();
        Line2D boatLine = new Line2D.Double(boatPrevious.getLon(), boatPrevious.getLat(), boatCurrent.getLon(), boatCurrent.getLat());

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

        return boatLine.intersectsLine(testingLine) && markPreviousDir.equals(boatPreviousDir);
    }

    /**
     *
     * @param boat
     * @param gate
     * @param previousMarkCoordinate
     * @param nextCompoundMark
     * @return
     */
    public static boolean boatPassedGate(Boat boat, CompoundMark gate, Coordinate previousMarkCoordinate, CompoundMark nextCompoundMark) {
        boolean hasPassedGate = false;
        Coordinate boatPrevious = boat.getPreviousPosition();
        Coordinate boatCurrent = boat.getCurrentPosition();
        Line2D boatLine = new Line2D.Double(boatPrevious.getLon(), boatPrevious.getLat(), boatCurrent.getLon(), boatCurrent.getLat());

        Line2D gateLine = new Line2D.Double(gate.getMark1().getPosition().getLon(), gate.getMark1().getPosition().getLat(), gate.getMark2().getPosition().getLon(), gate.getMark2().getPosition().getLat());

        Double angle = gate.getMark1().getPosition().headingToCoordinate(gate.getMark2().getPosition());

        //Two long distance coordinate points from each of gate marks
        Coordinate gateExteriorCoordinate1 = gate.getMark1().getPosition().coordAt(1000, (angle + 180) % 360);
        Coordinate gateExteriorCoordinate2 = gate.getMark2().getPosition().coordAt(1000, angle);

        //Two exterior infinite lines
        Line2D gateExteriorLine1 = new Line2D.Double(gate.getMark1().getPosition().getLon(), gate.getMark1().getPosition().getLat(), gateExteriorCoordinate1.getLon(), gateExteriorCoordinate1.getLat());
        Line2D gateExteriorLine2 = new Line2D.Double(gate.getMark2().getPosition().getLon(), gate.getMark2().getPosition().getLat(), gateExteriorCoordinate2.getLon(), gateExteriorCoordinate2.getLat());

        //Boat crossing Gate line
        int gatePreviousDir = gateLine.relativeCCW(previousMarkCoordinate.getLon(), previousMarkCoordinate.getLat());
        int boatPreviousDir = gateLine.relativeCCW(boatPrevious.getLon(), boatPrevious.getLat());

        //Boat crossing Infinite Gate line 1
        int gatePreviousDir1 = gateExteriorLine1.relativeCCW(previousMarkCoordinate.getLon(), previousMarkCoordinate.getLat());
        int boatPreviousDir1 = gateExteriorLine1.relativeCCW(boatPrevious.getLon(), boatPrevious.getLat());

        int gatePreviousDir2 = gateExteriorLine2.relativeCCW(previousMarkCoordinate.getLon(), previousMarkCoordinate.getLat());
        int boatPreviousDir2 = gateExteriorLine2.relativeCCW(boatPrevious.getLon(), boatPrevious.getLat());


        if(gatePreviousDir == boatPreviousDir && boatLine.intersectsLine(gateLine) && !boat.isInGate()) {
            boat.setInGate(true);
        }
        if (gatePreviousDir != boatPreviousDir && boatLine.intersectsLine(gateLine) && boat.isInGate()){
            boat.setInGate(false);
        }
        if(!nextCompoundMark.isFinishLine()) {
            if (boatLine.intersectsLine(gateExteriorLine1) && boat.isInGate() && gatePreviousDir1 != boatPreviousDir1) {
                hasPassedGate = true;
                boat.setInGate(false);
            }
            if (boatLine.intersectsLine(gateExteriorLine2) && boat.isInGate() && gatePreviousDir2 != boatPreviousDir2) {
                hasPassedGate = true;
                boat.setInGate(false);
            }
        } else if (nextCompoundMark.isFinishLine() && boat.isInGate()) {
            hasPassedGate = true;
        }


        return hasPassedGate;
    }


}
