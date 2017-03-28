package seng302;


import org.junit.Assert;
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

        testBoat1.updateLocation(2, course);
        testBoat2.updateLocation(2, course);
        ArrayList<Boat> places = new ArrayList<>();
        places.add(testBoat1);
        places.add(testBoat2);
        assertEquals(places, race.getRaceOrder());
    }

    @Test
    public void totalRaceTimeTest(){
        Course course = new Course();
        CompoundMark mark1 = new Gate("TestMark1", 1, 1, 1, 1);
        CompoundMark mark2 = new CompoundMark("TestMark2", 2, 2);
        course.addNewMark(mark1);
        course.addNewMark(mark2);
        course.addMarkInOrder("TestMark1");
        course.addMarkInOrder("TestMark2");
        ArrayList<Boat> boats = new ArrayList<>();

        Boat testBoat1 = new Boat("Boat 1","1", 10);

        boats.add(testBoat1);

        Race race = new Race("Test Race", course, boats);
        race.setTotalRaceTime();
        Assert.assertEquals(30542, Math.round(race.getTotalRaceTime()), 1);
    }
}
