package seng302;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by atc60 on 16/03/17.
 */
public class CourseTest {

    @Test
    public void gcDistanceTest(){
        Coordinate coord1 = new Coordinate(50, 30);
        Coordinate coord2 = new Coordinate(60, 60);
        assertEquals(1179, (int)Math.round(coord1.greaterCircleDistance(coord2)));
    }

    @Test
    public void defaultWindDirectionTest(){
        Course course = new Course();
        assertEquals(0.0, course.getWindDirection(), 0);
    }

    @Test
    public void outOfBoundsWindDirectionTest(){
        Course course = new Course();
        course.setWindDirection(-45);

        Course course2 = new Course();
        course2.setWindDirection(450);
        assertEquals(315.0, course.getWindDirection(), 0);
        assertEquals(90.0, course2.getWindDirection(), 0);
    }

}
