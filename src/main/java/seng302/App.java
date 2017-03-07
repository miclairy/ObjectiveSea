package seng302;

import java.util.ArrayList;
import java.util.Collections;
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
        System.out.println( "RaceVision\n" );

        ArrayList<Boat> boatsInRace = RaceVisionFileReader.importStarters();
        ArrayList<Mark> marksOnCourse = RaceVisionFileReader.importMarks();
        Display.printStartersList(boatsInRace);

        for(int i = 0; i<marksOnCourse.size(); i++) {
        	System.out.println();
        	
        	if(marksOnCourse.size() != i+1 && i != 0) {
            	randomizeOrder(boatsInRace);
        		Display.printMarksList(boatsInRace, marksOnCourse, i);
        	}
        }

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
