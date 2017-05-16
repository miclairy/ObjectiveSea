package seng302.models;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import seng302.utilities.DisplayUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Louis on 14-May-17.
 *
 */

public class DistanceLine {
    private Boat firstBoat;
    private Boat secondBoat;
    private CompoundMark mark;
    private Set<Line> lines;
    private double distanceBetweenBoats;

    public DistanceLine(){
        lines = new HashSet<>();
    }

    public void setFirstBoat(Boat firstBoat) {
        this.firstBoat = firstBoat;
    }

    public Boat getFirstBoat(){ return firstBoat;}

    public void setSecondBoat(Boat secondBoat) {
        this.secondBoat = secondBoat;
    }

    public Boat getSecondBoat(){ return secondBoat;}

    public void setMark(CompoundMark mark) {
        this.mark = mark;
    }

    public Collection<Line> getLines() {
        return Collections.unmodifiableCollection(lines);
    }

    public void reCalcLine() {
        lines.clear();
        if (mark != null){
            if (firstBoat != null && secondBoat != null) { // Line between two boats
                Coordinate midPoint = mark.getPosition();
                if (mark.hasTwoMarks()) {
                    midPoint = DisplayUtils.midPointFromTwoCoords(mark.getMark1().getPosition(), mark.getMark2().getPosition());
                }
                createLinesBetweenTwoBoats(midPoint);
            }
            Boat boatToUse = null;
            if (firstBoat == null){
                boatToUse = secondBoat;
            } else if (secondBoat == null) {
                boatToUse = firstBoat;
            }
            if (boatToUse != null) {
                Coordinate midPoint = mark.getPosition();
                if (mark.hasTwoMarks()) {
                    midPoint = DisplayUtils.midPointFromTwoCoords(mark.getMark1().getPosition(), mark.getMark2().getPosition());
                }
                createLine(midPoint, boatToUse.getCurrentPosition());
            }
        }
    }

    private void createLinesBetweenTwoBoats(Coordinate target){
        Coordinate boatMidPoint = DisplayUtils.midPointFromTwoCoords(firstBoat.getCurrentPosition(), secondBoat.getCurrentPosition());

        InfiniteLine infiniteLine = new InfiniteLine(target, boatMidPoint);
        Coordinate closestPoint1 = infiniteLine.closestPoint(firstBoat.getCurrentPosition());
        Coordinate closestPoint2 = infiniteLine.closestPoint(secondBoat.getCurrentPosition());
        double dist1 = target.greaterCircleDistance(closestPoint1);
        double dist2 = target.greaterCircleDistance(closestPoint2);
        if(dist1 < dist2){
            createLine(target, closestPoint2);
        } else{
            createLine(target, closestPoint1);
        }
        createLine(firstBoat.getCurrentPosition(), closestPoint1);
        createLine(secondBoat.getCurrentPosition(), closestPoint2);
    }

    private void createLine(Coordinate point1, Coordinate point2){
        CanvasCoordinate canvasPoint1 = DisplayUtils.convertFromLatLon(point1);
        CanvasCoordinate canvasPoint2 = DisplayUtils.convertFromLatLon(point2);
        Line line = new Line(
                canvasPoint1.getX(), canvasPoint1.getY(),
                canvasPoint2.getX(), canvasPoint2.getY()
        );
        line.setStroke(Color.web("#70aaa2"));
        lines.add(line);
    }

//
//    private void updateDistanceBetweenBoats(){
//        Coordinate firstBoatOnLine;
//        Coordinate secondBoatOnLine;
//        distanceBetweenBoats = TimeUtils.calcDistance(firstBoatOnLine, secondBoatOnLine);
//    }
//
//    private double getDistanceBetweenBoats(){
//        return distanceBetweenBoats;
//    }
}
