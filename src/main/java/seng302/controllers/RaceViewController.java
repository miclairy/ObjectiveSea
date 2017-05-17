package seng302.controllers;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Path;

import javafx.scene.input.MouseEvent;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import seng302.data.BoatStatus;
import seng302.utilities.DisplayUtils;
import seng302.utilities.TimeUtils;
import seng302.models.*;
import seng302.views.BoatDisplay;
import seng302.views.RaceView;

import java.util.*;

import static seng302.data.RaceStatus.STARTED;
import static seng302.data.RaceStatus.TERMINATED;

/**
 * Created on 6/03/17.
 * Class to manage the output display.
 */

public class RaceViewController extends AnimationTimer implements Observer {

    private enum AnnotationLevel {
        NO_ANNOTATION, IMPORTANT_ANNOTATIONS, ALL_ANNOTATIONS
    }
    private final double WAKE_SCALE_FACTOR = 17;

    private final double ANNOTATION_OFFSET_X = 10;
    private final double ANNOTATION_OFFSET_Y = 15;
    private Race race;

    private Group root;
    private Controller controller;
    private RaceView raceView;
    private ScoreBoardController scoreBoardController;
    private ArrayList<BoatDisplay> displayBoats = new ArrayList<>();
    private double previousTime = 0;
    private Polygon boundary;
    private double currentTimeInSeconds;
    private AnnotationLevel currentAnnotationsLevel;
    private boolean courseNeedsRedraw = false;
    private boolean initializedBoats = false;
    private ImageCursor boatCursor = new ImageCursor(new Image("graphics/boat-select-cursor.png"), 7, 7);

    private BoatDisplay selectedBoat = null;
    private Mark selectedMark = null;
    private boolean isTrackingPoint = false;
    private double rotationOffset = 0;
    private boolean isRotationEnabled = false;


    public RaceViewController(Group root, Race race, Controller controller, ScoreBoardController scoreBoardController) {
        this.root = root;
        this.race = race;
        this.controller = controller;
        this.raceView = new RaceView();
        this.scoreBoardController = scoreBoardController;
        drawCourse();
        addDeselectEvents();
    }

    @Override
    public void handle(long currentTime) {
        if (previousTime == 0) {
            previousTime = currentTime;
            return;
        }

        double secondsElapsed = TimeUtils.convertNanosecondsToSeconds(currentTime - previousTime);

        if(!race.isTerminated()){
            controller.updateRaceClock();
        }
        if(controller.hasRaceStatusChanged()){
            controller.updatePreRaceScreen();
            controller.setRaceStatusChanged(false);
        }
        currentTimeInSeconds += secondsElapsed;
        controller.setTimeZone(race.getUTCOffset());
        controller.updateFPSCounter(currentTime);
        run();
        previousTime = currentTime;
    }

    /**
     * Body of main loop of animation
     */
    private void run(){
        if (isTrackingPoint && selectedMark != null){
            DisplayUtils.moveToPoint(selectedMark.getPosition());
            redrawCourse();
        }
        if (isTrackingPoint && selectedBoat != null) {
            selectedBoat.getIcon().toFront();
            DisplayUtils.moveToPoint(selectedBoat.getBoat().getCurrentPosition());
            redrawCourse();
            if(isRotationEnabled){
                if(DisplayUtils.zoomLevel > 1){
                    rotationOffset = -selectedBoat.getBoat().getHeading();
                    updateRotation();
                }
            }
        }
        for (BoatDisplay boat: displayBoats) {
            CanvasCoordinate point = DisplayUtils.convertFromLatLon(boat.getBoat().getCurrentLat(), boat.getBoat().getCurrentLon());
            moveBoat(boat, point);
            moveWake(boat, point);
            if(race.getRaceStatus() == STARTED) {
                addToBoatPath(boat, point);
            }
            moveBoatAnnotation(boat.getAnnotation(), point);
        }
        if (courseNeedsRedraw) redrawCourse();
        changeAnnotations(currentAnnotationsLevel, true);
        controller.updatePlacings();
        controller.setWindDirection();

    }

    /**
     * Draws and sets up BoatDisplay objects onscreen
     */
    public void initializeBoats() {
        for (Boat boat : race.getCompetitors()){
            BoatDisplay displayBoat = new BoatDisplay(boat);
            raceView.assignColor(displayBoat);
            displayBoats.add(displayBoat);
            drawBoat(displayBoat);
            addBoatSelectionHandler(displayBoat);
            scoreBoardController.addBoatToSparkLine(boat.getSeries());
        }
        initializedBoats = true;
        changeAnnotations(currentAnnotationsLevel, true);
    }

    /**
     * adds event hadnlers so we can detect if the user has selected a boat
     * @param boat
     */
    private void addBoatSelectionHandler(BoatDisplay boat){
        Shape boatImage = boat.getIcon();
        boatImage.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            selectedBoat = boat;
            selectedMark = null;
            setBoatFocus();
        });

        boatImage.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            root.setCursor(boatCursor);
        });

        boatImage.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            root.setCursor(Cursor.DEFAULT);
        });
    }

    /**
     * Gets a drawing of a boat icon and sets it up onscreen
     * @param boat the BoatDisplay object that is to be drawn
     */
    private void drawBoat(BoatDisplay boat){
        Polyline boatImage = raceView.createBoatImage(boat.getColor());
        root.getChildren().add(boatImage);
        boat.setIcon(boatImage);
        drawBoatWake(boat);
    }

    /**
     * inits drawing of the boat paths
     */
    public void initBoatPaths(){
        for (BoatDisplay boat : displayBoats){
            initBoatPath(boat);
        }
    }

    /**
     * Initalises a Path for a boat
     * @param boatDisplay the boatDisplay to create a path for
     */
    public void initBoatPath(BoatDisplay boatDisplay){
        Path path = raceView.createBoatPath(boatDisplay.getColor());
        Boat boat = boatDisplay.getBoat();
        CanvasCoordinate point = DisplayUtils.convertFromLatLon(boat.getCurrentLat(), boat.getCurrentLon());
        path.getElements().add(new MoveTo(point.getX(), point.getY()));
        boatDisplay.setPath(path);
        root.getChildren().add(path);
    }

    /**
     * Handles getting the drawings for and setting up of these course images onscreen.
     */
    private void drawCourse(){
        drawBoundary();
        drawMarks();
        drawRaceLines();
        drawMap();
    }

    /**
     * adds Event handlers to areas of the course than don't contain boat, so deselect of boat
     * can be detected
     */
    private void addDeselectEvents(){
        boundary.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            deselectEventAction();
            scoreBoardController.btnTrack.setVisible(false);

        });

        controller.mapImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            deselectEventAction();
            scoreBoardController.btnTrack.setVisible(false);

        });
    }

    /**
     * the action of the event handler to areas of the course than don't contain boat, so deselect of boat
     * can be detected
     */
    private void deselectEventAction(){
        for(BoatDisplay boat : displayBoats){
            boat.focus();
            scoreBoardController.btnTrack.setVisible(false);
            selectedBoat = null;
            selectedMark = null;
            isTrackingPoint = false;
            rotationOffset =0;
            updateRotation();


        if(DisplayUtils.zoomLevel == 1){
                setMapVisibility(true);
                DisplayUtils.resetOffsets();
                redrawCourse();

            }
            boat.getBoat().getSeries().getNode().setOpacity(1);
        }
    }

    /**
     * Draws both the start end and the finish line
     */
    private void drawRaceLines() {
        drawRaceLine(race.getCourse().getStartLine());
        drawRaceLine(race.getCourse().getFinishLine());
    }

    /**
     * Handles drawing of all of the marks from the course
     */
    public void drawMarks() {
        for (Mark mark : race.getCourse().getAllMarks().values()) {
            Circle circle = raceView.createMark(mark.getPosition());
            root.getChildren().add(circle);
            mark.setIcon(circle);
            addMarkSelectionHandlers(mark);
        }
    }

    /**
     * adds event handler to marks so we can detet if selected by the user
     * @param mark
     */
    private void addMarkSelectionHandlers(Mark mark){

        Circle circle = mark.getIcon();

        circle.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {

            rotationOffset = 0;
            updateRotation();

            if(mark != selectedMark){
                controller.setZoomSliderValue(3);
                DisplayUtils.moveToPoint(mark.getPosition());
                selectedMark = mark;
                isTrackingPoint = true;
                selectedBoat = null;
                redrawCourse();
                setMapVisibility(false);
            }else{
                controller.setZoomSliderValue(1);
                selectedMark = null;
                isTrackingPoint = false;

                DisplayUtils.resetOffsets();
                redrawCourse();
                setMapVisibility(true);

            }
        });

        circle.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            root.setCursor(boatCursor);
        });

        circle.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            root.setCursor(Cursor.DEFAULT);
        });
    }

    /**
     * Creates a Line object based on two ends of a RaceLine
     * @param raceLine The RaceLine object for the line to be drawn for
     */
    public void drawRaceLine(RaceLine raceLine){
        Line line = raceView.createRaceLine(raceLine.getMark1().getPosition(), raceLine.getMark2().getPosition());
        raceLine.setLine(line);
    }

    /**
     * Handles drawing the boundary and adds styling from the course array of co-ordinates
     */
    private void drawBoundary(){
        boundary = raceView.createCourseBoundary(race.getCourse().getBoundary());
        root.getChildren().add(boundary);
        boundary.toBack();
    }

    /**
     * handles the drawing of the map image and makes it fullscreen
     */
    public void drawMap(){
        String mapURL = DisplayUtils.getGoogleMapsURL();
        Image image = new Image(mapURL);
        controller.mapImageView.setImage(image);
        controller.mapImageView.toBack();
        resizeMap();
    }

    /**
     * resizes the map to make it fit the bounds of the display
     */
    private void resizeMap(){
        double height = Controller.getAnchorHeight();
        double width = Controller.getAnchorWidth();
        controller.mapImageView.setFitWidth(width);
        controller.mapImageView.setFitHeight(height);
    }

    /**
     * Adds a text annotation to a boat
     * @param displayBoat the boat to draw the annotation to
     * @param annotationText the text to put in the annotation
     */
    private void drawBoatAnnotation(BoatDisplay displayBoat, ArrayList<String> annotationText){
        CanvasCoordinate point = DisplayUtils.convertFromLatLon(displayBoat.getBoat().getCurrentPosition());
        VBox annotationFrame = displayBoat.getAnnotation();
        annotationFrame.getChildren().clear();

        for(String string : annotationText){
            Label annotationLabel = new Label(string);
            annotationLabel.setId("annotationLabel");
            annotationFrame.getChildren().add(annotationLabel);
        }
        CanvasCoordinate annoPos = getAnnotationPosition(annotationFrame);
        annotationFrame.layoutXProperty().set(point.getX() + annoPos.getX());
        annotationFrame.layoutYProperty().set(point.getY() + annoPos.getY());

        Line annoLine = new Line(point.getX(), point.getY(), annotationFrame.getLayoutX(), annotationFrame.getLayoutY());
        annoLine.setId("annotationLine");
        root.getChildren().remove(displayBoat.getAnnotationLine());
        displayBoat.setAnnotationLine(annoLine);

        root.getChildren().add(annotationFrame);
        root.getChildren().add(annoLine);

        displayBoat.setAnnotation(annotationFrame);
        displayBoat.getAnnotation().toFront();
        displayBoat.getAnnotationLine().toBack();
    }

    /** Calculates best position for annotation to be displayed based on other items on the screen
     * @param currAnno
     * @return a CanvasCoordinate of the best screen position
     */
    private CanvasCoordinate getAnnotationPosition(VBox currAnno){
        double offsetX = ANNOTATION_OFFSET_X * DisplayUtils.zoomLevel;
        double offsetY = ANNOTATION_OFFSET_Y * DisplayUtils.zoomLevel;
        CanvasCoordinate coord = new CanvasCoordinate(offsetX, offsetY);
        return coord;
    }

    /**
     * Move's the annotation to where the boat is now.
     * @param annotation the annotation to be moved
     * @param point where the boat has moved to
     */
    private void moveBoatAnnotation(VBox annotation, CanvasCoordinate point){
        double adjustX = 10;
        annotation.relocate(
                (point.getX() + ANNOTATION_OFFSET_X),
                (point.getY() + ANNOTATION_OFFSET_Y)
        );
        annotation.setRotate(-rotationOffset);
        if(DisplayUtils.checkBounds(annotation)){
            adjustX -= annotation.getBoundsInParent().getWidth();
            annotation.relocate(
                    (point.getX() + adjustX),
                    (point.getY() + ANNOTATION_OFFSET_Y)
            );
        }
    }

    /**
     * When the slider gets to either 0, 1 or 2 change the annotations to Off, Important and Full respectively.
     * Don't make more annotations if there are already annotations.
     * @param level the annotation level
     * @param forceRedisplay forces the annotations to be redisplayed even if the level hasn't changed
     */
    public void changeAnnotations(AnnotationLevel level, boolean forceRedisplay) {
        long currTime = race.getCurrentTimeInEpochMs();
        ArrayList<String> annotations = new ArrayList<>();
        if(forceRedisplay || level != currentAnnotationsLevel) {
            for (BoatDisplay displayBoat : displayBoats) {
                String boatName = displayBoat.getBoat().getNickName();
                VBox oldAnnotation = displayBoat.getAnnotation();
                if (oldAnnotation != null) {
                    root.getChildren().remove(oldAnnotation);
                    root.getChildren().remove(displayBoat.getAnnotationLine());
                }
                if (level == AnnotationLevel.IMPORTANT_ANNOTATIONS) {
                    annotations.clear();
                    if(scoreBoardController.isNameSelected()){
                        annotations.add(boatName);
                    }
                    if(scoreBoardController.isSpeedSelected()){
                        annotations.add(displayBoat.getSpeed());
                    }
                    if(scoreBoardController.isTimePassedSelected()){
                        annotations.add(displayBoat.getTimeSinceLastMark(currTime));
                    }
                    if(scoreBoardController.isEstSelected()){
                        annotations.add(displayBoat.getTimeToNextMark(displayBoat.getBoat().getTimeAtNextMark(), currTime));
                    }
                    drawBoatAnnotation(displayBoat, annotations);
                } else if (level == AnnotationLevel.ALL_ANNOTATIONS) {
                    annotations.clear();
                    annotations.add(boatName);
                    annotations.add(displayBoat.getSpeed());
                    annotations.add(displayBoat.getTimeSinceLastMark(currTime));
                    annotations.add(displayBoat.getTimeToNextMark(displayBoat.getBoat().getTimeAtNextMark(), currTime));
                    drawBoatAnnotation(displayBoat, annotations);
                }
            }
            currentAnnotationsLevel = level;
        }
    }

    /**
     * Overload for changeAnnotations() which converts a raw level value into an AnnotationLevel
     * @param level the annotation level
     * @param forceRedisplay forces the annotations to be redisplayed even if the level hasn't changed
     */
    public void changeAnnotations(int level, boolean forceRedisplay) {
        changeAnnotations(AnnotationLevel.values()[level], forceRedisplay);
    }

    /**
     * Draws initial boat wake which is a V shaped polyline. Which is then coloured and attached to the boat.
     * @param boat to attach the wake to.
     */
    private void drawBoatWake(BoatDisplay boat){
        Polyline wake = raceView.createBoatWake();
        root.getChildren().add(wake);
        boat.setWake(wake);
    }

    /**
     * Update a boat icon's position on screen, translating to the input point
     * @param boat the BoatDisplay to be moved
     * @param point where the boat display should be moved to
     */
    private void moveBoat(BoatDisplay boat, CanvasCoordinate point){
        Shape icon = boat.getIcon();
        icon.setTranslateY(point.getY());
        icon.setTranslateX(point.getX());
        icon.setScaleX(DisplayUtils.zoomLevel);
        icon.setScaleY(DisplayUtils.zoomLevel);
        icon.getTransforms().clear();
        icon.getTransforms().add(new Rotate(boat.getBoat().getHeading()));
        icon.toFront();
    }


    /**
     * Moves the wake of a boat to the correct position
     * @param boat the boatDisplay who's wake should move
     * @param point where the boat is now positioned
     */
    private void moveWake(BoatDisplay boat, CanvasCoordinate point){

        Shape wake = boat.getWake();
        double scale = boat.getBoat().getSpeed() / WAKE_SCALE_FACTOR;
        scale *= DisplayUtils.zoomLevel;
        wake.getTransforms().clear();
        wake.getTransforms().add(new Scale(scale, scale,0, 0));
        wake.setTranslateY(point.getY());
        wake.setTranslateX(point.getX());
        wake.getTransforms().add(new Rotate(boat.getBoat().getHeading(), 0, 0));
    }

    /**
     * Handles redrawing of the course at the correct scale and position after a window resize
     */
    public void redrawCourse(){
        courseNeedsRedraw = false;
        redrawMarks();
        redrawBoundary();
        resizeMap();
        redrawRaceLines();
        redrawBoatPaths();
    }

    /**
     * Updates the positions of both the start and finish lines
     */
    private void redrawRaceLines() {
        redrawRaceLine(race.getCourse().getStartLine());
        redrawRaceLine(race.getCourse().getFinishLine());
    }

    /**
     * Redraws a raceline on the visuial
     * @param raceLine
     */
    private void redrawRaceLine(RaceLine raceLine) {
        root.getChildren().remove(raceLine.getLine());
        Line line = raceView.createRaceLine(raceLine.getMark1().getPosition(), raceLine.getMark2().getPosition());
        root.getChildren().add(line);
        raceLine.setLine(line);
        raceLine.getLine().toBack();
    }

    /**
     * Handles moving of all marks, including redrawing race lines
     */
    private void redrawMarks(){
        for (Mark mark : race.getCourse().getAllMarks().values()){
            CanvasCoordinate convertedPoint = DisplayUtils.convertFromLatLon(mark.getPosition());
            Circle icon = mark.getIcon();
            icon.toFront();
            icon.setScaleX(DisplayUtils.zoomLevel);
            icon.setScaleY(DisplayUtils.zoomLevel);
            icon.setCenterX(convertedPoint.getX());
            icon.setCenterY(convertedPoint.getY());
            icon.toBack();
        }
    }

    /**
     * Redraws the boundary at the correct scale and position after a window resize
     */
    private void redrawBoundary(){
        ObservableList<Double> points = boundary.getPoints();
        points.clear();
        for(Coordinate coord : race.getCourse().getBoundary()){
            CanvasCoordinate point = DisplayUtils.convertFromLatLon(coord);
            points.add(point.getX());
            points.add(point.getY());
        }
        boundary.toBack();
    }

    /**
     * Redraws all the boat paths by reconverting all the coordinates a boat has been to and recreates the
     * elements (points) of the path of the boat.
     */
    public void redrawBoatPaths(){
        for(BoatDisplay boatDisplay : displayBoats){
            Path path = boatDisplay.getPath();
            Boat boat = boatDisplay.getBoat();
            if(race.getRaceStatus() == STARTED || race.getRaceStatus() == TERMINATED) {
                if (!boat.getPathCoords().isEmpty() && path != null) {
                    path.getElements().clear();
                    CanvasCoordinate pathStart = DisplayUtils.convertFromLatLon(boat.getPathCoords().get(0));
                    path.getElements().add(new MoveTo(pathStart.getX(), pathStart.getY()));
                    for (Coordinate coord : boat.getPathCoords()) {
                        CanvasCoordinate currPoint = DisplayUtils.convertFromLatLon(coord);
                        path.getElements().add(new LineTo(currPoint.getX(), currPoint.getY()));
                    }
                    path.toBack();
                    path.setStrokeWidth(DisplayUtils.zoomLevel);
                }
            }
        }
    }

    /**
     * Adds a point to the boat path
     * @param boatDisplay The display component of the boat
     * @param point The position of the boat on screen
     */
    public void addToBoatPath(BoatDisplay boatDisplay, CanvasCoordinate point){
        if(boatDisplay.getPath() != null && boatDisplay.getBoat().getStatus() != BoatStatus.FINISHED){
            boatDisplay.getPath().getElements().add(new LineTo(point.getX(), point.getY()));
            boatDisplay.getPath().toBack();
        }
    }

    private void setBoatFocus(){
        if(selectedBoat != null) {
            scoreBoardController.btnTrack.setVisible(true);
            selectedBoat.getIcon().toFront();
            for(BoatDisplay boat : displayBoats){
                if(!boat.equals(selectedBoat)){
                    boat.unFocus();
                    boat.getBoat().getSeries().getNode().setOpacity(0.2);
                }else{
                    boat.focus();
                    boat.getBoat().getSeries().getNode().setOpacity(1);
                }
            }
        }
    }


    /**
     * This is currently called when the Course gets updated, and will redraw the course to reflect these changes
     * @param course
     */
    @Override
    public void update(Observable course, Object arg) {
        if (course == race.getCourse()){
            courseNeedsRedraw = true;
        }
    }

    /**
     * updates the rotation of the canvas to the rotationOffset class variable
     */
    private void updateRotation(){
        root.getTransforms().clear();
        root.getTransforms().add(new Rotate(rotationOffset, controller.getCanvasWidth()/2, controller.getCanvasHeight()/2));
    }

    @FXML
    /**
     *  Going to be used to toggle the zoom level of the map (currently only two levels will exist, on or off).
     */
    public void zoomToggle(boolean zoomed){
        isRotationEnabled = zoomed;
        rotationOffset = 0;
    }

    public boolean hasInitializedBoats() {
        return initializedBoats;
    }


    public BoatDisplay getSelectedBoat() {
        return selectedBoat;
    }


    public boolean isTrackingPoint() {
        return isTrackingPoint;
    }

    public void setTrackingPoint(boolean trackingPoint) {
        this.isTrackingPoint = trackingPoint;
    }

    public void setMapVisibility(boolean visible){
        controller.mapImageView.setVisible(visible);
    }

    public double getRotationOffset() {
        return rotationOffset;
    }

    public void setRotationOffset(double rotationOffset) {
        this.rotationOffset = rotationOffset;
    }

}

