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
        PolarReader.readPolars();
        ArrayList<Polar> polars = PolarReader.getPolarsForAC35Yachts();
        Course course = new Course();
        course.setTrueWindSpeed(20);
        PolarTable table = new PolarTable(polars);
        WindAngleAndSpeed test = table.calculateOptimumTack(20, polars);
        //Check VMG
        assertEquals(18.113029925346527, test.getWindAngle(), DELTA);
        //Check TWA
        assertEquals(41.0, test.getSpeed(), DELTA);
        //Check BSp
        assertEquals(24, (test.getWindAngle()/Math.cos(Math.toRadians(test.getSpeed()))), DELTA);
        //Check calculateOptimumGybe also works
        WindAngleAndSpeed gybeTest = table.calculateOptimumGybe(20, polars);
        //Check VMG
        assertEquals(-32.07623487078124, gybeTest.getWindAngle(), DELTA);
        //Check TWA
        assertEquals(153.0, gybeTest.getSpeed(), DELTA);
        //Check BSp
        assertEquals(36.0, (gybeTest.getWindAngle()/Math.cos(Math.toRadians(gybeTest.getSpeed()))), DELTA);
    }
}
