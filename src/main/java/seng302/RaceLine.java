package seng302;

import javafx.scene.shape.Line;

/**
 * Used to determine a stort or finish point of a race. A line is drawn between the 2 lat lon.
 */
public class RaceLine extends CompoundMark{
    private Coordinate end1;
    private Coordinate end2;
    private Line line;

    public RaceLine(String name, double lat1, double lon1, double lat2, double lon2){
        super(name, (lat1 + lat2) / 2, (lon1 + lon2) / 2);
        this.end1 = new Coordinate(lat1, lon1);
        this.end2 = new Coordinate(lat2, lon2);
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public double getEnd1Lat() {return end1.getLat();}

    public double getEnd1Lon() {return end1.getLon();}

    public double getEnd2Lat() {return end2.getLat();}

    public double getEnd2Lon() {return end2.getLon();}

    public Coordinate getEnd1() {
        return end1;
    }

    public Coordinate getEnd2() {
        return end2;
    }
}
