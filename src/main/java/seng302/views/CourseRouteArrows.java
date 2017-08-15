package seng302.views;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import seng302.controllers.RoundingMechanics;
import seng302.data.RoundingSide;
import seng302.models.CompoundMark;
import seng302.models.Coordinate;
import seng302.models.Course;
import seng302.models.Mark;
import seng302.utilities.DisplayUtils;

import java.util.*;

/**
 * Class to draw the arrows to display the course order
 */
public class CourseRouteArrows {

    private final int REFRESH_THRESHOLD = 8;
    private final int ARROW_ITERATIONS_SHOWN = 3;

    private Course course;
    private Group root;
    private Integer refreshTimer;
    private Boolean arrowsShown = false;
    private Color ARROW_PATH_COLOR = Color.color(0.25, 0.8, 0.25); //Light Green

    private ArrowOrderGraph arrowOrderGraph;
    private List<Set<Arrow>> shownArrows;

    public CourseRouteArrows(Course course, Group root) {
        this.course = course;
        this.root = root;
        createArrowedRoute();
        refreshTimer = 0;
        shownArrows = new ArrayList<>();
    }

    /**
     * Every REFRESH_THRESHOLD method calls, this method calls the updateArrowAnimation to the
     * draw the next step in the arrow path.
     */
    public void updateCourseArrows(){
        if(!arrowsShown) return;

        if (refreshTimer == 0){
            updateArrowAnimation();
        }
        refreshTimer = (refreshTimer + 1) % REFRESH_THRESHOLD;
    }

    /**
     * Moves the arrow path animation to the next step.
     */
    private void updateArrowAnimation(){
        for(Arrow currentArrow : arrowOrderGraph.getAllArrows()){
            currentArrow.fade();
        }
        arrowOrderGraph.moveToNextArrows();
        updateShownArrowList(arrowOrderGraph.getCurrentArrows());
        for(Set<Arrow> arrowIteration : shownArrows){
            for(Arrow currentArrow : arrowIteration){
                currentArrow.emphasize();
            }
        }
    }

    private void updateShownArrowList(Set<Arrow> currentArrows) {
        if(shownArrows.size() == ARROW_ITERATIONS_SHOWN) {
            shownArrows.remove(0);
        }
        shownArrows.add(currentArrows);
    }

    /**
     * Creates and adds arrows to the arrowRoute
     * @param previousMark The starting compound mark for the leg
     * @param currentMark The ending compound mark for the leg
     */
    private List<Arrow> addLegArrows(CompoundMark previousMark, CompoundMark currentMark) {
        List<Arrow> arrowList = new ArrayList<>();
        double legLength = currentMark.getPosition().greaterCircleDistance(previousMark.getPosition());
        Integer numArrows = (int)((legLength / 0.1) - 1);
        if (numArrows < 1){
            numArrows = 1;
        }
        double heading = previousMark.getPosition().headingToCoordinate(currentMark.getPosition());
        for (int num = 1; num <= numArrows; num++) {
            Coordinate position = previousMark.getPosition().coordAt((legLength / (numArrows + 1)) * num, heading);
            Arrow arrow = new Arrow(5, 10, position);
            arrow.rotate(heading + 180);
            arrowList.add(arrow);
        }
        arrowOrderGraph.addEdges(arrowList);
        return arrowList;
    }

    private List<Arrow> markRoundingArrows(CompoundMark currentMark, CompoundMark previousMark, CompoundMark nextMark, RoundingSide roundingSide){
        Double heading = previousMark.getPosition().headingToCoordinate(currentMark.getPosition());
        double nextHeading = currentMark.getPosition().headingToCoordinate(nextMark.getPosition());

        List<Arrow> arrowList = new ArrayList<>();

        // Return if goes straight through gate without rounding
        if(currentMark.hasTwoMarks() && RoundingMechanics.nextCompoundMarkAfterGate(nextMark, currentMark, previousMark)){
            return arrowList;
        }

        switch(roundingSide){
            case PORT:
                arrowList.addAll(addMarkRoundingArrows(currentMark.getMark1(), heading, nextHeading, 1));
                break;
            case STBD:
                arrowList.addAll(addMarkRoundingArrows(currentMark.getMark1(), heading, nextHeading, -1));
                break;
            case PORT_STBD:
                arrowList.addAll(addMarkRoundingArrows(currentMark.getMark1(), heading, nextHeading, 1));
                arrowList.addAll(addMarkRoundingArrows(currentMark.getMark2(), heading, nextHeading, -1));
                break;
            case STBD_PORT:
                arrowList.addAll(addMarkRoundingArrows(currentMark.getMark1(), heading, nextHeading, -1));
                arrowList.addAll(addMarkRoundingArrows(currentMark.getMark2(), heading, nextHeading, 1));
                break;
        }

        return arrowList;
    }

    private List<Arrow> addMarkRoundingArrows(Mark mark, double heading, double nextHeading, int isPort) {
        List<Arrow> arrowList = new ArrayList<>();

        Coordinate firstArrowPosition = mark.getPosition().coordAt(isPort * 0.05, (heading + 90));
        Coordinate finalArrowPosition = mark.getPosition().coordAt(isPort * 0.05, (nextHeading + 90));

        Double interpolatedHeading = firstArrowPosition.headingToCoordinate(finalArrowPosition);
        Coordinate middleArrowPosition = mark.getPosition().coordAt(isPort * 0.05, (interpolatedHeading + 90));

        Arrow mark1Arrow = new Arrow(5, 10, firstArrowPosition, (heading + 180));
        arrowList.add(mark1Arrow);
        Arrow mark1ArrowInterpolated = new Arrow(5, 10, middleArrowPosition, (interpolatedHeading + 180));
        arrowList.add(mark1ArrowInterpolated);
        Arrow mark1ArrowNext = new Arrow(5, 10, finalArrowPosition, (nextHeading + 180));
        arrowList.add(mark1ArrowNext);

        arrowOrderGraph.addEdges(arrowList);
        return arrowList;
    }


    /**
     * Creates the arrow route based on the course and colours it
     */
    private void createArrowedRoute() {
        arrowOrderGraph = new ArrowOrderGraph();

        List<CompoundMark> courseOrder = course.getCourseOrder();
        Arrow prevArrow = null;
        for (int i = 1; i < courseOrder.size(); i ++) {
            CompoundMark prevMark = courseOrder.get(i-1);
            CompoundMark currentMark = courseOrder.get(i);
            List<Arrow> legArrows = addLegArrows(prevMark, currentMark);

            if(prevArrow == null){
                arrowOrderGraph.setStartingArrow(legArrows.get(0));
            } else{
                arrowOrderGraph.addArrowEdge(prevArrow, legArrows.get(0));
            }
            prevArrow = legArrows.get(legArrows.size()-1);
            if (i != courseOrder.size() - 1){
                CompoundMark nextMark = courseOrder.get(i+1);
                List<Arrow> roundingArrows = markRoundingArrows(currentMark, prevMark, nextMark, course.getRoundingOrder().get(i));

                if(roundingArrows.size() != 0){
                    arrowOrderGraph.addArrowEdge(prevArrow, roundingArrows.get(0));
                    if(roundingArrows.size() > 3){
                        arrowOrderGraph.addArrowEdge(prevArrow, roundingArrows.get(3));
                    }
                    prevArrow = roundingArrows.get(2);
                }
            }
        }

        for (Arrow arrow : arrowOrderGraph.getAllArrows()){
            arrow.setColour(ARROW_PATH_COLOR);
        }
    }

    /**
     * Removes the all the arrows in the route from the canvas
     */
    public void removeArrowsFromCanvas(){
        for(Arrow arrow : arrowOrderGraph.getAllArrows()){
            arrow.removeFromCanvas(root);
        }
        arrowsShown = false;
    }

    /**
     * Removes, recreates and redraws all the route arrows on the canvas.
     */
    public void drawRaceRoute(){
        removeArrowsFromCanvas();
        createArrowedRoute();
        for (Arrow arrow : arrowOrderGraph.getAllArrows()){
            arrow.addToCanvas(root);
            arrow.setScale(DisplayUtils.zoomLevel);
            arrow.fade();
        }
        arrowsShown = true;
    }

    public Boolean getArrowsShown() {
        return arrowsShown;
    }

    public void hideArrows() {
        this.arrowsShown = false;
        removeArrowsFromCanvas();
    }

    public void showArrows() {
        this.arrowsShown = true;
        drawRaceRoute();
    }

}
