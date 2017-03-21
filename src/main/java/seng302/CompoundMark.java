package seng302;

import javafx.scene.shape.Circle;

import java.util.ArrayList;

/**
 * Created on 7/03/17.
 * Class to specify the marks/gates on the course.
 */

public class CompoundMark {

    private enum MarkType {
        START, FINISH, NORMAL
    }
	
	private String name;
    private double lat;
    private double lon;
    private MarkType type;
    private ArrayList<Circle> icons = new ArrayList<>();

    public CompoundMark(String name, double lat, double lon){
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.type = MarkType.NORMAL;
    }
	
    public String getName(){
        return this.name;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public void setMarkAsStart(){
        this.type = MarkType.START;
    }

    public void setMarkAsFinish(){
        this.type = MarkType.FINISH;
    }

    public boolean isStart(){
        return this.type == MarkType.START;
    }
    public boolean isFinish(){
        return this.type == MarkType.FINISH;
    }

    public void addIcon(Circle circle) {
        icons.add(circle);
    }

    public ArrayList<Circle> getIcons() {
        return icons;
    }
}
