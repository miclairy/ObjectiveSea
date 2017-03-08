package seng302;

/**
 * Created by mjt169 on 7/03/17.
 */

public class PassMarkEvent extends Event {

    private Mark mark;
    private Boat boat;

    PassMarkEvent(int time, Mark mark, Boat boat) {
        super(time);
        this.mark = mark;
        this.boat = boat;
    }

    @Override
    public String printEvent(){
        return String.format("%s: %s passed mark %s",
                getFormattedTime(),
                this.boat.getName(),
                this.mark.getName()
        );
    }

    public Boat getInvolvedBoat(){
        return this.boat;
    }
}
