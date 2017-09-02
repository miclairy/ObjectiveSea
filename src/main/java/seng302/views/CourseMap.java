package seng302.views;

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
    private String imageLocation;
    private Image image;
    private Integer numberOfMarks;
    private String estTimeToRace;
    private Polygon mapBoundary;
    private Course course;

    public CourseMap(String mapName, String imageLocation, Integer numberOfMarks, String estTimeToRace) {
        this.mapName = mapName;
        this.imageLocation = imageLocation;
        this.image = new Image(imageLocation);
        this.numberOfMarks = numberOfMarks;
        this.estTimeToRace = estTimeToRace;
        setUpBoundary();
        setUpMarks();
    }

    public String getMapName() {
        return mapName;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public Image getImage() {
        return image;
    }

    public Integer getNumberOfMarks() {
        return numberOfMarks;
    }

    public String getEstTimeToRace() {
        return estTimeToRace;
    }

    private void setUpBoundary(){
        RaceVisionXMLParser parser = new RaceVisionXMLParser();
        Course course = parser.importCourse(CourseMap.class.getResourceAsStream("/defaultFiles/" + mapName.replace(" ", "") + "-course.xml"));
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
            circle.setCenterX(circle.getCenterX() + 155);
            circle.setCenterY(circle.getCenterY() + 30);
            circle.setRadius(3f);
            mark.setIcon(circle);
            mark.getIcon().setScaleX(1);
            mark.getIcon().setScaleY(1);
        }
    }

    public Map<Integer, Mark> getMarks(){
        return course.getAllMarks();
    }
}
