package seng302;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created on 6/03/17.
 * Class to manage the output display.
 * For now this will be simple text-based output to terminal
 */

public class Display {

    /**
     * This is one of the main functions for displaying the race.
     * This invokes the function to print the starters list as well as invokes
     * the event queue to pull each event off to show in order of completion.
     * @param race - a defined and setup race object which contains a generated Event queue
     */
    public static void displayRace(Race race){
        System.out.printf( "%s\n\n", race.getName());
        printStartersList(race.getCompetitors());
        System.out.println();
        printEventQueue(race.getEvents(), race.getTotalEventTime());
    }

    /**
     * This function takes each boat from the ArrayList of type Boat and prints the boat name and speed.
     * @param starters - all boats that started the race
     */
    public static void printStartersList(ArrayList<Boat> starters) {
        System.out.println("Boats in this race:");
        for (Boat boat : starters) {
            System.out.printf("%s - %.2f knots\n",boat.getName(), boat.getSpeed());

        }
    }

    /**
     * This function also takes each boat from the ArrayList of type Boat and prints the finishing place and
     * the boat name in order from first to last.
     * @param finishers - an ArrayList of Boat objects with defined finishing parameters
     */
    public static void printFinishersList(ArrayList<Boat> finishers) {
        finishers.sort(BoatUtils.orderByPlacing);

        System.out.println("Finishing order:");
        for (Boat boat : finishers) {
            System.out.printf("%d. %s\n", boat.getFinishingPlace(), boat.getName());
        }
    }

    /**
     * @deprecated SUPERSEDED BY printEventQueue FUNCTION
     * This function prints each mark from the ArrayList of type Mark
     */
    public static void printMarksList(ArrayList<Boat> markPassers, ArrayList<Mark> marks, int markNumber) {
    	markPassers.sort(BoatUtils.orderByPlacing);
    	
    	System.out.printf("Mark: %s\n", marks.get(markNumber).getName());
    	for (Boat boat : markPassers) {
    		System.out.printf("%d. %s\n", boat.getFinishingPlace(), boat.getName());
    	}	
    }

    /**
     * This function takes the events from the PriorityQueue of type event(event queue) and the total time
     * which the race took and creates a timer and a scheduler which prints the events as they happen.
     * Time delay is scaled to the time of the TIME_SCALE from the config file.
     * A TimerTask is initially created and used with the given delay throughout the race.
     * Once the race is finished, the TimerDelay thread is closed and purged.
     * @param events - a PriorityQueue of event objects, ordered such that the head of the queue occurs first in time
     * @param totalEventTime - the total time in seconds that the event takes, used to scale the time
     */
    public static void printEventQueue(PriorityQueue<Event> events, int totalEventTime) {
        long lastDelay = 0;
        final Timer timer = new Timer();
        while(events.size() != 0) {
            Event currentEvent = events.poll();
            long delay = (long)(((double)currentEvent.getTime() / totalEventTime) * Config.TIME_SCALE);
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

