package seng302;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

import java.util.*;

/**
 * Created on 6/03/17.
 * Class to manage the output display.
 */

public class Display extends AnimationTimer {

    private Race race;
    private Group root;
    private double previousTime = 0;
    private ImageView currentWindArrow;
    private final ArrayList<Color> COLORS = new ArrayList<>((Arrays.asList(Color.WHITE, Color.web("#A0D468"), Color.web("#FC6E51"),
            Color.web("#FFCE54"), Color.web("#48CFAD"), Color.web("#4FC1E9"), Color.web("#656D78"))));
    private Polygon boundary;
    private double annotationsLevel;
    private final double WAKE_SCALE_FACTOR = 50;

    public Display(Group root, Race race) {
        this.root = root;
        this.race = race;
        race.setEvents();
        drawCourse();
        drawBoats();
    }

    @Override
    public void handle(long currentTime) {
        if (previousTime == 0) {
            previousTime = currentTime;
            return;
        }
        double secondsElapsed = (currentTime - previousTime) / 1e9f;
        previousTime = currentTime;
        run(secondsElapsed);
        Controller.updateFPSCounter(currentTime);
    }

    /**
     * Body of main loop of animation
     * @param timeIncrement
     */
    private void run(double timeIncrement){
            for (Boat boat : race.getCompetitors()){
                boat.updateLocation(timeIncrement, race.getCourse());
            }
            moveBoats();
            Controller.updatePlacings();
    }


    private void drawCourse(){
        drawBoundary();
        drawMarks();
    }


    /**
     * Draws all of the marks from the course
     */
    private void drawMarks(){
        for(CompoundMark mark : race.getCourse().getMarks().values()){
            if(mark instanceof Gate){
                Gate gate  = (Gate) mark;
                ArrayList<CartesianPoint> points = new ArrayList<>();
                points.add(DisplayUtils.convertFromLatLon(gate.getEnd1Lat(), gate.getEnd1Lon()));
                points.add(DisplayUtils.convertFromLatLon(gate.getEnd2Lat(), gate.getEnd2Lon()));
                if(gate.isStart() || gate.isFinish()){
                    Line line = new Line(points.get(0).getX(),points.get(0).getY(), points.get(1).getX(), points.get(1).getY());
                    line.setStroke(Color.web("#70aaa2"));
                    root.getChildren().add(line);
                    gate.setLine(line);
                }
                for(CartesianPoint point : points){
                    Circle circle = new Circle(point.getX(), point.getY(), 4f);
                    circle.setId("mark");
                    root.getChildren().add(circle);
                    gate.addIcon(circle);
                }
            }else{
                CartesianPoint point = DisplayUtils.convertFromLatLon(mark.getLat(), mark.getLon());
                Circle circle = new Circle(point.getX(), point.getY(), 4f);
                circle.setId("mark");
                root.getChildren().add(circle);
                mark.addIcon(circle);
            }
        }
        drawWindArrow();
    }

    /**
     * Draws the boundary and adds styling from the course array of co-ordinates
     */
    private void drawBoundary(){
        boundary = new Polygon();
        boundary.setId("boundary");
        for(Coordinate coord : race.getCourse().getBoundary()){
            CartesianPoint point = DisplayUtils.convertFromLatLon(coord.getLat(), coord.getLon());
            boundary.getPoints().add(point.getX());
            boundary.getPoints().add(point.getY());
        }
        root.getChildren().add(boundary);
        boundary.toBack();
    }

    /**
     * Draws the wind direction arrow from the course on the canvas.
     */
    private void drawWindArrow(){
        double windDirection = race.getCourse().getWindDirection();
        ImageView imv = new ImageView();
        Image windArrow = new Image("graphics/arrow.png");
        imv.setImage(windArrow);
        imv.setFitHeight(40);
        imv.setFitWidth(40);
        imv.setX(Controller.getCanvasSize().getX() - 60);
        imv.setY(15);
        imv.setRotate(windDirection);
        root.getChildren().add(imv);
        currentWindArrow = imv;
    }


    /**
     * Draws the boat icons (triangles with lines in the middle) and fills them with colour
     */
    private void drawBoats(){
        int i = 1;
        for (Boat boat : race.getCompetitors()) {
            Polyline boatImage = new Polyline();
            boatImage.getPoints().addAll(new Double[]{
                    5.0, 0.0,
                    10.0, 20.0,
                    0.0 , 20.0,
                    5.0, 0.0,
                    5.0, 20.0
                    }
            );
            boatImage.setFill(COLORS.get(i));
            boatImage.setStroke(Color.WHITE);
            root.getChildren().add(boatImage);
            boat.setIcon(boatImage);
            i++;
            drawBoatWake(boat);
        }
    }

    /**
     * Update each boat icon's position on screen, translating from the boat's latlon to cartesian coordinates
     */
    private void moveBoats(){
        for (Boat boat : race.getCompetitors()) {
            CartesianPoint point = DisplayUtils.convertFromLatLon(boat.getCurrentLat(), boat.getCurrentLon());
            boat.getIcon().setTranslateY(point.getY());
            boat.getIcon().setTranslateX(point.getX());
            boat.getIcon().getTransforms().clear();
            boat.getIcon().getTransforms().add(new Rotate(boat.getHeading(), 5.0, 0.0));
            if (annotationsLevel > 0) {
                moveBoatAnnotations(boat);
            }
            moveWake(boat, point);
            boat.getIcon().toFront();
        }
    }

    /**
     * Adds an text annotation to each boat offset by 10, 15.
     */
    private void drawBoatAnnotations(){
        for(Boat boat : race.getCompetitors()){
            String annotationText = boat.getNickName().toString() + ", " + boat.getSpeed() + "kn";
            CartesianPoint point = DisplayUtils.convertFromLatLon(boat.getCurrentLat(), boat.getCurrentLon());
            Text annotation = new Text();
            annotation.setText(annotationText);
            annotation.setId("annotation");
            annotation.setX(point.getX() + 10);
            annotation.setY(point.getY() + 15);
            boat.setAnnotation(annotation);
            root.getChildren().add(annotation);
        }
    }

    /**
     * Draws initial boat wake which is a V shaped polyline. Which is then coloured and attached to the boat.
     * @param boat to attach the wake to.
     */
    private void drawBoatWake(Boat boat){

        Polyline wake = new Polyline();
        wake.getPoints().addAll(new Double[]{
                0.0 , 50.0,
                5.0, 0.0,
                10.0, 50.0
                });

        root.getChildren().add(wake);
        boat.setWake(wake);
        wake.setId("wake");
    }

    /**
     * Move's the annotation to where the boat is now.
     * @param boat
     */
    private void moveBoatAnnotations(Boat boat){
        double adjustX = 10;
        CartesianPoint point = DisplayUtils.convertFromLatLon(boat.getCurrentLat(), boat.getCurrentLon());
        boat.getAnnotation().relocate((point.getX() + 10), point.getY() + 15);
        if(DisplayUtils.checkBounds(boat.getAnnotation())){
            adjustX -= boat.getAnnotation().getBoundsInParent().getWidth();
            boat.getAnnotation().relocate((point.getX() + adjustX), point.getY() + 15);
        }
    }

    private void moveWake(Boat boat, CartesianPoint point){
        boat.getWake().getTransforms().clear();
        double scale = boat.getSpeed() / WAKE_SCALE_FACTOR;
        boat.getWake().getTransforms().add(new Scale(scale, scale,5, 0));
        boat.getWake().setTranslateY(point.getY());
        boat.getWake().setTranslateX(point.getX());
        boat.getWake().getTransforms().add(new Rotate(boat.getHeading(), 5, 0));
    }

    public void redrawCourse(){
        redrawBoundary();
        for (CompoundMark mark : race.getCourse().getMarks().values()){
            CartesianPoint point = DisplayUtils.convertFromLatLon(mark.getLat(), mark.getLon());

            if (mark instanceof Gate){
                Gate gate = (Gate) mark;
                ArrayList<CartesianPoint> points = new ArrayList<>();
                points.add(DisplayUtils.convertFromLatLon(gate.getEnd1Lat(), gate.getEnd1Lon()));
                points.add(DisplayUtils.convertFromLatLon(gate.getEnd2Lat(), gate.getEnd2Lon()));
                if (gate.getLine() != null) {
                    root.getChildren().remove(gate.getLine());
                    Line line = new Line(points.get(0).getX(), points.get(0).getY(), points.get(1).getX(), points.get(1).getY());
                    line.setStroke(Color.web("#70aaa2"));
                    root.getChildren().add(line);
                    gate.setLine(line);
                }
                for (int i = 0; i < mark.getIcons().size(); i++) {
                    mark.getIcons().get(i).toFront();
                    mark.getIcons().get(i).setCenterX(points.get(i).getX());
                    mark.getIcons().get(i).setCenterY(points.get(i).getY());
                }
            } else {
                for (Circle icon : mark.getIcons()) {
                    icon.setCenterX(point.getX());
                    icon.setCenterY(point.getY());
                }
            }
        }

    }

    private void redrawBoundary(){
        boundary.getPoints().clear();
        for(Coordinate coord : race.getCourse().getBoundary()){
            CartesianPoint point = DisplayUtils.convertFromLatLon(coord.getLat(), coord.getLon());
            boundary.getPoints().add(point.getX());
            boundary.getPoints().add(point.getY());
        }

    }

    /**
     * Moves compass arrow to correct position when canvas is resized.
     */
    public void redrawWindArrow() {
        currentWindArrow.setX(Controller.getCanvasSize().getX() - 60);
    }

    /**
     * When the slider gets to either 0 or 1 change the annotations to be on or off. Don't make more annotations if
     * there are already annotations.
     * @param level
     */
    public void changeAnnotations(double level) {

        if (level == 0) {
            for (Boat boat : race.getCompetitors()) {
                root.getChildren().remove(boat.getAnnotation());
            }
            annotationsLevel = 0;
        } else if (level == 1 && annotationsLevel != 1){
            drawBoatAnnotations();
            annotationsLevel = 1.0;
        }
    }
}

