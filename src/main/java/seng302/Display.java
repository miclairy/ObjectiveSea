package seng302;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mjt169 on 6/03/17.
 * Edited by cjd137 on 7/03/17.
 * Class to manage the output display.
 * For now this will be simple text-based output to terminal
 */

public class Display {

    public static void displayRace(Race race){
        System.out.printf( "%s\n\n", race.getName());
        printStartersList(race.getCompetitors());
        System.out.println();
        printEventQueue(race.getEvents());
    }

    public static void printStartersList(ArrayList<Boat> starters) {
        System.out.println("Boats in this race:");
        for (Boat boat : starters) {
            System.out.println(boat.getName());
        }
    }

    public static void printFinishersList(ArrayList<Boat> finishers) {
        finishers.sort(BoatUtils.orderByPlacing);

        System.out.println("Finishing order:");
        for (Boat boat : finishers) {
            System.out.printf("%d. %s\n", boat.getFinishingPlace(), boat.getName());
        }
    }
    
    public static void printMarksList(ArrayList<Boat> markPassers, ArrayList<Mark> marks, int markNumber) {
    	markPassers.sort(BoatUtils.orderByPlacing);
    	
    	System.out.printf("Mark: %s\n", marks.get(markNumber).getName());
    	for (Boat boat : markPassers) {
    		System.out.printf("%d. %s\n", boat.getFinishingPlace(), boat.getName());
    	}	
    }

    public static void printEventQueue(PriorityQueue<Event> events) {
        long lastDelay = 0;
        final Timer timer = new Timer();
        while(events.size() != 0) {
            Event currentEvent = events.poll();
            long delay = currentEvent.getTime() * Config.TIME_SCALE * 100;
            final TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    System.out.printf("%s\n", currentEvent.printEvent());
                    if (currentEvent instanceof RaceEndEvent) {
                        System.out.println();
                        printFinishersList(((RaceEndEvent) currentEvent).getFinishers());
                    }
                }
            };
            lastDelay = delay;
            timer.schedule(task, delay);
        }
        final TimerTask cleanup = new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                timer.purge();
            }
        };
        timer.schedule(cleanup, lastDelay + 1);
    }
}

