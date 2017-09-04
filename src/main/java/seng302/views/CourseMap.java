package seng302.views;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import seng302.data.RaceVisionXMLParser;
import seng302.models.CanvasCoordinate;
import seng302.models.Coordinate;
import seng302.models.Course;
import seng302.models.Mark;
import seng302.utilities.DisplayUtils;

import java.util.ArrayList;
import java.util.Map;

import static seng302.utilities.DisplayUtils.zoomLevel;

public class CourseMap {

    private String mapName;
    private Integer numberOfMarks;
    private String estTimeToRace;
    private Polygon mapBoundary;
    private Course course;
    private CourseRouteArrows arrowedRoute;
    private final int XOFFSET = 155;
    private final int YOFFSET = 30;

    public CourseMap(String mapName, String estTimeToRace) {
        this.mapName = mapName;
        this.numberOfMarks = numberOfMarks;
        this.estTimeToRace = estTimeToRace;
        setUpBoundary();
        setUpMarks();
        this.numberOfMarks = course.getCourseOrder().size();
    }

    public String getMapName() {
        return mapName;
    }

    public Integer getNumberOfMarks() {
        return numberOfMarks;
    }

    public String getEstTimeToRace() {
        return estTimeToRace;
    }

    private void setUpBoundary(){
        RaceVisionXMLParser parser = new RaceVisionXMLParser();
        Course course = parser.importCourse(CourseMap.class.getResourceAsStream("/defaultFiles/" + getXML()));
        course.initCourseLatLon();
        DisplayUtils.setMaxMinLatLon(course.getMinLat(), course.getMinLon(), course.getMaxLat(), course.getMaxLon());
        RaceView raceView = new RaceView();
        this.mapBoundary = raceView.createCourseBoundary(course.getBoundary());
        this.course = course;
    }

    public Course getCourse(){
        return course;
    }

    public Polygon getMapBoundary(){
        return mapBoundary;
    }

    private void setUpMarks(){
        RaceView raceView = new RaceView();
        for (Mark mark : course.getAllMarks().values()) {
            Circle circle = raceView.createMark(mark.getPosition());
            circle.setCenterX(circle.getCenterX() + XOFFSET);
            circle.setCenterY(circle.getCenterY() + YOFFSET);
            circle.setRadius(3f);
            mark.setIcon(circle);
            mark.getIcon().setScaleX(1);
            mark.getIcon().setScaleY(1);
        }
    }

    public void setUpArrowRoute(Parent root){
        this.arrowedRoute = new CourseRouteArrows(course, root);
        arrowedRoute.drawRaceRoute();
    }

    public void removeArrowedRoute(){
        arrowedRoute.removeRaceRoute();
    }

    public Map<Integer, Mark> getMarks(){
        return course.getAllMarks();
    }

    public void updateArrowRoute(){
        arrowedRoute.updateCourseArrows();
    }

    public String getXML(){
        return mapName.replace(" ", "") + "-course.xml";
    }
}
