package seng302.utilities;

import javafx.util.Pair;
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
     * Function to determine if a boat is on the correct side of the start line (e.g if the boat is on the side that the course isn't on)
     * @param BoatLat the latitude of the boat
     * @param BoatLong the longitude of the boat
     * @param startMark1Lat the latitude of the first start mark
     * @param startMark1Long the longitude of the first start mark
     * @param startMark2Lat the latitude of the first second mark
     * @param startMark2Long the longitude of the first second mark
     * @param markLat the latitude of the first mark
     * @param markLong the longitude of the first mark
     * @return true if the boat is on the correct side of the start line
     */
    public static Boolean boatBeforeStartline(double BoatLat, double BoatLong, double startMark1Lat, double startMark1Long, double startMark2Lat, double startMark2Long, double markLat, double markLong){
        double determinantOfMark = (markLong - startMark1Long)*(startMark2Lat - startMark1Lat) - (markLat - startMark1Lat)*(startMark2Long - startMark1Long);
        double determinantOfBoat = (BoatLong - startMark1Long)*(startMark2Lat - startMark1Lat) - (BoatLat - startMark1Lat)*(startMark2Long - startMark1Long);
        if(determinantOfBoat > 0 && determinantOfMark < 0){
            return true;
        } else if(determinantOfBoat < 0 && determinantOfMark > 0){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Calculates the midpoint between two marks
     * @param mark first mark
     * @return new coordinate at half way between the two marks
     */
    public static Coordinate calculateMidPoint(CompoundMark mark){
        Coordinate mark1Coord = mark.getMark1().getPosition();
        Coordinate mark2Coord = mark.getMark2().getPosition();
        Double halfLat = (mark1Coord.getLat() + mark2Coord.getLat()) / 2;
        Double halfLong = (mark1Coord.getLon() + mark2Coord.getLon()) / 2;
        return new Coordinate(halfLat,halfLong);
    }


}
