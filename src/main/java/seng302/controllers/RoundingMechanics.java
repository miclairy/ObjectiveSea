package seng302.controllers;

import seng302.models.*;

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
    public static boolean boatPassedThroughCompoundMark(Boat boat, CompoundMark compoundMark, Coordinate previousMarkCoordinate, Boolean forward) {
        Coordinate boatPrevious = boat.getPreviousPosition();
        Coordinate boatCurrent = boat.getCurrentPosition();
        Coordinate lineMark1 = compoundMark.getMark1().getPosition();
        Coordinate lineMark2 = compoundMark.getMark2().getPosition();

        //Creates two lines using the boats current and previous positions, plus the passed in compound mark's two marks.
        Line2D markLine = new Line2D.Double(lineMark1.getLon(), lineMark1.getLat(), lineMark2.getLon(), lineMark2.getLat());
        Line2D boatLine = new Line2D.Double(boatPrevious.getLon(), boatPrevious.getLat(), boatCurrent.getLon(), boatCurrent.getLat());

        Integer markPreviousDir = markLine.relativeCCW(previousMarkCoordinate.getLon(), previousMarkCoordinate.getLat());
        Integer boatPreviousDir = markLine.relativeCCW(boatPrevious.getLon(), boatPrevious.getLat());

        if(forward){
            return boatLine.intersectsLine(markLine) && markPreviousDir.equals(boatPreviousDir);
        } else{
            return boatLine.intersectsLine(markLine) && !markPreviousDir.equals(boatPreviousDir);
        }

    }

    /**
     *
     * @param boat
     * @param mark
     * @param previousMarkCoordinate
     * @param nextMarkCoordinate
     * @return
     */
    public static boolean boatPassedMark(Boat boat, CompoundMark mark, Coordinate previousMarkCoordinate, Coordinate nextMarkCoordinate) {
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
        Coordinate otherPoint = mark.getPosition().coordAt(1, midway);
        Coordinate otherPoint1 = otherPoint.coordAt(2, (midway+180) % 360);

        Line2D testingDirLine = new Line2D.Double(otherPoint.getLon(), otherPoint.getLat(), otherPoint1.getLon(), otherPoint1.getLat());
        Line2D testingCrossingLine = new Line2D.Double(mark.getPosition().getLon(), mark.getPosition().getLat(), otherPoint.getLon(), otherPoint.getLat());

        Integer markPreviousDir = testingDirLine.relativeCCW(previousMarkCoordinate.getLon(), previousMarkCoordinate.getLat());
        Integer boatPreviousDir = testingDirLine.relativeCCW(boatPrevious.getLon(), boatPrevious.getLat());
        if (boatLine.intersectsLine(testingCrossingLine)) {
            System.out.println(boatLine.intersectsLine(testingCrossingLine));
        }
        return boatLine.intersectsLine(testingCrossingLine) && markPreviousDir.equals(boatPreviousDir);
    }

    /**
     *
     * @param boat
     * @param gate
     * @param previousMarkCoordinate
     * @return
     */
    public static boolean boatPassedThroughExternalGate(Boat boat, CompoundMark gate, Coordinate previousMarkCoordinate) {

        //Angle of the line in relation to true 0
        Double angle = gate.getMark1().getPosition().headingToCoordinate(gate.getMark2().getPosition());

        //Two long distance coordinate points from each of gate marks
        Coordinate gateExteriorCoordinate1 = gate.getMark1().getPosition().coordAt(1000, (angle + 180) % 360);
        Coordinate gateExteriorCoordinate2 = gate.getMark2().getPosition().coordAt(1000, angle);

        //Creating both exterior compound marks for the gate
        CompoundMark getExteriorCompoundMark1 = buildDummyCompoundMark(gate.getMark1().getPosition(), gateExteriorCoordinate1);
        CompoundMark getExteriorCompoundMark2 = buildDummyCompoundMark(gate.getMark2().getPosition(), gateExteriorCoordinate2);

        //Returning if the boat passed through the exterior lines
        return boatPassedThroughCompoundMark(boat, getExteriorCompoundMark1, previousMarkCoordinate, false) ||
                boatPassedThroughCompoundMark(boat, getExteriorCompoundMark2, previousMarkCoordinate, false);
    }

    private static CompoundMark buildDummyCompoundMark(Coordinate markCoordinate1, Coordinate markCoordinate2){
        Mark mark1 = new Mark(-1, "Dummy Mark", markCoordinate1);
        Mark mark2 = new Mark(-1, "Dummy Mark", markCoordinate2);

        return new CompoundMark(-1, "Dummy Compound Mark", mark1, mark2);
    }

    public static boolean nextCompoundMarkAfterGate(CompoundMark nextCompoundMark, CompoundMark currentGate, CompoundMark previousMark){
        Coordinate gateMark1 = currentGate.getMark1().getPosition();
        Coordinate gateMark2 = currentGate.getMark2().getPosition();

        Line2D markLine = new Line2D.Double(gateMark1.getLon(), gateMark1.getLat(), gateMark2.getLon(), gateMark2.getLat());

        Coordinate nextCompoundMarkCoordinate = nextCompoundMark.getPosition();
        Integer nextCompoundMarkDir = markLine.relativeCCW(nextCompoundMarkCoordinate.getLon(), nextCompoundMarkCoordinate.getLat());

        Coordinate previousMarkCoordinate = previousMark.getPosition();
        Integer prevMarkDir = markLine.relativeCCW(previousMarkCoordinate.getLon(), previousMarkCoordinate.getLat());

        return !nextCompoundMarkDir.equals(prevMarkDir);
    }

}
