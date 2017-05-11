package seng302.utilities;

import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static seng302.utilities.MathUtils.VMG;
import static seng302.utilities.MathUtils.lagrangeInterpolation;
import static seng302.utilities.MathUtils.pointBetweenTwoAngle;

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

    }

        @Test
    public void VMGTest(){
        assertEquals(6.427876097, VMG(10,50), DELTA);
        assertEquals(10, VMG(10,0), DELTA);
        assertEquals(9.998476952, VMG(10,1), DELTA);
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
        assertEquals(8.564421885974385, lagrangeInterpolation(A,B,C,x), DELTA);
        assertEquals(3, lagrangeInterpolation(D,E,F,4.0), DELTA);
    }
}
