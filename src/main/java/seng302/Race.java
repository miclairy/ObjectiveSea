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

        Random ran = new Random();
        int max = baseTimeFactor + maxVariance;
        int min = baseTimeFactor - maxVariance;

        for (Boat boat : boats) {
            int timePassed = 0;
            for (Mark mark : marks) {
                int travelTime = ran.nextInt(max - min + 1) + min;
                timePassed += travelTime;
                eventQueue.add(new PassMarkEvent(timePassed, mark, boat));
            }
        }
        return eventQueue;
    }

    public PriorityQueue<Event> getEvents(){
        return this.events;
    }


}
