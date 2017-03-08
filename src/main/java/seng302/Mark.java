package seng302;

/**
 * Created by cjd137 on 7/03/17.
 * Class to specify the marks/gates on the course.
 */

public class Mark {
	
	private String name;

	private static final String START_MARK = "Start";
    private static final String FINISH_MARK = "Finish";
	
    public Mark(String name){
        this.name = name;
    }
	
    public String getName(){
        return this.name;
    }

    public boolean isStart(){
        return getName().equals(START_MARK);
    }
    public boolean isFinish(){
        return getName().equals(FINISH_MARK);
    }

}
