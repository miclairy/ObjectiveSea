package seng302.utilities;

import seng302.models.CompoundMark;
import seng302.models.Coordinate;
import seng302.models.WindAngleAndSpeed;

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
    public static double lagrangeInterpolation(WindAngleAndSpeed A, WindAngleAndSpeed B, WindAngleAndSpeed C, double x) {
        return (((x - B.getWindAngle()) * (x - C.getWindAngle())) / ((A.getWindAngle() - B.getWindAngle()) * (A.getWindAngle() - C.getWindAngle()))) * A.getSpeed() + (((x - A.getWindAngle()) * (x - C.getWindAngle())) / ((B.getWindAngle() - A.getWindAngle()) * (B.getWindAngle() - C.getWindAngle()))) * B.getSpeed() + (((x - A.getWindAngle()) * (x - B.getWindAngle())) / ((C.getWindAngle() - A.getWindAngle()) * (C.getWindAngle() - B.getWindAngle()))) * C.getSpeed();}

    /**
     * Calculates the VMG of the boat
     * @param boatSpeed the speed of the boat
     * @param TWA the true wind angle that the boat is heading at
     * @return the velocity made good of the boat
     */
    public static double VMG(double boatSpeed, double TWA){
        return boatSpeed * Math.cos(Math.toRadians(TWA));
    }


    /**
     * Computes the bearing in degrees between two given marks (mark1 and mark2).
     * Note that mark1 and mark2 are compound marks, so in the case of a gate it
     * calculates form the midpoint of the two marks.
     * Note that bearing is calculated starting from north direction from mark1
     *  and clockwise around to mark2
     * @param mark1
     * @param mark2
     * @return
     */
    public static double calculateBearingBetweenTwoPoints(CompoundMark mark1, CompoundMark mark2) {

        boolean mark1isCompound = mark1.hasTwoMarks();
        boolean mark2isCompound = mark2.hasTwoMarks();


        Coordinate mark1A = mark1.getMark1().getPosition();
        Coordinate mark1B;
        Coordinate midPointMark1;
        if (mark1isCompound) {
            mark1B = mark1.getMark2().getPosition();
            midPointMark1 = new Coordinate((mark1A.getLat() - mark1B.getLat())/2,
                    (mark1A.getLon() - mark1B.getLon())/2);
        } else {
            midPointMark1 = mark1A;
        }




        double mark1Lat = midPointMark1.getLat();
        double mark1Lon = midPointMark1.getLon();


        Coordinate mark2A = mark2.getMark1().getPosition();
        Coordinate mark2B;
        if (mark2isCompound) {
            mark2B = mark2.getMark2().getPosition();
        } else {
            mark2B = new Coordinate(0,0);
        }

        Coordinate midPointMark2 = new Coordinate((mark2A.getLat() - mark2B.getLat())/2,
                (mark2A.getLon() - mark2B.getLon())/2);
        double mark2Lat = midPointMark2.getLat();
        double mark2Lon = midPointMark2.getLon();




        double longitude1 = mark1Lon;
        double longitude2 = mark2Lon;
        double latitude1 = Math.toRadians(mark1Lat);
        double latitude2 = Math.toRadians(mark2Lat);
        double longDiff= Math.toRadians(longitude2-longitude1);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;

    }


}
