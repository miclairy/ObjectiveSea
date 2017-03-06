package seng302;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "RaceVision\n" );

        ArrayList<Boat> boatsInRace = RaceVisionFileReader.importStarters();
        Display.printStartersList(boatsInRace);

        System.out.println();

        randomizeFinishingOrder(boatsInRace);
        Display.printFinishersList(boatsInRace);

    }

    private static void randomizeFinishingOrder(ArrayList<Boat> boats){
        int numBoats = boats.size();
        ArrayList<Integer> places = new ArrayList<>();
        for (int i = 1; i <= numBoats; i++){
            places.add(i);
        }
        Collections.shuffle(places, new Random(System.nanoTime()));
        for (int j = 0; j < numBoats; j++) {
            boats.get(j).setFinishingPlace(places.get(j));
        }
    }
}
