package seng302.models;

import org.junit.Before;
import org.junit.Test;
import seng302.utilities.PolarReader;

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
        boat = new Boat(0, "TestBoat", "testNickname", 10);
        boat.setCurrentSpeed(10);
    }

    @Test
    public void setLocationTest(){
        boat.setPosition(25, 50.7);
        assertEquals(25, boat.getCurrentLat(), DELTA);
        assertEquals(50.7, boat.getCurrentLon(), DELTA);
    }

    @Test
    public void tackingTest(){
        ArrayList<Polar> polars = PolarReader.getPolarsForAC35Yachts();
        Course course = new Course();
        course.setTrueWindSpeed(20);
        PolarTable table = new PolarTable(polars, course);
        WindAngleAndSpeed test = table.calculateOptimumTack(20);
        //Check VMG
        assertEquals(18.113029925346527, test.getWindAngle(), DELTA);
        //Check TWA
        assertEquals(41.0, test.getSpeed(), DELTA);
        //Check BSp
        assertEquals(24, (test.getWindAngle()/Math.cos(Math.toRadians(test.getSpeed()))), DELTA);
        //Check calculateOptimumGybe also works
        WindAngleAndSpeed gybeTest = table.calculateOptimumGybe(20);
        //Check VMG
        assertEquals(-32.07623487078124, gybeTest.getWindAngle(), DELTA);
        //Check TWA
        assertEquals(153.0, gybeTest.getSpeed(), DELTA);
        //Check BSp
        assertEquals(36.0, (gybeTest.getWindAngle()/Math.cos(Math.toRadians(gybeTest.getSpeed()))), DELTA);
    }

    @Test
    public void tackAndGybeTest(){
        Course course = new Course();
        course.setTrueWindSpeed(20);
        course.setWindDirection(0);
        boat.setHeading(95);
        boat.oldTackOrGybe(0,course);
        assertEquals(265.0,boat.getHeading(),DELTA); //downwind
        boat.setHeading(200);
        boat.oldTackOrGybe(10,course);
        assertEquals(180.0,boat.getHeading(),DELTA); //downwind
        boat.setHeading(50);
        boat.oldTackOrGybe(180,course);
        assertEquals(310.0,boat.getHeading(),DELTA); //downwind
        boat.setHeading(30);
        boat.oldTackOrGybe(310,course);
        assertEquals(230.0,boat.getHeading(),DELTA); //upwind
        boat.setHeading(70);
        boat.oldTackOrGybe(350,course);
        assertEquals(270.0,boat.getHeading(),DELTA); //upwind
        boat.setHeading(30);
        boat.oldTackOrGybe(0,course);
        assertEquals(330.0,boat.getHeading(),DELTA); //upwind

    }

    @Test
    public void getVMGHeadingTest() {
        Course course = new Course();
        course.setTrueWindSpeed(25);
        PolarTable polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), course);
        double optimum;

        // Tacking
        boat.setHeading(45);
        course.setWindDirection(0);
        optimum = boat.getVMGHeading(course, polarTable);
        assertEquals(40, optimum, DELTA);

        // Tacking
        boat.setHeading(280);
        course.setWindDirection(0);
        optimum = boat.getVMGHeading(course, polarTable);
        assertEquals(320, optimum, DELTA);

        // Gybing
        boat.setHeading(105);
        course.setWindDirection(0);
        optimum = boat.getVMGHeading(course, polarTable);
        assertEquals(151, optimum, DELTA);

        // Gybing
        boat.setHeading(250);
        course.setWindDirection(0);
        optimum = boat.getVMGHeading(course, polarTable);
        assertEquals(209, optimum, DELTA);

        // No Sail zone
        boat.setHeading(10);
        course.setWindDirection(0);
        optimum = boat.getVMGHeading(course, polarTable);
        assertEquals(10, optimum, DELTA);

        // Dead zone
        boat.setHeading(90);
        course.setWindDirection(0);
        optimum = boat.getVMGHeading(course, polarTable);
        assertEquals(90, optimum, DELTA);
    }

    @Test
    public void tackOrGybeTest() {
        Course course = new Course();
        course.setTrueWindSpeed(25);
        PolarTable polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), course);
        double optimum;

        // Tacking
        boat.setHeading(45);
        course.setWindDirection(0);
        optimum = boat.getTackOrGybeHeading(course, polarTable);
        assertEquals(320, optimum, DELTA);

        // Tacking
        boat.setHeading(280);
        course.setWindDirection(0);
        optimum = boat.getTackOrGybeHeading(course, polarTable);
        assertEquals(40, optimum, DELTA);

        // Gybing
        boat.setHeading(105);
        course.setWindDirection(0);
        optimum = boat.getTackOrGybeHeading(course, polarTable);
        assertEquals(209, optimum, DELTA);

        // Gybing
        boat.setHeading(250);
        course.setWindDirection(0);
        optimum = boat.getTackOrGybeHeading(course, polarTable);
        assertEquals(151, optimum, DELTA);

        // No Sail zone
        boat.setHeading(10);
        course.setWindDirection(0);
        optimum = boat.getTackOrGybeHeading(course, polarTable);
        assertEquals(10, optimum, DELTA);

        // Dead zone
        boat.setHeading(90);
        course.setWindDirection(0);
        optimum = boat.getTackOrGybeHeading(course, polarTable);
        assertEquals(90, optimum, DELTA);
    }

    @Test
    public void updateBoatSpeedTest(){
        Course course = new Course();

        boat.setHeading(80);
        course.setWindDirection(40);
        course.setTrueWindSpeed(10);
        double newSpeed = boat.updateBoatSpeed(course);
        assertEquals(10.25, newSpeed, DELTA);

        boat.setHeading(0);
        course.setTrueWindSpeed(25);
        course.setWindDirection(90);
        newSpeed = boat.updateBoatSpeed(course);
        assertEquals(41.125,newSpeed , DELTA);
    }


}
