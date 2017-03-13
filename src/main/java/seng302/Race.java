package seng302;

import java.util.*;

/**
 * Created on 7/03/17.
 * A Race encompasses a course and some competitors, and the events that occur throughout the race.
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

    /**
     * Generates the events that occur in the race using as simple methodology as possible
     * The queue will have a 'Race Start' event at it's head, and a 'Race Finish' event at it's tail
     * Between them will be a series of PassMarkEvents for every boat passing every mark in the course, calculated
     * using a constant speed for each boat (as defined in the Boat object).
     * @param boats - each boat in the race
     * @param course - the course with the definition of marks for the race
     * @return a priority queue of race events
     */
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
                    double distance = course.distanceBetweenMarks(i-1 , i);
                    double travelTime = distance / (speed / 3600);
                    timePassed += travelTime;
                    if (mark.isFinish()) {
                        eventQueue.add(new PassMarkEvent((int)timePassed, mark, boat, null));
                        finishingOrder.add(new PassMarkEvent((int)timePassed, mark, boat, null));
                    } else {
                        double heading = course.headingsBetweenMarks(i, i + 1);
                        eventQueue.add(new PassMarkEvent((int)timePassed, mark, boat, heading));
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

        /** Set the total event time as 1 second longer than the last boat takes to finish to ensure events are
         * are displayed in a sensible order, i.e. 'Race Ended' event is always displayed last*/
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
