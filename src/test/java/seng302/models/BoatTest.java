package seng302.models;

import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seng302.utilities.readPolars;

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
        try {
            readPolars.polars();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Polars> polars = readPolars.getPolars();
        Course course = new Course();
        course.setTrueWindSpeed(20);
        Pair<Double,Double> test = boat.tacking(20, polars);
        //Check VMG
        assertEquals(18.113029925346527, test.getKey(), DELTA);
        //Check TWA
        assertEquals(41.0, test.getValue(), DELTA);
        //Check BSp
        assertEquals(24, (test.getKey()/Math.cos(Math.toRadians(test.getValue()))), DELTA);
        //Check gybing also works
        Pair<Double,Double> gybeTest = boat.gybing(20, polars);
        //Check VMG
        assertEquals(-32.07623487078124, gybeTest.getKey(), DELTA);
        //Check TWA
        assertEquals(153.0, gybeTest.getValue(), DELTA);
        //Check BSp
        assertEquals(36.0, (gybeTest.getKey()/Math.cos(Math.toRadians(gybeTest.getValue()))), DELTA);
    }
}
