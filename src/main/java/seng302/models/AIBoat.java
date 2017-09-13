package seng302.models;

import org.omg.CORBA.TRANSACTION_MODE;
import seng302.controllers.CollisionManager;
import seng302.controllers.RoundingMechanics;
import seng302.data.BoatStatus;
import seng302.data.RoundingSide;
import seng302.utilities.MathUtils;
import seng302.utilities.PolarReader;

import java.util.ArrayList;
import java.util.List;

import static seng302.data.RoundingSide.PORT;

/**
 * Created by atc60 on 8/09/17.
 */
public class AIBoat {
    private static final Double ROUNDING_DISTANCE = 0.08;
    private static final Double ROUNDING_DELTA = 0.01;
    private static final Double COLLISION_CHECK_DISTANCE = 0.1;

    private Boat boat;
    private List<Coordinate> nextRoundingCoordinates;
    private Integer targetPositionIndex;
    private Coordinate targetPosition;
    private Course course;
    private PolarTable polarTable;

    public AIBoat(Boat boat, Course course) {
        this.polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), course);
        this.boat = boat;
        this.course = course;
        nextRoundingCoordinates = new ArrayList<>();
        setNextRoundingCoordinates();
        targetPositionIndex = 0;
        targetPosition = nextRoundingCoordinates.get(targetPositionIndex);
    }

    public void checkRounding() {
        if(boat.getCurrentPosition().greaterCircleDistance(targetPosition) < ROUNDING_DELTA){
            targetPositionIndex++;
            if(targetPositionIndex == nextRoundingCoordinates.size()){
                targetPositionIndex = 0;
                setNextRoundingCoordinates();
                boat.setLastRoundedMarkIndex(boat.getLastRoundedMarkIndex()+1);
            }
            targetPosition = nextRoundingCoordinates.get(targetPositionIndex);
        }
    }

    private void setNextRoundingCoordinates(){
        List<CompoundMark> courseOrder = course.getCourseOrder();
        Integer lastRoundedIndex = boat.getLastRoundedMarkIndex();
        if(lastRoundedIndex == courseOrder.size() - 1){
            boat.setStatus(BoatStatus.FINISHED);
            return;
        }
        CompoundMark currentMark = courseOrder.get(boat.getLastRoundedMarkIndex()+1);
        nextRoundingCoordinates.clear();
        Coordinate tackingGybingCoord = addTackandGybeMarks(boat.getCurrentPosition(), currentMark.getPosition());
        if(tackingGybingCoord != null && !currentMark.isStartLine()){
            nextRoundingCoordinates.add(tackingGybingCoord);
        }
        if(currentMark instanceof RaceLine){
            nextRoundingCoordinates.add(currentMark.getPosition());
        } else {
            CompoundMark previousMark = courseOrder.get(boat.getLastRoundedMarkIndex());
            CompoundMark nextMark = courseOrder.get(boat.getLastRoundedMarkIndex()+2);

            if(currentMark.hasTwoMarks() && RoundingMechanics.nextCompoundMarkAfterGate(nextMark, currentMark, previousMark)) {
                nextRoundingCoordinates.add(currentMark.getPosition());
            } else {
                Double heading = previousMark.getPosition().headingToCoordinate(currentMark.getPosition());
                Double nextHeading = currentMark.getPosition().headingToCoordinate(nextMark.getPosition());

                String roundingSideString = course.getRoundingOrder().get(boat.getLastRoundedMarkIndex()+1).getRoundingSideString();
                RoundingSide roundingSide;
                if(currentMark.hasTwoMarks()){
                    roundingSide = roundingSideString.charAt(0) == 'P' ? RoundingSide.PORT : RoundingSide.STBD;
                } else{
                    roundingSide = course.getRoundingOrder().get(boat.getLastRoundedMarkIndex()+1);
                }
                nextRoundingCoordinates.addAll(RoundingMechanics.markRoundingCoordinates(currentMark.getMark1(), heading, nextHeading, roundingSide, ROUNDING_DISTANCE));
            }
        }
    }

    public void updateHeading() {
        Double headingToNextMark = boat.getCurrentPosition().headingToCoordinate(targetPosition);
        boat.setHeading(headingToNextMark);
    }

    public boolean checkFutureCollision(Mark mark) {
        Coordinate checkPointEnd = boat.getCurrentPosition().coordAt(COLLISION_CHECK_DISTANCE, boat.getHeading());
        Double distance = MathUtils.distanceToLineSegment(boat.getCurrentPosition(), checkPointEnd, mark.getPosition());
        return distance <= CollisionManager.MARK_SENSITIVITY && boat.getCurrentPosition().greaterCircleDistance(targetPosition) > boat.getCurrentPosition().greaterCircleDistance(mark.getPosition());
    }

    public void avoidFutureCollision() {
        for(Integer markID : course.getAllMarks().keySet()){
            Mark mark = course.getAllMarks().get(markID);
            if(checkFutureCollision(mark)){

                Coordinate avoidCoordinate = coordinateToAvoid(boat, mark.getPosition());
                nextRoundingCoordinates.add(targetPositionIndex, avoidCoordinate);
                System.out.println("Collision with: " + mark.getSourceID());
                System.out.println("Avoiding to: " + avoidCoordinate);
                targetPosition = nextRoundingCoordinates.get(targetPositionIndex);
                updateHeading();
            }
        }
    }

    private Coordinate coordinateToAvoid(Boat boat, Coordinate collisionPosition) {
        Double avoidHeading = (boat.getHeading() + 270) % 360;
        Coordinate coord1 = collisionPosition.coordAt(ROUNDING_DISTANCE, avoidHeading);

        Double avoidHeading2 = (boat.getHeading() + 90) % 360;
        Coordinate coord2 = collisionPosition.coordAt(ROUNDING_DISTANCE, avoidHeading2);

        Double dist1 = boat.getCurrentPosition().greaterCircleDistance(coord1) + coord1.greaterCircleDistance(targetPosition);
        Double dist2 = boat.getCurrentPosition().greaterCircleDistance(coord2) + coord2.greaterCircleDistance(targetPosition);

        return dist1 < dist2 ? coord1 : coord2;
    }

    public Coordinate addTackandGybeMarks(Coordinate lastMark, Coordinate nextMark) {
        double TWD = course.getWindDirection();
        double heading = boat.getHeading();
        double headingBetweenMarks = lastMark.headingToCoordinate(nextMark);
        double TWA = Math.abs(TWD - headingBetweenMarks);

        double TrueWindAngle;

        double optimumAngle;

        double optimumHeadingA;
        double optimumHeadingB;

        if(MathUtils.pointBetweenTwoAngle(TWD, polarTable.getOptimumTWA(true), headingBetweenMarks)){
            System.out.println("TACK");
            TrueWindAngle = polarTable.getOptimumTWA(true);
        } else if (MathUtils.pointBetweenTwoAngle((TWD + 180) % 360, 180 - polarTable.getOptimumTWA(false), headingBetweenMarks)){
            TrueWindAngle = polarTable.getOptimumTWA(false);
            System.out.println(TrueWindAngle);
            System.out.println("GYBE " + TWD + "h " + headingBetweenMarks);
        } else {
            return null;
        }

        optimumHeadingA = (TWD - TrueWindAngle + 360) % 360;
        optimumHeadingB = (TWD + TrueWindAngle + 360) % 360;

        double angleToOptimumA = MathUtils.getAngleBetweenTwoHeadings(heading, optimumHeadingA);
        double angleToOptimumB = MathUtils.getAngleBetweenTwoHeadings(heading, optimumHeadingB);

        if (angleToOptimumA <= angleToOptimumB) {
            optimumAngle = optimumHeadingB;
        } else {
            optimumAngle = optimumHeadingA;
        }

        double lengthOfTack = Math.abs(calculateLengthOfTack(TrueWindAngle,nextMark,lastMark));

        Coordinate tackingCoord = lastMark.coordAt(lengthOfTack,optimumAngle);

        return tackingCoord;
    }

    public double calculateLengthOfTack(double TrueWindAngle,Coordinate nextMark, Coordinate lastMark){
        double lengthOfLeg = lastMark.greaterCircleDistance(nextMark);
        double lengthOfTack = (lengthOfLeg/2.0)/(Math.cos(Math.toRadians(TrueWindAngle)));
        return lengthOfTack;
    }

    /**
     * @param windDirection the current wind direction for the course
     * @param bearing
     * @param onTack whether calculateOptimumTack is happening, or calculateOptimumGybe
     * @return the alpha angle
     */
    private double getAlphaAngle(double windDirection, double bearing, boolean onTack) {
        double alphaAngle;
        if(bearing <= (windDirection + 90.0)){
            alphaAngle = Math.abs(bearing - windDirection) % 360;
        } else {
            alphaAngle = (360 + windDirection - bearing) % 360;
        }
        return onTack ? alphaAngle : 360 - alphaAngle;
    }
}
