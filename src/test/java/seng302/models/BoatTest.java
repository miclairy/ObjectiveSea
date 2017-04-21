package seng302.models;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class BoatTest
{
    private Boat boat;
    private double DELTA = 1e-6;

    @Before
    public void before(){
        boat = new Boat(0, "TestBoat", "testNickname", 10);
        boat.setSpeed(10);
    }

    @Test
    public void updateLocationTest() {
        Course course = new Course();

        Mark startLine1 = new Mark(0, "Start Line 1", new Coordinate(50, 30));
        Mark startLine2 = new Mark(1, "Start Line 2", new Coordinate(51, 30));
        RaceLine start = new RaceLine(1, "Start Line",startLine1, startLine2);

        Mark mark1 = new Mark(2, "Mark 1", new Coordinate(60, 60));
        CompoundMark mark = new CompoundMark(2, "Mark", mark1);

        course.addNewCompoundMark(start);
        course.addNewCompoundMark(mark);
        course.addMarkInOrder(1);
        course.addMarkInOrder(2);

        boat.setPosition(start.getPosition().getLat(), start.getPosition().getLon());
        ArrayList<Boat> competitors = new ArrayList<>();
        competitors.add(boat);
        boat.updateLocation(58.98, course);
        assertEquals(55.33319865, boat.getCurrentLat(), DELTA);
        assertEquals(45.26273259, boat.getCurrentLon(), DELTA);
    }

    @Test
    public void setLocationTest(){
        boat.setPosition(25, 50.7);
        assertEquals(25, boat.getCurrentLat(), DELTA);
        assertEquals(50.7, boat.getCurrentLon(), DELTA);
    }

    @Test
    public void passedMarkTest() {
        Course course = new Course();

        Mark startLine1 = new Mark(0, "Start Line 1", new Coordinate(50, 30));
        Mark startLine2 = new Mark(1, "Start Line 2", new Coordinate(50.02, 30.02));
        RaceLine start = new RaceLine(1, "Start Line",startLine1, startLine2);

        Mark mark1 = new Mark(2, "Mark 1", new Coordinate(50, 30.5));
        CompoundMark mark = new CompoundMark(2, "Mark", mark1);

        Mark finishLine1 = new Mark(3, "Finish Line 1", new Coordinate(50.5, 30.5));
        Mark finishLine2 = new Mark(4, "Finish Line 2", new Coordinate(50.52, 30.52));
        RaceLine finish = new RaceLine(3, "Finish", finishLine1, finishLine2);


        course.addNewCompoundMark(start);
        course.addNewCompoundMark(mark);
        course.addNewCompoundMark(finish);
        course.addMarkInOrder(1);
        course.addMarkInOrder(2);
        course.addMarkInOrder(3);

        boat.setPosition(50, 30);
        ArrayList<Boat> competitors = new ArrayList<>();
        competitors.add(boat);
        boat.updateLocation(2, course);

        assertEquals(50.01193918885366, boat.getCurrentLat(), DELTA);
        assertEquals(30.50023410174223, boat.getCurrentLon(), DELTA);
        assertEquals( 1, boat.getLastPassedMark());
    }


    @Test
    public void finishedRaceTest() {
        Course course = new Course();

        Mark startLine1 = new Mark(0, "Start Line 1", new Coordinate(51.55, 30.11));
        Mark startLine2 = new Mark(1, "Start Line 2", new Coordinate(51.60, 30.16));
        RaceLine start = new RaceLine(1, "Start Line", startLine1, startLine2);

        Mark finishLine1 = new Mark(2, "Finish Line 1", new Coordinate(51.56, 30.12));
        Mark finishLine2 = new Mark(3, "Finish Line 2", new Coordinate(51.61, 30.18));
        RaceLine finish = new RaceLine(2, "Finish Line", finishLine1, finishLine2);

        course.addNewCompoundMark(start);
        course.addNewCompoundMark(finish);
        course.addMarkInOrder(1);
        course.addMarkInOrder(2);

        boat.setPosition(51.55, 30.11);
        List<Boat> competitors = new ArrayList<>();
        competitors.add(boat);
        boat.updateLocation(20, course);

        assertTrue(boat.isFinished());
        assertEquals(51.585, boat.getCurrentLat(), DELTA);
        assertEquals(30.15, boat.getCurrentLon(), DELTA);
    }

}
