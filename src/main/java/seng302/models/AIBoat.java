package seng302.models;

import javafx.scene.shape.Polygon;
import seng302.controllers.CollisionManager;
import seng302.controllers.RoundingMechanics;
import seng302.data.BoatStatus;
import seng302.data.RaceStatus;
import seng302.data.RoundingSide;
import seng302.utilities.MathUtils;
import seng302.utilities.PolarReader;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by atc60 on 8/09/17.
 * A class to control an AI boat for the user
 * to practice racing against
 */
public class AIBoat extends Boat{
    private static final Double ROUNDING_DISTANCE = 0.08;
    private static final Double ROUNDING_DELTA = 0.01;
    private static final Double COLLISION_CHECK_DISTANCE = 0.1;
    private static final Double BOUNDARY_OFFSET = 0.05;

    private List<Coordinate> nextCoordinates;
    private Integer targetPositionIndex;
    private Course course;
    private PolarTable polarTable;
    private Polygon boundary;
    private int AIDifficulty;

    public AIBoat(Integer id, String name, String nickName, double speed, Course course, int AIDifficulty) {
        super(id, name, nickName, speed);
        this.AIDifficulty = AIDifficulty;
        this.polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), course);
        this.course = course;
        nextCoordinates = new ArrayList<>();
        setNextRoundingCoordinates();
        targetPositionIndex = 0;
        CollisionManager collisionManager = new CollisionManager();
        this.boundary = collisionManager.createCourseBoundary(course.getBoundary());
    }

    /**
     * This function checks whether an AI boat has rounded a mark
     * If it has, it updates the next mark to round to be the next mark
     * in the course using setNextRoundingCoordinates
     */
    public void checkRounding() {
        Coordinate targetPosition = nextCoordinates.get(targetPositionIndex);
        if(getCurrentPosition().greaterCircleDistance(targetPosition) < ROUNDING_DELTA && getStatus() != BoatStatus.FINISHED){
            targetPositionIndex++;
            if(targetPositionIndex == nextCoordinates.size()){
                targetPositionIndex = 0;
                setNextRoundingCoordinates();
                setLastRoundedMarkIndex(getLastRoundedMarkIndex()+1);
            }
        }
    }

    /**
     * This function sets the next rounding coordinate of the AI
     * boat to be that of the next mark in the course
     * If the last rounded mark is the finish line
     * the boat has its status set as finished.
     */
    private void setNextRoundingCoordinates(){
        List<CompoundMark> courseOrder = course.getCourseOrder();
        Integer lastRoundedIndex = getLastRoundedMarkIndex();
        if(lastRoundedIndex == courseOrder.size() - 1){
            setStatus(BoatStatus.FINISHED);
            return;
        }
        CompoundMark currentMark = courseOrder.get(getLastRoundedMarkIndex()+1);
        nextCoordinates.clear();

        if(currentMark != course.getStartLine() && AIDifficulty == 2){
            addTackandGybeMarks(getCurrentPosition(), currentMark.getPosition(), 0);
        }

        if(currentMark instanceof RaceLine){
            nextCoordinates.add(currentMark.getPosition());
        } else {
            CompoundMark previousMark = courseOrder.get(getLastRoundedMarkIndex());
            CompoundMark nextMark = courseOrder.get(getLastRoundedMarkIndex()+2);

            if(currentMark.hasTwoMarks() && RoundingMechanics.nextCompoundMarkAfterGate(nextMark, currentMark, previousMark)) {
                nextCoordinates.add(currentMark.getPosition());
            } else {
                Double heading = previousMark.getPosition().headingToCoordinate(currentMark.getPosition());
                Double nextHeading = currentMark.getPosition().headingToCoordinate(nextMark.getPosition());

                String roundingSideString = course.getRoundingOrder().get(getLastRoundedMarkIndex()+1).getRoundingSideString();
                RoundingSide roundingSide;
                if(currentMark.hasTwoMarks()){
                    roundingSide = roundingSideString.charAt(0) == 'P' ? RoundingSide.PORT : RoundingSide.STBD;
                } else{
                    roundingSide = course.getRoundingOrder().get(getLastRoundedMarkIndex()+1);
                }
                nextCoordinates.addAll(RoundingMechanics.markRoundingCoordinates(currentMark.getMark1(), heading, nextHeading, roundingSide, ROUNDING_DISTANCE));
            }
        }
    }

    /**
     * Updates the target heading of the boat
     * using the current position and the
     * desired position of the boat
     */
    public void updateTargetHeading() {
        Coordinate targetPosition = nextCoordinates.get(targetPositionIndex);
        Double headingToNextMark = getCurrentPosition().headingToCoordinate(targetPosition);
        setTargetHeading(headingToNextMark);
        rotate = true;
    }

    /**
     * Checks whether the AI boat is in line to collide with future
     * marks
     * @param mark The mark the AI boat is potentially colliding with
     * @return true if the AI boat will collide with mark, false otherwise
     */
    public boolean checkFutureCollision(Mark mark) {
        Coordinate targetPosition = nextCoordinates.get(targetPositionIndex);
        Coordinate checkPointEnd = getCurrentPosition().coordAt(COLLISION_CHECK_DISTANCE, heading);
        Double distance = MathUtils.distanceToLineSegment(getCurrentPosition(), checkPointEnd, mark.getPosition());
        return distance <= CollisionManager.MARK_SENSITIVITY &&
                getCurrentPosition().greaterCircleDistance(targetPosition) > getCurrentPosition().greaterCircleDistance(mark.getPosition());
    }

    /**
     * If the AI boat is on course for a collision
     * it will add a new mark to its target position
     * list.  This new mark will set the boat onto a non-colliding route
     */
    public void avoidFutureCollision() {
        for(Integer markID : course.getAllMarks().keySet()){
            Mark mark = course.getAllMarks().get(markID);
            if(checkFutureCollision(mark)){
                Coordinate avoidCoordinate = coordinateToAvoid(mark.getPosition());
                nextCoordinates.add(targetPositionIndex, avoidCoordinate);
                System.out.println("Collision with: " + mark.getSourceID());
                System.out.println("Avoiding to: " + avoidCoordinate);
            }
        }
    }

    /**
     * Function computes position of new avoidance
     * target coordinate for the boat to head
     * towards
     * @param collisionPosition the position of the potential colliding mark
     * @return the new target coordinate
     */
    private Coordinate coordinateToAvoid(Coordinate collisionPosition) {
        Coordinate targetPosition = nextCoordinates.get(targetPositionIndex);
        Double avoidHeading = (heading + 270) % 360;
        Coordinate coord1 = collisionPosition.coordAt(ROUNDING_DISTANCE, avoidHeading);

        Double avoidHeading2 = (heading + 90) % 360;
        Coordinate coord2 = collisionPosition.coordAt(ROUNDING_DISTANCE, avoidHeading2);

        Double dist1 = getCurrentPosition().greaterCircleDistance(coord1) + coord1.greaterCircleDistance(targetPosition);
        Double dist2 = getCurrentPosition().greaterCircleDistance(coord2) + coord2.greaterCircleDistance(targetPosition);
        return dist1 < dist2 ? coord1 : coord2;
    }

    /**
     * Checks whether the AI boat needs to be tacking or gybing
     * and adds the new tacking/gybing marks to the target
     * coordinate list if needed
     * @param lastCoordinate the previous Coordinate
     * @param nextCoordinate the next Coordinate
     * @param headingOption the heading switch option (allows boat to zigzag)
     */
    public void addTackandGybeMarks(Coordinate lastCoordinate, Coordinate nextCoordinate, Integer headingOption) {
        double TWD = course.getWindDirection();
        double headingBetweenMarks = lastCoordinate.headingToCoordinate(nextCoordinate);

        double trueWindAngle;
        double optimumAngle;

        if(MathUtils.pointBetweenTwoAngle(TWD, polarTable.getOptimumTWA(true), headingBetweenMarks)){
            trueWindAngle = polarTable.getOptimumTWA(true);
        } else if (MathUtils.pointBetweenTwoAngle((TWD + 180) % 360, 180 - polarTable.getOptimumTWA(false), headingBetweenMarks)){
            trueWindAngle = polarTable.getOptimumTWA(false);
        } else{
            //Don't need to tack/gybe
            return;
        }

        double optimumHeadingA = (TWD - trueWindAngle + 360) % 360;
        double optimumHeadingB = (TWD + trueWindAngle + 360) % 360;

        double angleToOptimumA = MathUtils.getAngleBetweenTwoHeadings(heading, optimumHeadingA);
        double angleToOptimumB = MathUtils.getAngleBetweenTwoHeadings(heading, optimumHeadingB);
        if(headingOption == 1){
            optimumAngle = optimumHeadingA;
        } else if(headingOption == -1){
            optimumAngle = optimumHeadingB;
        } else{
            if (angleToOptimumA <= angleToOptimumB) {
                optimumAngle = optimumHeadingB;
                headingOption = -1;
            } else {
                optimumAngle = optimumHeadingA;
                headingOption = 1;
            }
        }

        double lengthOfTack = Math.abs(calculateLengthOfTack(trueWindAngle, nextCoordinate, lastCoordinate));

        Coordinate tackingCoord = lastCoordinate.coordAt(lengthOfTack,optimumAngle);
        if(boundary != null && !boundary.contains(tackingCoord.getLat(), tackingCoord.getLon())) {
            Double distance = distanceToBoundary(lastCoordinate, tackingCoord, course.getBoundary());
            tackingCoord = lastCoordinate.coordAt(distance - BOUNDARY_OFFSET, optimumAngle);
            nextCoordinates.add(tackingCoord);
            addTackandGybeMarks(tackingCoord, nextCoordinate, headingOption * -1);
        } else{
            nextCoordinates.add(tackingCoord);
        }
    }

    /**
     * Calculates the distance to the boundary that a boat
     * on route to target position, may exit through
     * @param lastMark the last position of the boat
     * @param tackingCoord the current tacking coordinate (out of bounds)
     * @param boundary the list of coordinates on the boundary
     * @return if a crossing is found, returns the distance, otherwise returns null
     */
    private Double distanceToBoundary(Coordinate lastMark, Coordinate tackingCoord, ArrayList<Coordinate> boundary) {
        for(int i = 0; i < boundary.size(); i++){
            Coordinate boundaryEnd1 = boundary.get(i);
            Coordinate boundaryEnd2 = boundary.get((i+1) % boundary.size());
            if(crossingLines(lastMark, tackingCoord, boundaryEnd1, boundaryEnd2)){
                InfiniteLine infiniteLine1 = new InfiniteLine(lastMark, tackingCoord);
                InfiniteLine infiniteLine2 = new InfiniteLine(boundaryEnd1, boundaryEnd2);
                Coordinate intersectionPoint = InfiniteLine.intersectionPoint(infiniteLine1, infiniteLine2);
                return lastMark.greaterCircleDistance(intersectionPoint);
            }
        }
        return null;
    }

    /**
     * Calculates if given coordinates (forming two lines) intersect
     * @param lastMark the start of boats target position line
     * @param tackingCoord the end of the boats target position line
     * @param boundaryEnd1 the start of a boundary line
     * @param boundaryEnd2 the end of a boundary line
     * @return true if the two finite lines cross, false otherwise.
     */
    private boolean crossingLines(Coordinate lastMark, Coordinate tackingCoord, Coordinate boundaryEnd1, Coordinate boundaryEnd2) {
        return Line2D.linesIntersect(lastMark.getLon(), lastMark.getLat(), tackingCoord.getLon(), tackingCoord.getLat(),
                                    boundaryEnd1.getLon(), boundaryEnd1.getLat(), boundaryEnd2.getLon(), boundaryEnd2.getLat());
    }

    /**
     * calculates the optimum length of a tack (one that gives minimum number of turns)
     * @param TrueWindAngle the optimum angle the boat should head for maximum speed
     * @param nextMark the target position of the boat
     * @param lastMark the previous position of the boat
     * @return length before next turn
     */
    public double calculateLengthOfTack(double TrueWindAngle,Coordinate nextMark, Coordinate lastMark){
        double lengthOfLeg = lastMark.greaterCircleDistance(nextMark);
        double lengthOfTack = (lengthOfLeg/2.0)/(Math.cos(Math.toRadians(TrueWindAngle)));
        return lengthOfTack;
    }

    public void updateAIBoatHeading(double raceSecondsPassed){
        Double defaultTurnAngle = 9 * raceSecondsPassed;
        Double upDiff = (targetHeading - heading + 360) % 360;
        Double downDiff = (heading - targetHeading + 360) % 360;
        if(upDiff < downDiff){
            Double angleOfRotation = Math.min(defaultTurnAngle, upDiff);
            heading = (heading + angleOfRotation) % 360;
        } else {
            Double angleOfRotation = Math.min(defaultTurnAngle, downDiff);
            heading = (heading - angleOfRotation + 360) % 360;
        }
    }

    /**
     * Updates the location of a given boat to be displayed to the clients
     * @param raceSecondsPassed time passed since last update in seconds
     * @param course the course the boat is racing on
     */
    public void move(double raceSecondsPassed, Course course, RaceStatus raceStatus) {
        if(raceStatus.equals(RaceStatus.PREPARATORY)){
            setSailsIn(true);
            return;
        }
        updateTargetHeading();
        setSailsIn(false);
        updateAIBoatHeading(raceSecondsPassed);
        avoidFutureCollision();
        updateLocation(raceSecondsPassed, course);
        checkRounding();
    }
}
