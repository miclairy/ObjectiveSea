package seng302.utilities;

import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Test;
import seng302.models.CompoundMark;
import seng302.models.Coordinate;
import seng302.models.Mark;
import seng302.models.WindAngleAndSpeed;
import seng302.models.*;

import static junit.framework.TestCase.assertEquals;
import static seng302.utilities.MathUtils.*;

/**
 * Created by gla42 on 11/05/17.
 */
public class MathUtilsTest {

    private double DELTA = 1e-6;
    @Test
    public void pointBetweenTwoAngleTest(){
        //if TWD is 0, then if a boat bearing is between 270 and 90 it should be true
        Assert.assertTrue(pointBetweenTwoAngle(0,90,271));
        Assert.assertTrue(pointBetweenTwoAngle(0,90,0));
        Assert.assertTrue(pointBetweenTwoAngle(0,90,89));
        Assert.assertFalse(pointBetweenTwoAngle(0,90,269));
        Assert.assertFalse(pointBetweenTwoAngle(0,90,180));
        Assert.assertFalse(pointBetweenTwoAngle(0,90,91));
        //if TWD is 20, then if a boat bearing is between 290 and 110 it should be true
        Assert.assertTrue(pointBetweenTwoAngle(20,90,291));
        Assert.assertTrue(pointBetweenTwoAngle(20,90,10));
        Assert.assertTrue(pointBetweenTwoAngle(20,90,109));
        Assert.assertFalse(pointBetweenTwoAngle(20,90,289));
        Assert.assertFalse(pointBetweenTwoAngle(20,90,180));
        Assert.assertFalse(pointBetweenTwoAngle(20,90,111));
        //if TWD is 0 and the boat bearing is 341 and TWA is 20
        Assert.assertTrue(pointBetweenTwoAngle(0,20,341));
        Assert.assertTrue(pointBetweenTwoAngle(0,20,0));
        Assert.assertTrue(pointBetweenTwoAngle(0,20,19));
        Assert.assertFalse(pointBetweenTwoAngle(0,20,21));
        Assert.assertFalse(pointBetweenTwoAngle(0,20,339));
        Assert.assertFalse(pointBetweenTwoAngle(0,20,180));
        //if TWD is 280 and boat bearing is 271 and TWA is 30
        Assert.assertTrue(pointBetweenTwoAngle(280,30,271));
        Assert.assertTrue(pointBetweenTwoAngle(280,30,300));
        Assert.assertFalse(pointBetweenTwoAngle(280, 30, 0));
        Assert.assertFalse(pointBetweenTwoAngle(280, 30, 249));

        Assert.assertFalse(pointBetweenTwoAngle((131+270)%360,90,165));
        Assert.assertFalse(pointBetweenTwoAngle(131+90,90,27));

        Assert.assertTrue(pointBetweenTwoAngle(131+90,90,165));
        Assert.assertTrue(pointBetweenTwoAngle((131+270)%360,90,27));

    }

        @Test
    public void VMGTest(){
        assertEquals(6.427876097, VMG(10,50), DELTA);
        assertEquals(10, VMG(10,0), DELTA);
        assertEquals(9.998476952, VMG(10,1), DELTA);
    }

    @Test
    public void LagrangeInterpolationTest(){
        WindAngleAndSpeed A = new WindAngleAndSpeed(44.7,8.843719);
        WindAngleAndSpeed B = new WindAngleAndSpeed(80.9,2.982861);
        WindAngleAndSpeed C = new WindAngleAndSpeed(101.9,-4.66228);
        WindAngleAndSpeed D = new WindAngleAndSpeed(1.0,2.0);
        WindAngleAndSpeed E = new WindAngleAndSpeed(2.0,3.0);
        WindAngleAndSpeed F = new WindAngleAndSpeed(5.0,2.0);
        double x = 50.0;
        assertEquals(8.564421885974385, lagrangeInterpolation(A,B,C,x), DELTA);
        assertEquals(3, lagrangeInterpolation(D,E,F,4.0), DELTA);
    }

    @Test
    public void calculateMidPointTest(){
        Coordinate mark1Coord = new Coordinate(32, 64);
        Coordinate mark2Coord = new Coordinate(35, 68);
        Mark mark1 = new Mark(0, "Test",mark1Coord);
        Mark mark2 = new Mark(0, "Test",mark2Coord);
        CompoundMark cm1 = new CompoundMark(0, "Test",mark1,mark2);
        Coordinate testAnswer = new Coordinate(33.5, 66);
        assertEquals(testAnswer, calculateMidPoint(cm1));
    }

    @Test
    public void boatBeforeStartlineTest(){
        Assert.assertTrue(boatBeforeStartline(32.2937,-64.855242,32.296577,-64.854304,-32.293771,-64.855242,32.293039,-64.845045));
        Assert.assertTrue(boatBeforeStartline(32.293039,-64.845045,32.296577,-64.854304,-32.293771,-64.855242,32.2937,-64.855242));
        Assert.assertFalse(boatBeforeStartline(32.296577,-64.854304,32.296577,-64.854304,-32.293771,-64.855242,32.2937,-64.855242));
    }

    @Test
    public void boatHeadingToLineTest(){
        Assert.assertTrue(boatHeadingToLine(90,0,270));
        Assert.assertFalse(boatHeadingToLine(90,0,85));
        Assert.assertTrue(boatHeadingToLine(165,131,27));
    }
}