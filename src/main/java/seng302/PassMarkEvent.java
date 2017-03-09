package seng302;

/**
 * Created by mjt169 on 7/03/17.
 */

public class PassMarkEvent extends Event {

    private Mark mark;
    private Boat boat;
    private Double heading;

    PassMarkEvent(int time, Mark mark, Boat boat, Double heading) {
        super(time);
        this.mark = mark;
        this.boat = boat;
        this.heading = heading;
    }

    @Override
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

    public Boat getInvolvedBoat(){
        return this.boat;
    }
}
