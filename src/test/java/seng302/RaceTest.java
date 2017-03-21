package seng302;


import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class RaceTest {

    @Test
    public void setStartingPositionsTest(){
        Course course = new Course();
        Gate startLine = new Gate("StartLine", 20, 10, 40, 30);
        course.addNewMark(startLine);
        course.addMarkInOrder("StartLine");
        ArrayList<Boat> boats = new ArrayList<Boat>();

        Boat testBoat1 = new Boat("Boat 1", "1", 10);
        Boat testBoat2 = new Boat("Boat 2", "2",15);

        boats.add(testBoat1);
        boats.add(testBoat2);

        Race race = new Race("Test Race", course, boats);
        race.setStartingPositions();

        assertEquals(30, (int)Math.round(testBoat1.getCurrentLat()));
        assertEquals(20, (int)Math.round(testBoat1.getCurrentLon()));
        assertEquals(40, (int)Math.round(testBoat2.getCurrentLat()));
        assertEquals(30, (int)Math.round(testBoat2.getCurrentLon()));

    }

    @Test
    public void checkFinishPlacings(){
        Course course = new Course();
        Gate start = new Gate("Start", 51.55, 30.11, 51.60, 30.16);
        Gate finish = new Gate("Finish", 51.56, 30.12, 51.61, 30.18);
        course.addNewMark(start);
        course.addNewMark(finish);
        course.addMarkInOrder("Start");
        course.addMarkInOrder("Finish");
        ArrayList<Boat> boats = new ArrayList<>();

        Boat testBoat1 = new Boat("Boat 1","1", 10);
        Boat testBoat2 = new Boat("Boat 2", "2",15);

        boats.add(testBoat1);
        boats.add(testBoat2);

        Race race = new Race("Test Race", course, boats);

        testBoat1.updateLocation(2, course, race);
        testBoat2.updateLocation(2, course, race);
        ArrayList<Boat> places = new ArrayList<>();
        places.add(testBoat1);
        places.add(testBoat2);
        assertEquals(places, race.getPlacings());
    }
}
