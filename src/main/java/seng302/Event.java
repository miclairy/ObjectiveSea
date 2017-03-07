package seng302;

/**
 * Created by mjt169 on 7/03/17.
 * An event describes a specific thing which happens during a race.
 * E.g. a boat passes a mark, a boat is awarded a foul
 * The display to the user will be based on these events.
 */

public abstract class Event implements Comparable<Event> {

    private int time;

    Event(int time) {
        this.time = time;
    }

    public int getTime(){
        return this.time;
    }

    public int compareTo(Event other) {
        return Integer.compare(this.time, other.time);
    }

    public abstract String printEvent();
}
