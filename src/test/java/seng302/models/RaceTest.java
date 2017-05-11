package seng302.models;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RaceTest {
    private final double DELTA = 1e-9;

    private Race defaultRace;

    @Before
    public void before(){
        Course course = new Course();

        Mark startLine1 = new Mark(0, "Start Line 1", new Coordinate(1, 1));
        Mark startLine2 = new Mark(1, "Start Line 2", new Coordinate(1, 1));
        RaceLine startingLine = new RaceLine(1, "Start Line", startLine1, startLine2);
        startingLine.setMarkAsStart();
        course.addNewCompoundMark(startingLine);
        course.setStartLine(startingLine);

        Mark finishLine1 = new Mark(2, "Finish Line 1", new Coordinate(2, 2));
        Mark finishLine2 = new Mark(3, "Finish Line 2", new Coordinate(2, 2));
        RaceLine finishLine = new RaceLine(2, "Finish", finishLine1, finishLine2);
        finishLine.setMarkAsFinish();
        course.addNewCompoundMark(finishLine);

        course.addMarkInOrder(1);
        course.addMarkInOrder(2);

        List<Boat> boats = new ArrayList<>();

        Boat testBoat1 = new Boat(1, "Boat 1","1", 10);
        Boat testBoat2 = new Boat(2, "Boat 2", "2", 20);
        boats.add(testBoat1);
        boats.add(testBoat2);

        defaultRace = new Race("Test Race", course, boats);
    }
    @Test
    public void setStartingPositionsTest(){
        Course course = new Course();

        Mark startLine1 = new Mark(0, "Start Line 1", new Coordinate(20, 10));
        Mark startLine2 = new Mark(1, "Start Line 2", new Coordinate(40, 30));
        RaceLine startLine = new RaceLine(1, "Start Line", startLine1, startLine2);

        startLine.setMarkAsStart();
        course.setStartLine(startLine);

        Mark finishLine1 = new Mark(2, "Finish Line 1", new Coordinate(51.56, -30.12));
        Mark finishLine2 = new Mark(3, "Finish Line 2", new Coordinate(51.61, -30.18));
        RaceLine finish = new RaceLine(2, "Finish", finishLine1, finishLine2);

        finish.setMarkAsFinish();

        course.addNewCompoundMark(startLine);
        course.addNewCompoundMark(finish);

        course.addMarkInOrder(1);
        course.addMarkInOrder(2);

        ArrayList<Boat> boats = new ArrayList<>();

        Boat testBoat1 = new Boat(0, "Boat 1", "1", 10);
        Boat testBoat2 = new Boat(1, "Boat 2", "2",15);

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
    public void totalRaceTimeTest(){
        defaultRace.setTotalRaceTime();
        Assert.assertEquals(30542, Math.round(defaultRace.getTotalRaceTime()), 1);
    }

    @Test
    public void updateBoatTest(){
        defaultRace.updateBoat(1, 2.5, 3.2, 178.1, 28.1, 97);

        Boat boat = defaultRace.getBoatById(1);

        Assert.assertEquals(2.5, boat.getCurrentLat(), DELTA);
        Assert.assertEquals(3.2, boat.getCurrentLon(), DELTA);
        Assert.assertEquals(178.1, boat.getHeading(), DELTA);
        Assert.assertEquals(28.1, boat.getSpeed(), DELTA);
        Assert.assertEquals(97, boat.getTWAofBoat(), DELTA);
    }

    @Test
    public void updateMarkRoundedTest(){
        Boat boat1 = defaultRace.getBoatById(1);
        Boat boat2 = defaultRace.getBoatById(2);

        assertEquals(-1, boat1.getLastRoundedMarkIndex());
        assertEquals(-1, boat2.getLastRoundedMarkIndex());

        defaultRace.updateMarkRounded(2, 1, 1);
        assertEquals(-1, boat1.getLastRoundedMarkIndex());
        assertEquals(0, boat2.getLastRoundedMarkIndex());
        assertEquals(boat2, defaultRace.getRaceOrder().get(0));
        assertEquals(boat1, defaultRace.getRaceOrder().get(1));

        defaultRace.updateMarkRounded(1, 1, 2);
        assertEquals(0, boat1.getLastRoundedMarkIndex());
        assertEquals(0, boat2.getLastRoundedMarkIndex());
        assertEquals(boat2, defaultRace.getRaceOrder().get(0));
        assertEquals(boat1, defaultRace.getRaceOrder().get(1));

        defaultRace.updateMarkRounded(1, 999, 2); //Non-existent mark
        assertEquals(0, boat1.getLastRoundedMarkIndex());
        assertEquals(0, boat2.getLastRoundedMarkIndex());

        defaultRace.updateMarkRounded(1, 2, 2);
        assertEquals(1, boat1.getLastRoundedMarkIndex());
        assertEquals(boat1, defaultRace.getRaceOrder().get(0));
        assertEquals(boat2, defaultRace.getRaceOrder().get(1));
    }

    @Test
    public void updateDuplicateMarkRoundedTest2(){
        Boat boat1 = defaultRace.getBoatById(1);

        defaultRace.getCourse().addMarkInOrder(1);

        assertEquals(-1, boat1.getLastRoundedMarkIndex());
        defaultRace.updateMarkRounded(1, 1, 1);
        assertEquals(0, boat1.getLastRoundedMarkIndex());
        defaultRace.updateMarkRounded(1, 2, 5);
        assertEquals(1, boat1.getLastRoundedMarkIndex());
        defaultRace.updateMarkRounded(1, 1, 10);
        assertEquals(2, boat1.getLastRoundedMarkIndex());

        defaultRace.updateMarkRounded(1, 2, 10); //No more marks of this id exist in race order
        assertEquals(2, boat1.getLastRoundedMarkIndex());
    }
}
