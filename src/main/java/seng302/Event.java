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

    protected String getFormattedTime(){
        int secondTime = 0;
        int minuteTime = 0;
        String formattedSecondTime;

        if(this.getTime() > 59) {
            minuteTime = this.getTime() / 60;
            secondTime = this.getTime() - (minuteTime * 60);
        }
        else {
            secondTime = this.getTime();
        }

        formattedSecondTime = Integer.toString(secondTime);

        if(secondTime < 10)
        {
            formattedSecondTime = "0" + formattedSecondTime.toString();
        }

        return String.format("%dm%ss", minuteTime, formattedSecondTime);
    }

    public abstract String printEvent();
}
