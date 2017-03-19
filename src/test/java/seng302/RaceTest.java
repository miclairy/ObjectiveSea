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

        Boat testBoat1 = new Boat("Boat 1", 10);
        Boat testBoat2 = new Boat("Boat 2", 15);

        boats.add(testBoat1);
        boats.add(testBoat2);

        Race race = new Race("Test Race", course, boats);
        race.setStartingPositions();

        assertEquals(30, (int)Math.round(testBoat1.getCurrentLat()));
        assertEquals(20, (int)Math.round(testBoat1.getCurrentLon()));
        assertEquals(40, (int)Math.round(testBoat2.getCurrentLat()));
        assertEquals(30, (int)Math.round(testBoat2.getCurrentLon()));

    }
}
