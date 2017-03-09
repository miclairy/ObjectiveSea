package seng302;

/**
 * Created by cjd137 on 7/03/17.
 * Class to specify the marks/gates on the course.
 */

public class Mark {
	
	private String name;
    private double lat;
    private double lon;

	private static final String START_MARK = "Start";
    private static final String FINISH_MARK = "Finish";
	
    public Mark(String name, double lat, double lon){
        this.name = name;
        this.lat = lat;
        this.lon = lon;
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

    public boolean isStart(){
        return getName().equals(START_MARK);
    }
    public boolean isFinish(){
        return getName().equals(FINISH_MARK);
    }

}
