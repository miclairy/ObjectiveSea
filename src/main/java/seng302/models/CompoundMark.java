package seng302.models;

import javafx.scene.shape.Circle;
import seng302.models.Coordinate;

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
    private Integer markID;
    private Coordinate position;
    private MarkType type;
    private ArrayList<Circle> icons = new ArrayList<>();

    public CompoundMark(String name, Integer markID, double lat, double lon){
        this.name = name;
        this.markID = markID;
        this.position = new Coordinate(lat, lon);
        this.type = MarkType.NORMAL;
    }


    public Integer getMarkID(){
        return markID;
    }
    public String getName(){
        return this.name;
    }

    public double getLon() {
        return position.getLon();
    }

    public double getLat() {
        return position.getLat();
    }

    public Coordinate getPosition() {
        return this.position;
    }

    public void setMarkAsStart(){
        this.type = MarkType.START;
    }

    public void setMarkAsFinish(){
        this.type = MarkType.FINISH;
    }

    public boolean isStartLine(){
        return this.type == MarkType.START;
    }
    public boolean isFinishLine(){
        return this.type == MarkType.FINISH;
    }

    public void addIcon(Circle circle) {
        icons.add(circle);
    }

    public ArrayList<Circle> getIcons() {
        return icons;
    }
}
