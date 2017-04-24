package seng302.models;


import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class RaceTest {

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
    public void checkFinishPlacings(){
        Course course = new Course();

        Mark startLine1 = new Mark(0, "Start Line 1", new Coordinate(51.55, 30.11));
        Mark startLine2 = new Mark(1, "Start Line 2", new Coordinate(51.60, 30.16));
        RaceLine start = new RaceLine(1, "Start Line", startLine1, startLine2);

        start.setMarkAsStart();
        course.setStartLine(start);

        Mark finishLine1 = new Mark(2, "Finish Line 1", new Coordinate(51.56, 30.12));
        Mark finishLine2 = new Mark(3, "Finish Line 2", new Coordinate(51.61, 30.18));
        RaceLine finish = new RaceLine(2, "Finish", finishLine1, finishLine2);
        finish.setMarkAsFinish();

        course.addNewCompoundMark(start);
        course.addNewCompoundMark(finish);

        course.addMarkInOrder(1);
        course.addMarkInOrder(2);
        ArrayList<Boat> boats = new ArrayList<>();

        Boat testBoat1 = new Boat(0, "Boat 1","1", 10);
        Boat testBoat2 = new Boat(1, "Boat 2", "2",15);

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

        ArrayList<Boat> boats = new ArrayList<>();

        Boat testBoat1 = new Boat(0, "Boat 1","1", 10);
        boats.add(testBoat1);

        Race race = new Race("Test Race", course, boats);
        race.setTotalRaceTime();
        Assert.assertEquals(30542, Math.round(race.getTotalRaceTime()), 1);
    }
}
