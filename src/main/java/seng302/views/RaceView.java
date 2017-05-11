package seng302.views;

import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import seng302.models.*;
import seng302.utilities.DisplayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mjt169 on 30/03/17.
 * Views for the live race view
 */
public class RaceView {

    private int nextColorToBeUsed = 1;

    private final ArrayList<Color> COLORS = new ArrayList<>((Arrays.asList(Color.WHITE, Color.web("#A0D468"), Color.web("#FC6E51"),
            Color.web("#FFCE54"), Color.web("#48CFAD"), Color.web("#4FC1E9"), Color.web("#656D78"))));

    /**
     * Creates a boat image, which is a triangle with a line through the middle, parallel to the direction the image
     * is facing
     * @param color the color to draw the boat
     * @return a rendered boat image
     */
    public Polyline createBoatImage(Color color){
        Polyline boatImage = new Polyline();
        boatImage.getPoints().addAll(0.0, -10.0,
                5.0, 10.0,
                -5.0, 10.0,
                0.0, -10.0,
                0.0, 10.0);
        boatImage.setFill(color);
        boatImage.setStroke(Color.WHITE);
        return boatImage;
    }

    /**
     * Creates a boat wake image, which is a triangular shape with highlights
     * @return a rendered boat wake image
     */
    public Polyline createBoatWake() {
        Polyline wake = new Polyline();
        wake.getPoints().addAll(-5.0, 40.0,
                0.0, -10.0,
                5.0, 40.0);
        wake.setId("wake");
        return wake;
    }

    /**
     * Creates a Path to be used to draw a trail behind a boat
     * @param color to make the path
     * @return a new Path
     */
    public Path createBoatPath(Color color) {
        Path path = new Path();
        path.getStrokeDashArray().addAll(5.0,7.0,5.0,7.0);
        path.setId("boatPath");
        path.setOpacity(1);
        path.setStroke(color);
        return path;
    }

    /**
     * Creates a Polygon representing the boundaries of a course
     * @param boundaryCoordinates the coordinates describing the boundary
     * @return a boundary shaped Polygon
     */
    public Polygon createCourseBoundary(List<Coordinate> boundaryCoordinates){
        Polygon boundary = new Polygon();
        boundary.setId("boundary");
        for(Coordinate coord : boundaryCoordinates){
            CanvasCoordinate point = DisplayUtils.convertFromLatLon(coord);
            boundary.getPoints().add(point.getX());
            boundary.getPoints().add(point.getY());
        }
        return boundary;
    }

    /**
     * Creates a Circle representing a mark
     * @param coordinate where the mark is located
     * @return a Circle object representing the mark image
     */
    public Circle createMark(Coordinate coordinate) {
        CanvasCoordinate point = DisplayUtils.convertFromLatLon(coordinate);
        Circle circle = new Circle(point.getX(), point.getY(), 4f);
        circle.setId("mark");
        return circle;
    }

    public Line createRaceLine(Coordinate end1, Coordinate end2) {
        CanvasCoordinate convertedEnd1 = DisplayUtils.convertFromLatLon(end1);
        CanvasCoordinate convertedEnd2 = DisplayUtils.convertFromLatLon(end2);
        Line line = new Line(
                convertedEnd1.getX(), convertedEnd1.getY(),
                convertedEnd2.getX(), convertedEnd2.getY()
        );
        line.setStroke(Color.web("#70aaa2"));
        return line;
    }

    /**
     * Creates a vector extending from the boat with length proportional to it's speed over ground
     * @param BoatsPosition the current position of the boat
     * @param lengthOfVector the length of the vector (in terms of Nautical Miles)
     * @param boatBearing the bearing of the boat
     * @return a line extending from the boat
     */
    public Line createSOGVector(Coordinate BoatsPosition, double lengthOfVector, double boatBearing, Color color){
        Coordinate end2 = BoatsPosition.coordAt(lengthOfVector, boatBearing);
        CanvasCoordinate convertedEnd1 = DisplayUtils.convertFromLatLon(BoatsPosition);
        CanvasCoordinate convertedEnd2 = DisplayUtils.convertFromLatLon(end2);
        Line line = new Line(
                convertedEnd1.getX(), convertedEnd1.getY(),
                convertedEnd2.getX(), convertedEnd2.getY()
        );
        line.setStroke(color);
        return line;
    }

    public Line createVMGVector(Coordinate BoatsPosition, double lengthOfVector, Color color, Boat boat, Course course){
        int lastRoundedMarkIndex = boat.getLastRoundedMarkIndex();
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        Coordinate markLocation;
        if(lastRoundedMarkIndex + 1 < courseOrder.size()){
        markLocation = courseOrder.get(lastRoundedMarkIndex + 1).getPosition();
        } else {
            markLocation = courseOrder.get(lastRoundedMarkIndex).getPosition();
        }
        double lineBearing = BoatsPosition.headingToCoordinate(markLocation);
        Coordinate end2 = BoatsPosition.coordAt(lengthOfVector, lineBearing);
        CanvasCoordinate convertedEnd1 = DisplayUtils.convertFromLatLon(BoatsPosition);
        CanvasCoordinate convertedEnd2 = DisplayUtils.convertFromLatLon(end2);
        Line line = new Line(
                convertedEnd1.getX(), convertedEnd1.getY(),
                convertedEnd2.getX(), convertedEnd2.getY()
        );
        line.setStroke(color);
        return line;
    }

    /**
     * Assigns a color to a BoatDisplay to be used when drawing things for that boat
     * @param boat the boat to assign a color to
     */
    public void assignColor(BoatDisplay boat) {
        boat.setColor(COLORS.get(nextColorToBeUsed));
        nextColorToBeUsed++;
        nextColorToBeUsed %= COLORS.size();
    }
}
