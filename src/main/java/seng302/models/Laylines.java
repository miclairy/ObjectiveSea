package seng302.models;

import javafx.scene.Group;
import javafx.scene.shape.Line;
import seng302.utilities.MathUtils;

/**
 * Created by swa158 on 15/05/17.
 */
public class Laylines {

    private double angle1;
    private double angle2;
    private Line layline1;
    private Line layline2;

    public void removeDrawnLines(Group root) {
        if (layline1 != null && layline2 != null) {
            root.getChildren().remove(layline1);
            root.getChildren().remove(layline2);
        }
        layline1= null;
        layline2= null;
    }

    public double getAngle1() { return angle1; }

    public double getAngle2() { return angle2; }

    public void setDrawnlines(Line layline1, Line layline2) {
        this.layline1 = layline1;
        this.layline2 = layline2;
    }

    /**
     * Calculates the layline heading of a boat
     * @param TWD, boatHeading, twa
     */
    public void calculateLaylineAngle(double TWD, CompoundMark lastMark, CompoundMark nextMark, PolarTable polarTable) {
        Coordinate lastMarkPosition = lastMark.getPosition();
        Coordinate nextMarkPosition = nextMark.getPosition();
        double markBearing = lastMarkPosition.headingToCoordinate(nextMarkPosition);
        boolean upwind = MathUtils.pointBetweenTwoAngle(TWD, 90, markBearing);
        double TWA = polarTable.getOptimumTWA(upwind);
        this.angle1 = TWD + TWA;
        this.angle2 = TWD - TWA;
    }
}
