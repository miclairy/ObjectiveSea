package seng302;


/**
 * Created on 3/9/2017.
 * Extends Event.
 * A GenericRaceEvent is a very simple event which requires nothing other than a timestamp and a message
 * An example event would be "Race Started" which requires no additional information
 */
public class GenericRaceEvent extends Event {

    private String message;

    public GenericRaceEvent(int time, String eventMessage){
        super(time);
        this.message = eventMessage;
    }

    /**
     * Print out the event as 'XXmYYs: This is the event message'
     */
    @Override
    public String printEvent(){
        return String.format("%s: %s", getFormattedTime(), message);
    }
}
