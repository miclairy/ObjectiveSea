package seng302.controllers;

import seng302.models.*;

import java.awt.geom.Line2D;

/**
 * Created by cjd137 on 2/08/17.
 *
 */
public class RoundingMechanics {

    /**
     * Checking that a boat has passed through a compound mark
     * @param boat current boat
     * @param compoundMark compound mark the boat is heading to
     * @param previousMarkCoordinate last feature the boat passed
     * @return boolean of whether or not the boat has crossed the compound mark line
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
     * method to check if a boat has gone around the mark
     * @param boat current boat
     * @param mark mark that the boat is heading to
     * @param previousMarkCoordinate last feature the boat went past / through
     * @param nextMarkCoordinate next feature the boat is heading to
     * @return boolean, true if the boat has passed around the mark in the correct direction
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
        return boatLine.intersectsLine(testingCrossingLine) && markPreviousDir.equals(boatPreviousDir);
    }

    /**
     * Checking to see if a boat has gone over the external lines of the compound mark
     * @param boat current boat
     * @param gate compound mark that the boat is trying to pass
     * @param previousMarkCoordinate last feature the boat passed
     * @return boolean, true if the boat passes the external lines of the compound mark going the correct way.
     */
    public static boolean boatPassedThroughExternalGate(Boat boat, CompoundMark gate, Coordinate previousMarkCoordinate) {
        Double angle = gate.getMark1().getPosition().headingToCoordinate(gate.getMark2().getPosition());

        Coordinate gateExteriorCoordinate1 = gate.getMark1().getPosition().coordAt(1000, (angle + 180) % 360);
        Coordinate gateExteriorCoordinate2 = gate.getMark2().getPosition().coordAt(1000, angle);

        CompoundMark getExteriorCompoundMark1 = buildDummyCompoundMark(gate.getMark1().getPosition(), gateExteriorCoordinate1);
        CompoundMark getExteriorCompoundMark2 = buildDummyCompoundMark(gate.getMark2().getPosition(), gateExteriorCoordinate2);

        return boatPassedThroughCompoundMark(boat, getExteriorCompoundMark1, previousMarkCoordinate, false) ||
                boatPassedThroughCompoundMark(boat, getExteriorCompoundMark2, previousMarkCoordinate, false);
    }

    private static CompoundMark buildDummyCompoundMark(Coordinate markCoordinate1, Coordinate markCoordinate2){
        Mark mark1 = new Mark(-1, "Dummy Mark", markCoordinate1);
        Mark mark2 = new Mark(-1, "Dummy Mark", markCoordinate2);

        return new CompoundMark(-1, "Dummy Compound Mark", mark1, mark2);
    }

    /**
     * Finds which way around the boat has to go around a mark
     * @param nextCompoundMark next course feature the boat will head to
     * @param currentGate current gate the boat is going for
     * @param previousMark last feature the boat passed
     * @return boolean, true if the CCW rotation of the feature line is the same as the CCW of the previous feature line rotation
     */
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

    public static void boatHeadingToMark(Boat boat, CompoundMark currentMark, CompoundMark previousMark, CompoundMark nextMark){
        if(RoundingMechanics.boatPassedMark(boat, currentMark, previousMark.getPosition(), nextMark.getPosition())) {
            boat.setLastRoundedMarkIndex(boat.getLastRoundedMarkIndex() + 1);
        }
    }

    /**
     * Logic behind a boat coming from a course feature and heading to a gate.
     * @param boat boat we are currently working with
     * @param currentMark current course feature the boat is heading to
     * @param previousMark last course feature the boat passed
     * @param nextMark course feature the boat is heading to
     */
    public static void boatHeadingToGate(Boat boat, CompoundMark currentMark, CompoundMark previousMark, CompoundMark nextMark){
        if(boat.isInGate()){
            if(boatPassedThroughCompoundMark(boat, currentMark, previousMark.getPosition(), false)){
                boat.setInGate(false);
            } else if(!nextMark.isFinishLine() && boatPassedThroughExternalGate(boat, currentMark, previousMark.getPosition())){
                boat.setLastRoundedMarkIndex(boat.getLastRoundedMarkIndex() + 1);
                boat.setInGate(false);
            }
        } else {
            if(boatPassedThroughCompoundMark(boat, currentMark, previousMark.getPosition(), true)){
                if (nextCompoundMarkAfterGate(nextMark, currentMark, previousMark)) {
                    boat.setLastRoundedMarkIndex(boat.getLastRoundedMarkIndex() + 1);
                    boat.setInGate(false);
                } else{
                    boat.setInGate(true);
                }
            }
        }
    }

}
