package seng302;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class BoatTest
{
    @Test
    public void updateLocationTest() {
        Course course = new Course();
        Mark start = new Mark("Start", 50, 30);
        Mark mark = new Mark("Mark", 60, 60);
        course.addNewMark(start);
        course.addNewMark(mark);
        course.addMarkInOrder("Start");
        course.addMarkInOrder("Mark");

        Boat boat = new Boat("TestBoat", 10);
        boat.setPosition(start.getLat(), start.getLon());

        boat.updateLocation(58.95, course);
        assertEquals(55, (int) Math.round(boat.getCurrentLat()));
        assertEquals(45, (int) Math.round(boat.getCurrentLon()));
    }
}
