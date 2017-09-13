package seng302.models;

import javafx.scene.shape.Polygon;
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
public class AIBoat extends Boat{
    private static final Double ROUNDING_DISTANCE = 0.08;
    private static final Double ROUNDING_DELTA = 0.01;
    private static final Double COLLISION_CHECK_DISTANCE = 0.1;

    private List<Coordinate> nextRoundingCoordinates;
    private Integer targetPositionIndex;
    private Coordinate targetPosition;
    private Course course;
    private PolarTable polarTable;
    private Polygon boundary;


    public AIBoat(Integer id, String name, String nickName, double speed, Course course) {
        super(id, name, nickName, speed);
        this.polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), course);
        this.course = course;
        nextRoundingCoordinates = new ArrayList<>();
        setNextRoundingCoordinates();
        targetPositionIndex = 0;
        targetPosition = nextRoundingCoordinates.get(targetPositionIndex);
        CollisionManager collisionManager = new CollisionManager();
        this.boundary = collisionManager.createCourseBoundary(course.getBoundary());
    }

    public void checkRounding() {
        if(getCurrentPosition().greaterCircleDistance(targetPosition) < ROUNDING_DELTA && getStatus() != BoatStatus.FINISHED){
            targetPositionIndex++;
            if(targetPositionIndex == nextRoundingCoordinates.size()){
                targetPositionIndex = 0;
                setNextRoundingCoordinates();
                setLastRoundedMarkIndex(getLastRoundedMarkIndex()+1);
            }
            targetPosition = nextRoundingCoordinates.get(targetPositionIndex);
        }
    }

    private void setNextRoundingCoordinates(){
        List<CompoundMark> courseOrder = course.getCourseOrder();
        Integer lastRoundedIndex = getLastRoundedMarkIndex();
        if(lastRoundedIndex == courseOrder.size() - 1){
            setStatus(BoatStatus.FINISHED);
            return;
        }
        CompoundMark currentMark = courseOrder.get(getLastRoundedMarkIndex()+1);
        nextRoundingCoordinates.clear();
        Coordinate tackingGybingCoord = addTackandGybeMarks(getCurrentPosition(), currentMark.getPosition());
        if(tackingGybingCoord != null && !currentMark.isStartLine()){
            nextRoundingCoordinates.add(tackingGybingCoord);
        }
        if(currentMark instanceof RaceLine){
            nextRoundingCoordinates.add(currentMark.getPosition());
        } else {
            CompoundMark previousMark = courseOrder.get(getLastRoundedMarkIndex());
            CompoundMark nextMark = courseOrder.get(getLastRoundedMarkIndex()+2);

            if(currentMark.hasTwoMarks() && RoundingMechanics.nextCompoundMarkAfterGate(nextMark, currentMark, previousMark)) {
                nextRoundingCoordinates.add(currentMark.getPosition());
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
                nextRoundingCoordinates.addAll(RoundingMechanics.markRoundingCoordinates(currentMark.getMark1(), heading, nextHeading, roundingSide, ROUNDING_DISTANCE));
            }
        }
    }

    public void updateHeading() {
        Double headingToNextMark = getCurrentPosition().headingToCoordinate(targetPosition);
        setHeading(headingToNextMark);
    }

    public boolean checkFutureCollision(Mark mark) {
        Coordinate checkPointEnd = getCurrentPosition().coordAt(COLLISION_CHECK_DISTANCE, getHeading());
        Double distance = MathUtils.distanceToLineSegment(getCurrentPosition(), checkPointEnd, mark.getPosition());
        return distance <= CollisionManager.MARK_SENSITIVITY &&
                getCurrentPosition().greaterCircleDistance(targetPosition) > getCurrentPosition().greaterCircleDistance(mark.getPosition());
    }

    public void avoidFutureCollision() {
        for(Integer markID : course.getAllMarks().keySet()){
            Mark mark = course.getAllMarks().get(markID);
            if(checkFutureCollision(mark)){
                Coordinate avoidCoordinate = coordinateToAvoid(mark.getPosition());
                nextRoundingCoordinates.add(targetPositionIndex, avoidCoordinate);
                System.out.println("Collision with: " + mark.getSourceID());
                System.out.println("Avoiding to: " + avoidCoordinate);
                targetPosition = nextRoundingCoordinates.get(targetPositionIndex);
                updateHeading();
            }
        }
    }

    private Coordinate coordinateToAvoid(Coordinate collisionPosition) {
        Double avoidHeading = (getHeading() + 270) % 360;
        Coordinate coord1 = collisionPosition.coordAt(ROUNDING_DISTANCE, avoidHeading);

        Double avoidHeading2 = (getHeading() + 90) % 360;
        Coordinate coord2 = collisionPosition.coordAt(ROUNDING_DISTANCE, avoidHeading2);

        Double dist1 = getCurrentPosition().greaterCircleDistance(coord1) + coord1.greaterCircleDistance(targetPosition);
        Double dist2 = getCurrentPosition().greaterCircleDistance(coord2) + coord2.greaterCircleDistance(targetPosition);

        return dist1 < dist2 ? coord1 : coord2;
    }

    public Coordinate addTackandGybeMarks(Coordinate lastMark, Coordinate nextMark) {
        double TWD = course.getWindDirection();
        double heading = getHeading();
        double headingBetweenMarks = lastMark.headingToCoordinate(nextMark);

        double trueWindAngle;
        double optimumAngle;

        double optimumHeadingA;
        double optimumHeadingB;

        if(MathUtils.pointBetweenTwoAngle(TWD, polarTable.getOptimumTWA(true), headingBetweenMarks)){
            System.out.println("TACK");
            trueWindAngle = polarTable.getOptimumTWA(true);
        } else if (MathUtils.pointBetweenTwoAngle((TWD + 180) % 360, 180 - polarTable.getOptimumTWA(false), headingBetweenMarks)){
            trueWindAngle = polarTable.getOptimumTWA(false);
            System.out.println(trueWindAngle);
            System.out.println("GYBE " + TWD + "h " + headingBetweenMarks);
        } else {
            return null;
        }

        optimumHeadingA = (TWD - trueWindAngle + 360) % 360;
        optimumHeadingB = (TWD + trueWindAngle + 360) % 360;

        double angleToOptimumA = MathUtils.getAngleBetweenTwoHeadings(heading, optimumHeadingA);
        double angleToOptimumB = MathUtils.getAngleBetweenTwoHeadings(heading, optimumHeadingB);

        if (angleToOptimumA <= angleToOptimumB) {
            optimumAngle = optimumHeadingB;
        } else {
            optimumAngle = optimumHeadingA;
        }

        double lengthOfTack = Math.abs(calculateLengthOfTack(trueWindAngle,nextMark,lastMark));

        Coordinate tackingCoord = lastMark.coordAt(lengthOfTack,optimumAngle);
        if(boundary != null && !boundary.contains(tackingCoord.getLat(), tackingCoord.getLon())) {
            Double distance = distanceToBoundary(lastMark, tackingCoord, course.getBoundary());
            System.out.println("Distance: " + distance);
            tackingCoord = lastMark.coordAt(distance, optimumAngle);
        }
        return tackingCoord;
    }

    private Double distanceToBoundary(Coordinate lastMark, Coordinate tackingCoord, ArrayList<Coordinate> boundary) {
        for(int i = 0; i < boundary.size(); i++){
            Coordinate boundaryEnd1 = boundary.get(i);
            Coordinate boundaryEnd2 = boundary.get((i+1) % boundary.size());
            if(crossingBoundary(lastMark, tackingCoord, boundaryEnd1, boundaryEnd2)){
                InfiniteLine infiniteLine1 = new InfiniteLine(lastMark, tackingCoord);
                InfiniteLine infiniteLine2 = new InfiniteLine(boundaryEnd1, boundaryEnd2);
                Coordinate intersectionPoint = InfiniteLine.intersectionPoint(infiniteLine1, infiniteLine2);
                return lastMark.greaterCircleDistance(intersectionPoint);
            }
        }
        return null;
    }

    private boolean crossingBoundary(Coordinate lastMark, Coordinate tackingCoord, Coordinate boundaryEnd1, Coordinate boundaryEnd2) {
        InfiniteLine infiniteLine1 = new InfiniteLine(lastMark, tackingCoord);
        InfiniteLine infiniteLine2 = new InfiniteLine(boundaryEnd1, boundaryEnd2);
        Coordinate intersectionPoint = InfiniteLine.intersectionPoint(infiniteLine1, infiniteLine2);
        double minLat = Math.min(boundaryEnd1.getLat(), boundaryEnd2.getLat());
        double minLon = Math.min(boundaryEnd1.getLon(), boundaryEnd2.getLon());
        double maxLat = Math.max(boundaryEnd1.getLat(), boundaryEnd2.getLat());
        double maxLon = Math.max(boundaryEnd1.getLon(), boundaryEnd2.getLon());

        return intersectionPoint.getLat() >= minLat && intersectionPoint.getLat() <= maxLat &&
                intersectionPoint.getLon() >= minLon && intersectionPoint.getLon() <= maxLon;
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


    /**
     * Updates the location of a given boat to be displayed to the clients
     * @param raceSecondsPassed time passed since last update in seconds
     * @param course the course the boat is racing on
     */
    public void move(double raceSecondsPassed, Course course) {
        updateHeading();
        avoidFutureCollision();
        updateLocation(raceSecondsPassed, course);
        checkRounding();
    }
}
