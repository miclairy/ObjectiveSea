package seng302.models;

import javafx.scene.shape.Line;
import seng302.models.CompoundMark;

/**
 * Inheriting from compound mark it is used to determine a start or finish point of a race.
 * A line is drawn between the 2 lat lon.
 */
public class RaceLine extends CompoundMark {
    private Line line;

    public RaceLine(Integer compoundMarkID, String name, Mark mark1, Mark mark2){
        super(compoundMarkID, name, mark1, mark2);
        setPositionAsMidpoint();
    }

    private void setPositionAsMidpoint(){
        Coordinate mark1Coord = mark1.getPosition();
        Coordinate mark2Coord = mark2.getPosition();
        Double halfLat = (mark1Coord.getLat() + mark2Coord.getLat()) / 2;
        Double halfLon = (mark1Coord.getLon() + mark2Coord.getLon()) / 2;
        setPosition(new Coordinate(halfLat, halfLon));
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }
}
