package seng302;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.*;

/**
 * Created by mjt169 on 6/03/17.
 * Edited by cjd137 on 7/03/17.
 * Class to manage the output display.
 * For now this will be simple text-based output to terminal
 */

public class Display extends Thread {

    private Race race;
    private Group root;
    private final ArrayList<Color> COLORS = new ArrayList<>((Arrays.asList(Color.WHITE, Color.web("#A0D468"), Color.web("#FC6E51"),
            Color.web("#FFCE54"), Color.web("#48CFAD"), Color.web("#4FC1E9"), Color.web("#656D78"))));

    public Display(Group root, Race race) {
        this.root = root;
        this.race = race;
        race.setEvents();
        drawBoats();
    }

//    public void displayRace(){
//
//        System.out.printf( "%s\n\n", race.getName());
//        // printStartersList(race.getCompetitors());
//        System.out.println();
//        // printEventQueue(race.getEvents(), race.getTotalEventTime());
//    }

//    public static void printStartersList(ArrayList<Boat> starters) {
//        System.out.println("Boats in this race:");
//        for (Boat boat : starters) {
//            System.out.printf("%s - %.2f knots\n",boat.getName(), boat.getSpeed());
//
//        }
//    }

/*
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
*/

//    public static void printEventQueue(PriorityQueue<Event> events, int totalEventTime) {
//        long lastDelay = 0;
//        final Timer timer = new Timer();
//        while(events.size() != 0) {
//            Event currentEvent = events.poll();
//            long delay = (long)(((double)currentEvent.getTime() / totalEventTime) * Config.TIME_SCALE);
//            final TimerTask task = new TimerTask() {
//                @Override
//                public void run() {
//                    System.out.printf("%s\n", currentEvent.printEvent());
//                    if (currentEvent instanceof RaceEndEvent) {
//                        System.out.println();
//                        printFinishersList(((RaceEndEvent) currentEvent).getFinishers());
//                    }
//                }
//            };
//            lastDelay = delay;
//            timer.schedule(task, delay);
//        }
//        final TimerTask cleanup = new TimerTask() {
//            @Override
//            public void run() {
//                timer.cancel();
//                timer.purge();
//            }
//        };
//        timer.schedule(cleanup, lastDelay + 1);
//    }

    @Override
    public void run(){
        double timeIncrement = 5;
        boolean finished = false;
        while (!finished){
            finished = true;
            for (Boat boat : race.getCompetitors()){
                boat.updateLocation(timeIncrement, race.getCourse());
                if (!boat.isFinished()){
                    finished = false;
                }
            }
            redrawBoats();

            try {
                this.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public void drawBoats(){

        int i = 1;

        for (Boat boat : race.getCompetitors()) {
            Circle boatImage = new Circle(Math.abs(boat.getCurrentLat() * i), Math.abs(boat.getCurrentLon()) , 5.0f);
            boatImage.setFill(COLORS.get(i));
            boatImage.setStroke(Color.WHITE);
            root.getChildren().add(boatImage);
            boat.setIcon(boatImage);
            i++;
        }
    }

    public void redrawBoats(){
        for (Boat boat : race.getCompetitors()) {
            boat.getIcon().relocate(Math.abs(boat.getCurrentLat()) , Math.abs(boat.getCurrentLon()) );
        }
        }
}

