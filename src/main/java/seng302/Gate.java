package seng302;

/**
 * Created on 16/03/17.
 * A Gate has a marked point at either end
 * The lat and lon properties inherited from CompoundMark define the midpoint of the gate
 */
public class Gate extends CompoundMark {

    private double end1Lat;
    private double end1Lon;
    private double end2Lat;
    private double end2Lon;

    public Gate(String name, double lat1, double lon1, double lat2, double lon2){
        super(name, (lat1 + lat2) / 2, (lon1 + lon2) / 2);
        this.end1Lat = lat1;
        this.end1Lon = lon1;
        this.end2Lat = lat2;
        this.end2Lon = lon2;
    }

    public double getEnd1Lat() {return end1Lat;}

    public double getEnd1Lon() {return end1Lon;}

    public double getEnd2Lat() {return end2Lat;}

    public double getEnd2Lon() {return end2Lon;}
}
