package seng302;


/**
 * Created by Mikey on 3/9/2017.
 */
public class GenericRaceEvent extends Event {

    private String message;

    public GenericRaceEvent(int time, String eventMessage){
        super(time);
        this.message = eventMessage;
    }

    @Override
    public String printEvent(){
        return String.format("%s: %s", getFormattedTime(), message);
    }
}
