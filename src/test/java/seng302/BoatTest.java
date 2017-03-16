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
    public void setLocationTest() {
        Course course = new Course();
        Mark mark1 = new Mark("Mark1", 50, 30);
        Mark mark2 = new Mark("Mark2", 60, 60);
        course.addNewMark(mark1);
        course.addNewMark(mark2);
        course.addMarkInOrder("Mark1");
        course.addMarkInOrder("Mark2");

        Boat boat = new Boat("TestBoat", 10);
        boat.setLocation(58.95, course);
        assertEquals(55, (int) Math.round(boat.getCurrentLat()));
        assertEquals(45, (int) Math.round(boat.getCurrentLon()));
    }
}
