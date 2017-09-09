package seng302.models;

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
    private static final Double ROUNDING_DISTANCE = 0.06;
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
        System.out.println(tackingGybingCoord);
        if(tackingGybingCoord != null){
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
        return distance <= CollisionManager.MARK_SENSITIVITY;
    }

    public void avoidFutureCollision() {
        for(Integer markID : course.getAllMarks().keySet()){
            Mark mark = course.getAllMarks().get(markID);
            if(checkFutureCollision(mark)){
                List<Coordinate> avoidCoords = RoundingMechanics.markRoundingCoordinates(mark, boat.getHeading(), boat.getHeading(), PORT, ROUNDING_DISTANCE);
                nextRoundingCoordinates.add(targetPositionIndex, avoidCoords.get(1));
                System.out.println(avoidCoords);
                targetPosition = nextRoundingCoordinates.get(targetPositionIndex);
                updateHeading();
            }
        }
    }

    public Coordinate addTackandGybeMarks(Coordinate lastMark, Coordinate nextMark) {
        double headingBetweenMarks = lastMark.headingToCoordinate(nextMark);
        double TrueWindAngle;
        double alphaAngle;

        if(MathUtils.pointBetweenTwoAngle(course.getWindDirection(), polarTable.getOptimumTWA(true), headingBetweenMarks)){
            TrueWindAngle = polarTable.getOptimumTWA(true);
            alphaAngle = getAlphaAngle(course.getWindDirection(), headingBetweenMarks, true);
        } else if (MathUtils.pointBetweenTwoAngle((course.getWindDirection() + 180) % 360, 180 - polarTable.getOptimumTWA(false), headingBetweenMarks)){
            TrueWindAngle = polarTable.getOptimumTWA(false);
            alphaAngle = getAlphaAngle(course.getWindDirection(), headingBetweenMarks, false);
        } else {
            return null;
        }
        double lengthOfTack = Math.abs(calculateLengthOfTack(TrueWindAngle,alphaAngle,nextMark,lastMark));
        Coordinate tackingCoord = lastMark.coordAt(lengthOfTack,alphaAngle);

        return tackingCoord;
    }

    public double calculateLengthOfTack(double TrueWindAngle, double alphaAngle,Coordinate nextMark, Coordinate lastMark){

        double lengthOfLeg = lastMark.greaterCircleDistance(nextMark);
        double betaAngle = (2*TrueWindAngle) - alphaAngle;
        double lengthOfTack = ((lengthOfLeg* Math.sin(Math.toRadians(betaAngle)))/Math.sin(Math.toRadians(180 - 2*TrueWindAngle)))/2.0;
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
