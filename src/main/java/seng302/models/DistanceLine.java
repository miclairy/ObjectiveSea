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

    public void updateLine() {
        if (mark != null && firstBoat != null && secondBoat != null) {
            Coordinate markCoord = mark.getPosition();
            CanvasCoordinate boatPosition = DisplayUtils.convertFromLatLon(firstBoat.getCurrentPosition());
            CanvasCoordinate markPosition = DisplayUtils.convertFromLatLon(markCoord);
            line = new Line(
                    boatPosition.getX(), boatPosition.getY(),
                    markPosition.getX(), markPosition.getY()
            );
            line.setStroke(Color.web("#70aaa2"));
        }
    }
}
