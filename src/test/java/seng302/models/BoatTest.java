package seng302.models;

import javafx.util.Pair;
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
        boat.setSpeed(10);
        boat.setLastRoundedMarkIndex(0);
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
    public void updateTackingLocationTest() {
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

        double distanceGained = 58.98 * boat.getVMGofBoat();
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        course.setWindDirection(30);
        double windDirection = course.getWindDirection();
        double bearing = course.headingsBetweenMarks(0,1);
        double alphaAngle;
        if(bearing <= (windDirection + 90.0)){
            alphaAngle = Math.abs(bearing - windDirection)%360;
        } else {
            alphaAngle = (360 + windDirection - bearing)%360;
        }

        boat.setPosition(start.getPosition().getLat(), start.getPosition().getLon());
        ArrayList<Boat> competitors = new ArrayList<>();
        competitors.add(boat);
        boat.tackingUpdateLocation(distanceGained, courseOrder, true, alphaAngle);
        assertEquals(60.0, boat.getCurrentLat(), DELTA);
        assertEquals(60.0, boat.getCurrentLon(), DELTA);
    }

    @Test
    public void updateGybingLocationTest() {
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

        double distanceGained = 58.98 * boat.getGybeVMGofBoat() * - 1;

        course.setWindDirection(30);
        double windDirection = course.getWindDirection();
        double bearing = course.headingsBetweenMarks(0,1);
        double alphaAngle;
        if(bearing <= (windDirection + 90.0)){
            alphaAngle = 180 - Math.abs(bearing - windDirection)%360;
        } else {
            alphaAngle = 180 - (360 + windDirection - bearing)%360;
        }

        boat.setPosition(start.getPosition().getLat(), start.getPosition().getLon());
        ArrayList<Boat> competitors = new ArrayList<>();
        competitors.add(boat);
        boat.tackingUpdateLocation(distanceGained, course.getCourseOrder(), false,alphaAngle);
        assertEquals(60.0, boat.getCurrentLat(), DELTA);
        assertEquals(60.0, boat.getCurrentLon(), DELTA);
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
        assertEquals( 1, boat.getLastRoundedMarkIndex());
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

        boolean a = boat.isFinished();

        assertTrue(boat.isFinished());
        assertEquals(51.585, boat.getCurrentLat(), DELTA);
        assertEquals(30.15, boat.getCurrentLon(), DELTA);
    }

    @Test
    public void VMGTest(){
        assertEquals(6.427876097, boat.VMG(10,50), DELTA);
        assertEquals(10, boat.VMG(10,0), DELTA);
        assertEquals(9.998476952, boat.VMG(10,1), DELTA);
    }

    @Test
    public void LagrangeInterpolationTest(){
        Pair<Double, Double> A = new Pair<>(44.7,8.843719);
        Pair<Double, Double> B = new Pair<>(80.9,2.982861);
        Pair<Double, Double> C = new Pair<>(101.9,-4.66228);
        Pair<Double, Double> D = new Pair<>(1.0,2.0);
        Pair<Double, Double> E = new Pair<>(2.0,3.0);
        Pair<Double, Double> F = new Pair<>(5.0,2.0);
        double x = 50.0;
        assertEquals(8.564421885974385, boat.lagrangeInterpolation(A,B,C,x), DELTA);
        assertEquals(3, boat.lagrangeInterpolation(D,E,F,4.0), DELTA);
    }

    @Test
    public void tackingTest(){
        try {
            readPolars.polars();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Integer> TWSList = readPolars.getTWS();
        ArrayList<ArrayList<Pair<Double, Double>>> polars = readPolars.getPolars();
        Course course = new Course();
        course.setTrueWindSpeed(20);
        Pair<Double,Double> test = boat.tacking(20,TWSList,polars, true);
        //Check VMG
        assertEquals(18.2690537492209, test.getKey(), DELTA);
        //Check TWA
        assertEquals(36.0, test.getValue(), DELTA);
        //Check BSp
        assertEquals(22.58179231863443, (test.getKey()/Math.cos(Math.toRadians(test.getValue()))), DELTA);
        //Check gybing also works
        Pair<Double,Double> gybeTest = boat.tacking(20,TWSList,polars, false);
        //Check VMG
        assertEquals(-32.07623487078124, gybeTest.getKey(), DELTA);
        //Check TWA
        assertEquals(153.0, gybeTest.getValue(), DELTA);
        //Check BSp
        assertEquals(36.0, (gybeTest.getKey()/Math.cos(Math.toRadians(gybeTest.getValue()))), DELTA);
    }


}
