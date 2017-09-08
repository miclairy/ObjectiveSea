package seng302.models;

import seng302.controllers.CollisionManager;
import seng302.controllers.RoundingMechanics;
import seng302.data.BoatStatus;
import seng302.data.RoundingSide;
import seng302.utilities.MathUtils;

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

    public AIBoat(Boat boat, Course course) {
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
        if(currentMark instanceof RaceLine){
            nextRoundingCoordinates.add(currentMark.getPosition());
        } else {
            CompoundMark previousMark = courseOrder.get(boat.getLastRoundedMarkIndex());
            CompoundMark nextMark = courseOrder.get(boat.getLastRoundedMarkIndex()+2);

            Double heading = previousMark.getPosition().headingToCoordinate(currentMark.getPosition());
            Double nextHeading = currentMark.getPosition().headingToCoordinate(nextMark.getPosition());

            String roundingSideString = course.getRoundingOrder().get(boat.getLastRoundedMarkIndex()+1).getRoundingSideString();
            RoundingSide roundingSide;
            if(currentMark.hasTwoMarks()){
                roundingSide = roundingSideString.charAt(0) == 'P' ? RoundingSide.PORT : RoundingSide.STBD;
            } else{
                roundingSide = course.getRoundingOrder().get(boat.getLastRoundedMarkIndex()+1);
            }
            nextRoundingCoordinates = RoundingMechanics.markRoundingCoordinates(currentMark.getMark1(), heading, nextHeading, roundingSide, ROUNDING_DISTANCE);
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
}
