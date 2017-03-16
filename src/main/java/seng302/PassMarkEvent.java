package seng302;

/**
 * Created on 7/03/17.
 * A PassMarkEvent represents the event that occurs when a Boat passes a Mark.
 * It also encapsulates the heading the boat has as it leaves the mark.
 */

public class PassMarkEvent extends Event {

    private CompoundMark mark;
    private Boat boat;
    private Double heading;

    PassMarkEvent(int time, CompoundMark mark, Boat boat, Double heading) {
        super(time);
        this.mark = mark;
        this.boat = boat;
        this.heading = heading;
    }

    @Override
    /**
     * This function prints the events as each boat passes a mark/gate with the time and the heading.
     * \u00B0 is unicode character for the degrees symbol.
     */
    public String printEvent(){
        if (this.heading != null) {
            return String.format("%s: %s passed mark %s at %.0f\u00B0",
                    getFormattedTime(),
                    this.boat.getName(),
                    this.mark.getName(),
                    this.heading
            );
        } else {
            return String.format("%s: %s passed mark %s",
                    getFormattedTime(),
                    this.boat.getName(),
                    this.mark.getName()
            );
        }
    }

    /**
     * @return the boat which has passed the mark
     */
    public Boat getInvolvedBoat(){
        return this.boat;
    }
}
