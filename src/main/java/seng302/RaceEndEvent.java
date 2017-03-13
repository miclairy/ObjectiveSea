package seng302;

import java.util.ArrayList;

/**
 * Created on 3/9/2017.
 * Extends event.
 * Occurs when each boat has finished the race.
 * TODO: consider boats that DNF (do not finish)
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
