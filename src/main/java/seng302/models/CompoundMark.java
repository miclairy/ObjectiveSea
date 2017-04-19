package seng302.models;

/**
 * Created on 7/03/17.
 * Class to specify the marks/gates on the course.
 */

public class CompoundMark {

    private enum MarkType {
        START, FINISH, NORMAL
    }
	protected Mark mark1, mark2;
	private String name;
    private Integer compoundMarkID;
    private MarkType type;
    private Coordinate position;

    public CompoundMark(Integer compoundMarkID, String name, Mark mark1, Mark mark2){
        this.name = name;
        this.compoundMarkID = compoundMarkID;
        this.type = MarkType.NORMAL;
        this.mark1 = mark1;
        this.mark2 = mark2;
        position = mark1.getPosition();
    }

    public CompoundMark(Integer compoundMarkID, String name, Mark mark1){
        this(compoundMarkID, name, mark1, null);
    }

    public boolean hasTwoMarks(){
        return mark2 != null;
    }

    public Integer getCompoundMarkID(){
        return compoundMarkID;
    }

    public String getName(){
        return this.name;
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

    public void setPosition(Coordinate position) {
        this.position = position;
    }

    public Coordinate getPosition() {
        return position;
    }

    public Mark getMark1() {
        return mark1;
    }

    public Mark getMark2() {
        return mark2;
    }
}
