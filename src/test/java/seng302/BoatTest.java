package seng302;

import org.junit.Before;
import org.junit.Test;
import seng302.models.Boat;
import seng302.models.CompoundMark;
import seng302.models.Course;
import seng302.models.RaceLine;

import java.util.ArrayList;

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
        boat = new Boat("TestBoat", "testNickname", 10);
        boat.setSpeed(10);
    }

    @Test
    public void updateLocationTest() {
        Course course = new Course();
        RaceLine start = new RaceLine("Start", 50, 30, 51, 30);
        CompoundMark mark = new CompoundMark("Mark", 60, 60);

        course.addNewMark(start);
        course.addNewMark(mark);
        course.addMarkInOrder("Start");
        course.addMarkInOrder("Mark");

        boat.setPosition(start.getLat(), start.getLon());
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
        RaceLine start = new RaceLine("Start", 50, 30, 50.02, 30.02);
        CompoundMark mark = new CompoundMark("Mark", 50, 30.5);
        RaceLine finish = new RaceLine("Finish", 50.5, 30.5, 50.52, 30.52);
        course.addNewMark(start);
        course.addNewMark(mark);
        course.addNewMark(finish);
        course.addMarkInOrder("Start");
        course.addMarkInOrder("Mark");
        course.addMarkInOrder("Finish");

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
        RaceLine start = new RaceLine("Start", 51.55, 30.11, 51.60, 30.16);
        RaceLine finish = new RaceLine("Finish", 51.56, 30.12, 51.61, 30.18);
        course.addNewMark(start);
        course.addNewMark(finish);
        course.addMarkInOrder("Start");
        course.addMarkInOrder("Finish");

        boat.setPosition(51.55, 30.11);
        ArrayList<Boat> competitors = new ArrayList<>();
        competitors.add(boat);
        boat.updateLocation(20, course);

        assertTrue(boat.isFinished());
        assertEquals(51.585, boat.getCurrentLat(), DELTA);
        assertEquals(30.15, boat.getCurrentLon(), DELTA);
    }

    @Test
    public void VMGTest(){;
        assertEquals(6.427876097, boat.VMG(10,50), DELTA);
        assertEquals(10, boat.VMG(10,0), DELTA);
        assertEquals(9.998476952, boat.VMG(10,1), DELTA);
    }
}
