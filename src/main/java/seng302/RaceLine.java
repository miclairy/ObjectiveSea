package seng302;

import javafx.scene.shape.Line;

/**
 * Inheriting from compound mark it is used to determine a start or finish point of a race.
 * A line is drawn between the 2 lat lon.
 */
public class RaceLine extends CompoundMark{
    private double end1Lat;
    private double end1Lon;
    private double end2Lat;
    private double end2Lon;
    private Line line;

    public RaceLine(String name, double lat1, double lon1, double lat2, double lon2){
        super(name, (lat1 + lat2) / 2, (lon1 + lon2) / 2);

        this.end1Lat = lat1;
        this.end1Lon = lon1;
        this.end2Lat = lat2;
        this.end2Lon = lon2;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public double getEnd1Lat() {
        return end1Lat;
    }

    public double getEnd1Lon() {
        return end1Lon;
    }

    public double getEnd2Lat() {
        return end2Lat;
    }

    public double getEnd2Lon() {
        return end2Lon;
    }
}
