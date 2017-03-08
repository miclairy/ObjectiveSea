package seng302;

import java.util.ArrayList;

/**
 * Created by Mikey on 3/9/2017.
 */
public class RaceEndEvent extends Event {

    private String message;
    private ArrayList<Boat> finishers;

    public RaceEndEvent(int time, ArrayList<Boat> finishers, String eventMessage){
        super(time);
        this.finishers = finishers;
        this.message = eventMessage;
    }

    @Override
    public String printEvent(){
        return String.format("%s: %s", getFormattedTime(), message);
    }

    public ArrayList<Boat> getFinishers(){
        return this.finishers;
    }

}
