package seng302.models;

import org.junit.BeforeClass;
import org.junit.Test;
import seng302.models.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by atc60 on 16/03/17.
 */
public class CourseTest {

    private final double DELTA = 1e-6;
    private static Course defaultCourse;

    @BeforeClass
    public static void beforeClass(){
        //Initialise a default course for testing.

        defaultCourse = new Course();
        RaceLine start = new RaceLine("Start", 1, 32.296577, -64.854304, 32.293771, -64.855242);
        CompoundMark mark = new CompoundMark("Mark", 2, 32.293039, -64.843983);
        Gate gate = new Gate("Gate", 3, 32.284680, -64.850045, 32.280164, -64.847591);
        RaceLine finish = new RaceLine("Finish", 4,32.317379, -64.839291, 32.317257, -64.836260);

        defaultCourse.addNewMark(start);
        defaultCourse.addNewMark(mark);
        defaultCourse.addNewMark(gate);
        defaultCourse.addNewMark(finish);

        defaultCourse.addMarkInOrder(1);
        defaultCourse.addMarkInOrder(2);
        defaultCourse.addMarkInOrder(3);
        defaultCourse.addMarkInOrder(4);

        defaultCourse.addToBoundary(new Coordinate(32.318879, -64.840291));
        defaultCourse.addToBoundary(new Coordinate(32.328879, -64.840291));
        defaultCourse.addToBoundary(new Coordinate(32.328879, -64.865304));
        defaultCourse.addToBoundary(new Coordinate(32.318879, -64.865304));

    }

    @Test
    public void defaultWindDirectionTest(){
        Course course = new Course();
        assertEquals(0.0, course.getWindDirection(), 0);
    }

    @Test
    public void distanceBetweenMarksTest(){
        assertEquals(0.562050737, defaultCourse.distanceBetweenMarks(0, 1), DELTA); //start and mark;
        assertEquals(0.588289145, defaultCourse.distanceBetweenMarks(1, 2), DELTA); //mark and gate;
        assertEquals(2.054763984, defaultCourse.distanceBetweenMarks(2, 3), DELTA); //gate and finish;
        assertEquals(1.583759843, defaultCourse.distanceBetweenMarks(0, 3), DELTA); //start and finish;
        assertEquals(1.490365837, defaultCourse.distanceBetweenMarks(1, 3), DELTA); //mark and finish;
    }

    @Test
    public void headingsBetweenMarksTest(){
        assertEquals(103.1714893, defaultCourse.headingsBetweenMarks(0, 1), DELTA); //start and mark;
        assertEquals(211.5126065, defaultCourse.headingsBetweenMarks(1, 2), DELTA); //mark and gate;
        assertEquals(17.62453811, defaultCourse.headingsBetweenMarks(2, 3), DELTA); //gate and finish;
        assertEquals(32.96972826, defaultCourse.headingsBetweenMarks(0, 3), DELTA); //start and finish;
        assertEquals(12.19238350, defaultCourse.headingsBetweenMarks(1, 3), DELTA); //mark and finish;
    }

    @Test
    public void initCourseLatLonTest(){
        defaultCourse.initCourseLatLon();
        assertEquals(32.2806800, defaultCourse.getMinLat(), DELTA);
        assertEquals(-64.869304, defaultCourse.getMinLon(), DELTA);
        assertEquals(32.332879, defaultCourse.getMaxLat(), DELTA);
        assertEquals(-64.833775, defaultCourse.getMaxLon(), DELTA);
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
