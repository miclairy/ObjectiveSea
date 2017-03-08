package seng302;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Created by mjt169 on 6/03/17.
 * Edited by cjd137 on 7/03/17.
 * Main App class to control program
 */


public class App 
{
    public static void main( String[] args )
    {
        Config.initializeConfig();
        String name = "America's Cup Race";
        System.out.printf( "%s\n\n", name );

        ArrayList<Boat> boatsInRace = RaceVisionFileReader.importStarters();
        Course course = new Course(RaceVisionFileReader.importMarks());
        Race race = new Race(name, course, boatsInRace);

        //this queue can be used to pull events to print to screen
        PriorityQueue<Event> events = race.getEvents();

        Display.printStartersList(boatsInRace);

        System.out.println();

        Display.printEventQueue(events);

        System.out.println();

        randomizeOrder(boatsInRace);
        Display.printFinishersList(boatsInRace);

    }

    private static void randomizeOrder(ArrayList<Boat> boats){
        int numBoats = boats.size();
        ArrayList<Integer> places = new ArrayList<>();
        for (int i = 1; i <= numBoats; i++){
            places.add(i);
        }
        Collections.shuffle(places, new Random());
        for (int j = 0; j < numBoats; j++) {
            boats.get(j).setFinishingPlace(places.get(j));
        }
    }

}
