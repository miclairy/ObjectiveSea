package seng302;

/**
 * Created by cjd137 on 7/03/17.
 * Class to specify the marks/gates on the course and to calculate distances and degrees from one mark/gate to the other
 */

public class Mark {
	
	private String name;
	
    public Mark(String name){
        this.name = name;
    }
	
    public String getName(){
        return this.name;
    }

}
