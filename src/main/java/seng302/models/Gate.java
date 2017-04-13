package seng302.models;

import seng302.models.CompoundMark;

/**
 * Created on 16/03/17.
 * A Gate has a marked point at either end
 * The lat and lon properties inherited from CompoundMark define the midpoint of the gate
 */
public class Gate extends CompoundMark {

    private Coordinate end1;

    private Coordinate end2;
    public Gate(String name, Integer markID, double lat1, double lon1, double lat2, double lon2){
        super(name, markID, lat1, lon1);
        this.end1 = new Coordinate(lat1, lon1);
        this.end2 = new Coordinate(lat2, lon2);
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
