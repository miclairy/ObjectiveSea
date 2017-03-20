package seng302;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        boat = new Boat("TestBoat", 10);
    }

    @Test
    public void updateLocationTest() {
        Course course = new Course();
        Gate start = new Gate("Start", 50, 30, 50.02, 30.02);
        CompoundMark mark = new CompoundMark("Mark", 60, 60);
        course.addNewMark(start);
        course.addNewMark(mark);
        course.addMarkInOrder("Start");
        course.addMarkInOrder("Mark");

        boat.setPosition(start.getLat(), start.getLon());
        ArrayList<Boat> competitors = new ArrayList<>();
        competitors.add(boat);
        boat.updateLocation(58.95, course, new Race("", course, competitors));
        assertEquals(55, (int) Math.round(boat.getCurrentLat()));
        assertEquals(45, (int) Math.round(boat.getCurrentLon()));
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
        Gate start = new Gate("Start", 50, 30, 50.02, 30.02);
        CompoundMark mark = new CompoundMark("Mark", 50, 30.5);
        Gate finish = new Gate("Finish", 50.5, 30.5, 50.52, 30.52);
        course.addNewMark(start);
        course.addNewMark(mark);
        course.addNewMark(finish);
        course.addMarkInOrder("Start");
        course.addMarkInOrder("Mark");
        course.addMarkInOrder("Finish");

        boat.setPosition(50, 30);
        ArrayList<Boat> competitors = new ArrayList<>();
        competitors.add(boat);
        boat.updateLocation(2, course, new Race("", course, competitors));

        assertEquals(50.0242104224617, boat.getCurrentLat(), DELTA);
        assertEquals(30.500474714165914, boat.getCurrentLon(), DELTA);
        assertEquals( 1, boat.getLastPassedMark());
    }


    @Test
    public void finishedRaceTest() {
        Course course = new Course();
        Gate start = new Gate("Start", 51.55, 30.11, 51.60, 30.16);
        Gate finish = new Gate("Finish", 51.56, 30.12, 51.61, 30.18);
        course.addNewMark(start);
        course.addNewMark(finish);
        course.addMarkInOrder("Start");
        course.addMarkInOrder("Finish");

        boat.setPosition(51.55, 30.11);
        ArrayList<Boat> competitors = new ArrayList<>();
        competitors.add(boat);
        boat.updateLocation(20, course, new Race("", course, competitors));

        assertTrue(boat.isFinished());
        assertEquals(51.585, boat.getCurrentLat(), DELTA);
        assertEquals(30.15, boat.getCurrentLon(), DELTA);
    }
}
