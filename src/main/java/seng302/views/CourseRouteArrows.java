package seng302.views;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import seng302.models.CompoundMark;
import seng302.models.Coordinate;
import seng302.models.Course;
import seng302.utilities.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to draw the arrows to display the course order
 */
public class CourseRouteArrows {

    private final int REFRESH_THRESHOLD = 6;

    private List<Arrow> arrowRoute;
    private Course course;
    private Group root;
    private Integer refreshTimer;
    private Integer arrowIteration;
    private Boolean arrowsShown = false;

    public CourseRouteArrows(Course course, Group root) {
        this.course = course;
        this.root = root;
        createArrowedRoute();
        refreshTimer = 0;
        arrowIteration = 0;
    }

    /**
     * Every REFRESH_THRESHOLD method calls, this method calls the updateArrowAnimation to the
     * draw the next step in the arrow path.
     */
    public void updateCourseArrows(){
        if (refreshTimer == 0){
            arrowIteration = (arrowIteration + 1) % arrowRoute.size();
            updateArrowAnimation(arrowIteration);
        }
        refreshTimer = (refreshTimer + 1) % REFRESH_THRESHOLD;
    }

    /**
     * Moves the arrow path animation to the next step.
     * @param iteration The iteration of the animation, determines which arrows to emphasize or fade
     */
    private void updateArrowAnimation(Integer iteration){
        int numArrows = arrowRoute.size();
        for(int i = 0; i < numArrows; i++){
            if (i == iteration || (i + 1) % numArrows == iteration || (i + 2) % numArrows == iteration){
                arrowRoute.get(i).emphasize();
            } else{
                arrowRoute.get(i).fade();
            }
        }
    }

    /**
     * Creates and adds arrows to the arrowRoute
     * @param previousMark The starting compound mark for the leg
     * @param currentMark The ending compound mark for the leg
     */
    private void addLegArrows(CompoundMark previousMark, CompoundMark currentMark) {
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
            arrowRoute.add(arrow);
        }
    }

    /**
     * Creates the arrow route based on the course and colours it
     */
    private void createArrowedRoute() {
        arrowRoute = new ArrayList<>();
        List<CompoundMark> courseOrder = course.getCourseOrder();
        for (int i = 1; i < courseOrder.size(); i ++) {
            addLegArrows(courseOrder.get(i - 1), courseOrder.get(i));
        }
        Color color = Color.color(0.25, 0.25, 0.25); //Grey
        double increment = 0.6 / (arrowRoute.size() + 1.0);
        for (Arrow arrow : arrowRoute){
            color = Color.color(0.25, color.getGreen() + increment, 0.25);
            arrow.setColour(color);
        }
    }

    /**
     * Removes the all the arrows in the route from the canvas
     */
    public void removeArrowsFromCanvas(){
        for(Arrow arrow : arrowRoute){
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
        for (Arrow arrow : arrowRoute){
            arrow.addToCanvas(root);
            arrow.setScale(DisplayUtils.zoomLevel);
        }
        arrowsShown = true;
    }

    public Boolean getArrowsShown() {
        return arrowsShown;
    }
}
