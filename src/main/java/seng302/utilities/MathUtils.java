package seng302.utilities;

import seng302.models.CompoundMark;
import seng302.models.Coordinate;
import seng302.models.Mark;
import seng302.models.CompoundMark;
import seng302.models.Coordinate;
import seng302.models.WindAngleAndSpeed;

/**
 * Created by gla42 on 11/05/17.
 */
public class MathUtils {

    /**
     * A function to determine whether a given direction is within an angle of a target direction
     * @param targetDirection the direction to check against
     * @param deltaAngle the maximum allowed variance of directions
     * @param actualDirection the direction to test
     * @return true if he boat is within angle
     */
    public static boolean pointBetweenTwoAngle(double targetDirection, double deltaAngle, double actualDirection){
        double diff;
        double middle;
        if(targetDirection > 180){
            actualDirection -= 180;
            middle = targetDirection - 180;
        } else {
            middle = 90;
            diff = Math.abs(90 - targetDirection);
            actualDirection += diff;
            actualDirection = (actualDirection + 360) % 360;}
        return (middle - deltaAngle) <= actualDirection && actualDirection <= (middle + deltaAngle);
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

    /**
     * Function to determine if the boat which is on the correct side of the sideline is heading towards it
     * @param boatsHeading heading of boat
     * @param lineHeading the heading of the line, if the boat is heading towards it the boat will essentially be heading on either side of the perpendicular line ot it
     * @param headingOfMark the heading from the mark to the startline
     * @return true if the boat is heading in the general direction of the line
     */
    public static Boolean boatHeadingToLine(double boatsHeading, double lineHeading, double headingOfMark){
        Boolean towardsLine = false;

        if(pointBetweenTwoAngle((lineHeading + 270)%360, 90, boatsHeading) && pointBetweenTwoAngle(lineHeading + 90, 90, headingOfMark)){
            towardsLine = true;
        } else if(pointBetweenTwoAngle(lineHeading + 90, 90, boatsHeading) && pointBetweenTwoAngle((lineHeading + 270)%360, 90, headingOfMark)){
            towardsLine = true;
        }
        return towardsLine;
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

    /**
     * A function for calculating the bilinear interpolation of a third variable (e.g if windspeed and heading are
     * known this function will calculate the boat speed at those values
     * @param x0 first value in x axis
     * @param x1 second value in x axis
     * @param y0 first value in y axis
     * @param y1 second value in y axis
     * @param z00 the z value at (x0,y0)
     * @param z01 the z value at (x0,y1)
     * @param z10 the z value at (x1,y0)
     * @param z11 the z value at (x1,y1)
     * @param x the x value you want to compute z at (e.g the current windspeed)
     * @param y the y value you want to compute z at (e.g the current boat heading)
     * @return the interpolated z value (e.g the new boat speed at current heading and wind speed)
     */
    public static double bilinearInterpolation(double x0, double x1, double y0, double y1, double z00, double z01, double z10, double z11, double x, double y){

        double r1 = (((x1 - x)/(x1 - x0)) * z00 + ((x - x0)/(x1 - x0)) * z10);
        double r2 = (((x1 - x)/(x1 - x0)) * z01 + ((x - x0)/(x1 - x0)) * z11);

        return (((y1 - y)/(y1 - y0)) * r1 + ((y - y0)/ (y1 - y0)) * r2);
    }


}
