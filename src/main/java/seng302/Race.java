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
    private int totalEventTime;

    public Race(String name, Course course, ArrayList<Boat> competitors) {
        this.name = name;
        this.course = course;
        this.competitors = competitors;
        events = generateEvents(competitors, course);
    }

    private PriorityQueue<Event> generateEvents(ArrayList<Boat> boats, Course course){
        PriorityQueue<Event> eventQueue = new PriorityQueue<>();
        PriorityQueue<PassMarkEvent> finishingOrder = new PriorityQueue<>();
        ArrayList<Mark> marks = course.getCourseOrder();

        eventQueue.add(new GenericRaceEvent(0, "Race Start"));
        for (Boat boat : boats) {
            double speed = boat.getSpeed();
            double timePassed = 0;
            for (int i = 0; i < marks.size(); i++) {
                Mark mark = marks.get(i);
                if (!mark.isStart()) {
                    double distance = course.distanceBetweenMarks(i, i-1);
                    double travelTime = distance / speed;
                    timePassed += travelTime;
                    eventQueue.add(new PassMarkEvent((int)timePassed, mark, boat));
                    if (mark.isFinish()){
                        finishingOrder.add(new PassMarkEvent((int)timePassed, mark, boat));
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

        this.totalEventTime = finishEvent.getTime() + 1;
        eventQueue.add(new RaceEndEvent(totalEventTime, boats,"Race Ended"));

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

    public int getTotalEventTime() {
        return this.totalEventTime;
    }


}
