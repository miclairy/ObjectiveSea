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

        return String.format("%s passed mark %s at %d:%sm",  this.boat.getName(), this.mark.getName(), minuteTime, formattedSecondTime);
    }
}
