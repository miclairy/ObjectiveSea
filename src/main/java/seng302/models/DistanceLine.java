package seng302.models;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import seng302.utilities.DisplayUtils;

/**
 * Created by Louis on 14-May-17.
 */
public class DistanceLine {
    private Boat firstBoat;
    private Boat secondBoat;
    private CompoundMark mark;
    private Line line;

    public void setFirstBoat(Boat firstBoat) {
        this.firstBoat = firstBoat;
    }

    public void setSecondBoat(Boat secondBoat) {
        this.secondBoat = secondBoat;
    }

    public void setMark(CompoundMark mark) {
        this.mark = mark;
    }

    public Line getLine() {
        return line;
    }

    public void reCalcLine() {
        if (mark != null){
            if (firstBoat != null && secondBoat != null) { // Line between two boats
                Coordinate midPoint = DisplayUtils.midPointFromTwoCoords(mark.getMark1().getPosition(), mark.getMark2().getPosition());
                Coordinate boatMidPoint = DisplayUtils.midPointFromTwoCoords(firstBoat.getCurrentPosition(), secondBoat.getCurrentPosition());
                createLine(midPoint, boatMidPoint);
            }
            Boat boatToUse = null;
            if (firstBoat == null){
                boatToUse = secondBoat;
            } else if (secondBoat == null) {
                boatToUse = firstBoat;
            }
            if (boatToUse != null) {
                Coordinate midPoint = DisplayUtils.midPointFromTwoCoords(mark.getMark1().getPosition(), mark.getMark2().getPosition());
                createLine(midPoint, boatToUse.getCurrentPosition());
            }
        }
    }

    private void createLine(Coordinate markMidPoint, Coordinate boatMidPoint){
        CanvasCoordinate boatCanvasMidPoint = DisplayUtils.convertFromLatLon(boatMidPoint);
        CanvasCoordinate markPoint = DisplayUtils.convertFromLatLon(markMidPoint);
        line = new Line(
                boatCanvasMidPoint.getX(), boatCanvasMidPoint.getY(),
                markPoint.getX(), markPoint.getY()
        );
        line.setStroke(Color.web("#70aaa2"));
    }
}
