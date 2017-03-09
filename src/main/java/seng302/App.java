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
        Config.initializeConfig();
        String name = "America's Cup Race";

        ArrayList<Boat> boatsInRace = RaceVisionFileReader.importStarters();
        Course course = RaceVisionFileReader.importCourse();
        Race race = new Race(name, course, boatsInRace);
        Display.displayRace(race);
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
