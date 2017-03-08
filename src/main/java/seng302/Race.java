package seng302;

import java.util.*;

/**
 * Created by mjt169 on 7/03/17.
 * A Race encompasses a course and some competitors, and the events occur throughout the race.
 * For now, events are randomly pre-computed and stored in a queue ready to be read by other classes such as Display
 */
public class Race {

    private String name;
    private Course course;
    private ArrayList<Boat> competitors;
    private PriorityQueue<Event> events;

    private static final int baseTimeFactor = 100;
    private static final int maxVariance = 51;

    public Race(String name, Course course, ArrayList<Boat> competitors) {
        this.name = name;
        this.course = course;
        this.competitors = competitors;
        events = generateEvents(competitors, this.course.getMarks());
    }

    private PriorityQueue<Event> generateEvents(ArrayList<Boat> boats, ArrayList<Mark> marks){
        PriorityQueue<Event> eventQueue = new PriorityQueue<>();
        PriorityQueue<PassMarkEvent> finishingOrder = new PriorityQueue<>();

        Random ran = new Random();
        int max = baseTimeFactor + maxVariance;
        int min = baseTimeFactor - maxVariance;

        eventQueue.add(new GenericRaceEvent(0, "Race Start"));
        for (Boat boat : boats) {
            int timePassed = 0;
            for (Mark mark : marks) {
                if (!mark.isStart()) {
                    int travelTime = ran.nextInt(max - min + 1) + min;
                    timePassed += travelTime;
                    eventQueue.add(new PassMarkEvent(timePassed, mark, boat));
                    if (mark.isFinish()){
                        finishingOrder.add(new PassMarkEvent(timePassed, mark, boat));
                    }
                }
            }
        }

        int place = 1;
        PassMarkEvent finishEvent;
        do  {
            finishEvent = finishingOrder.poll();
            finishEvent.getInvolvedBoat().setFinishingPlace(place);
            place++;
        } while (finishingOrder.size() > 0);

        eventQueue.add(new RaceEndEvent(finishEvent.getTime() + 1, boats,"Race Ended"));

        return eventQueue;
    }

    public ArrayList<Boat> getCompetitors() {
        return this.competitors;
    }

    public String getName() {
        return name;
    }

    public PriorityQueue<Event> getEvents(){
        return this.events;
    }


}
