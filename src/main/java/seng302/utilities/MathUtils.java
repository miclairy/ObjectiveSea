package seng302.utilities;

import javafx.util.Pair;

/**
 * Created by gla42 on 11/05/17.
 */
public class MathUtils {

    /**
     * A function to determine whether a boat's heading is within an angle of a direction
     * @param TWD True wind direction
     * @param deltaAngle the change in angle from the TWD
     * @param bearing the boats heading
     * @return true if he boat is within angle
     */
    public static boolean pointBetweenTwoAngle(double TWD, double deltaAngle, double bearing){
        double diff;
        double middle;
        if(TWD > 180){
            diff = 0;
            bearing -= 180;
            middle = TWD - 180;
            System.out.println(bearing);
        } else {
            middle = 90;
            diff = Math.abs(90 - TWD);}
        bearing += diff;
        bearing = (bearing + 360) % 360;
        return (middle - deltaAngle) <= bearing && bearing <= (middle + deltaAngle);
    }

    /**
     * A function for interpolating three values, only does three as the math is easier and this provides an accurate enough
     * interpolation for its current uses
     * @param A first pair
     * @param B second pair
     * @param C third pair
     * @param x the value to interpolate on
     * @return the interpolated value using x as input
     */
    public static double lagrangeInterpolation(Pair<Double, Double> A, Pair<Double, Double> B, Pair<Double, Double> C, double x) {
        return (((x - B.getKey()) * (x - C.getKey())) / ((A.getKey() - B.getKey()) * (A.getKey() - C.getKey()))) * A.getValue() + (((x - A.getKey()) * (x - C.getKey())) / ((B.getKey() - A.getKey()) * (B.getKey() - C.getKey()))) * B.getValue() + (((x - A.getKey()) * (x - B.getKey())) / ((C.getKey() - A.getKey()) * (C.getKey() - B.getKey()))) * C.getValue();}

    /**
     * Calculates the VMG of the boat
     * @param boatSpeed the speed of the boat
     * @param TWA the true wind angle that the boat is heading at
     * @return the velocity made good of the boat
     */
    public static double VMG(double boatSpeed, double TWA){
        return boatSpeed * Math.cos(Math.toRadians(TWA));
    }


}
