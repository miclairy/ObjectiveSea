package seng302.controllers;

import javafx.animation.AnimationTimer;
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
import javafx.util.Pair;
import seng302.data.BoatStatus;
import seng302.utilities.DisplayUtils;
import seng302.utilities.MathUtils;
import seng302.utilities.TimeUtils;
import seng302.models.*;
import seng302.views.BoatDisplay;
import seng302.views.RaceView;

import java.util.*;

import static seng302.data.RaceStatus.STARTED;

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
    private ImageView currentWindArrow;
    private Polygon boundary;
    private double currentTimeInSeconds;
    private AnnotationLevel currentAnnotationsLevel;
    //number of pixels from right edge of canvas that the wind arrow will be drawn
    private final int WIND_ARROW_OFFSET = 60;
    private boolean courseNeedsRedraw = false;
    private boolean initializedBoats = false;
    private BoatDisplay selectedBoat;
    private ImageCursor cursor = new ImageCursor(new Image("graphics/boat-select-cursor.png"), 7, 7);

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
            setBoatFocus();
        });

        boatImage.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            root.setCursor(cursor);
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
     * adds Event handlers to areas of the course than don't contain boat, so deselct of boat
     * can be detetced
     */
    private void addDeselectEvents(){
        boundary.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            deselectBoat();
        });

        controller.mapImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            deselectBoat();
        });
    }

    private void deselectBoat() {
        for(BoatDisplay boat : displayBoats){
            boat.focus();
            scoreBoardController.btnTrack.setVisible(false);
            scoreBoardController.chkMultipleSelect.setVisible(false);
            boat.removeLaylines(root);
            boat.removeBoatLaylines(root);
            selectedBoat = null;
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
            addMarkSelectionHandlers(circle);
        }
    }

    /**
     * adds event handler to marks so we can detet if selected by the user
     * @param circle
     */
    private void addMarkSelectionHandlers(Circle circle){
        circle.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            root.setCursor(cursor);
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
        double offsetX = ANNOTATION_OFFSET_X;
        double offsetY = ANNOTATION_OFFSET_Y;
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
                    displayBoat.removeLaylines(root);
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
                    if (scoreBoardController.isLayLinesSelected()){
                        if (displayBoat.equals(selectedBoat)) {
                            drawLayLine(displayBoat);
                        }
                    }
                    drawBoatAnnotation(displayBoat, annotations);
                } else if (level == AnnotationLevel.ALL_ANNOTATIONS) {
                    annotations.clear();
                    annotations.add(boatName);
                    annotations.add(displayBoat.getSpeed());
                    annotations.add(displayBoat.getTimeSinceLastMark(currTime));
                    annotations.add(displayBoat.getTimeToNextMark(displayBoat.getBoat().getTimeAtNextMark(), currTime));
                    drawBoatAnnotation(displayBoat, annotations);
                    if (displayBoat.equals(selectedBoat)) {
                        drawLayLine(displayBoat);
                    }
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
     *  Draws laylines for a boat coming from the next mark it is heading to (at the moment is Mark1)
     * @param boat
     */
    //TODO create function that chooses closest mark to draw laylines from also check if boat is not tacking or gybing so lines are not drawn
    private void drawLayLine(BoatDisplay boat){
        boolean draw = false;
        double windDirection = race.getCourse().getWindDirection();
        double heading = boat.getBoat().getHeading();
        if(MathUtils.pointBetweenTwoAngle(windDirection, boat.getBoat().getTWAofBoat(), heading)){
            draw = true;
        } else if(MathUtils.pointBetweenTwoAngle((windDirection + 180) % 360, 180 - boat.getBoat().getGybeTWAofBoat(), heading)) {
            draw = true;
        }
        if (boat.getBoat().getLastRoundedMarkIndex() < race.getCourse().getCourseOrder().size() - 1 && boat.getBoat().getLastRoundedMarkIndex() != -1 && draw == true) {
            boat.removeLaylines(root);
            boat.removeBoatLaylines(root);
            CompoundMark mark = race.getCourse().getCourseOrder().get(boat.getBoat().getLastRoundedMarkIndex() + 1);
            Pair<Double, Double> bearing = boat.getBoat().calculateLaylineHeading(race.getCourse().getTrueWindDirection());
            Pair<Line, Line> laylines = raceView.createLayLines(bearing, mark, boat);
            Pair<Line, Line> boatLaylines = raceView.createBoatLayLines(bearing, mark, boat);
            Line layline1 = laylines.getKey();
            Line layline2 = laylines.getValue();
            Line boatLayline1 = boatLaylines.getKey();
            Line boatLayline2 = boatLaylines.getValue();
            root.getChildren().add(layline1);
            root.getChildren().add(layline2);
            root.getChildren().add(boatLayline1);
            root.getChildren().add(boatLayline2);
            boat.setLaylines(laylines);
            boat.setBoatLaylines(boatLaylines);
            layline1.toBack();
            layline2.toBack();
        }
    }


    /**
     * Update a boat icon's position on screen, translating to the input point
     * @param boat the BoatDisplay to be moved
     * @param point where the boat display should be moved to
     */
    private void moveBoat(BoatDisplay boat, CanvasCoordinate point){
        boat.getIcon().setTranslateY(point.getY());
        boat.getIcon().setTranslateX(point.getX());
        boat.getIcon().getTransforms().clear();
        boat.getIcon().getTransforms().add(new Rotate(boat.getBoat().getHeading()));
        boat.getIcon().toFront();
    }

    /**
     * Moves compass arrow to correct position when canvas is resized.
     */
    public void moveWindArrow() {
        currentWindArrow.setX(Controller.getCanvasWidth() - WIND_ARROW_OFFSET);
    }

    /**
     * Moves the wake of a boat to the correct position
     * @param boat the boatDisplay who's wake should move
     * @param point where the boat is now positioned
     */
    private void moveWake(BoatDisplay boat, CanvasCoordinate point){
        double scale = boat.getBoat().getSpeed() / WAKE_SCALE_FACTOR;
        boat.getWake().getTransforms().clear();
        boat.getWake().getTransforms().add(new Scale(scale, scale,0, 0));
        boat.getWake().setTranslateY(point.getY());
        boat.getWake().setTranslateX(point.getX());
        boat.getWake().getTransforms().add(new Rotate(boat.getBoat().getHeading(), 0, 0));
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
            mark.getIcon().toFront();
            mark.getIcon().setCenterX(convertedPoint.getX());
            mark.getIcon().setCenterY(convertedPoint.getY());
            mark.getIcon().toBack();
        }
    }

    /**
     * Redraws the boundary at the correct scale and position after a window resize
     */
    private void redrawBoundary(){
        boundary.getPoints().clear();
        for(Coordinate coord : race.getCourse().getBoundary()){
            CanvasCoordinate point = DisplayUtils.convertFromLatLon(coord);
            boundary.getPoints().add(point.getX());
            boundary.getPoints().add(point.getY());
        }
        boundary.toBack();
    }

    /**
     * Redraws all the boat paths by reconverting all the coordinates a boat has been to and recreates the
     * elements (points) of the path of the boat.
     */
    public void redrawBoatPaths(){
        for(BoatDisplay boatDisplay : displayBoats){
            Boat boat = boatDisplay.getBoat();
            if(race.getRaceStatus() == STARTED && !boat.isFinished()) {
                if (boat.getPathCoords().size() > 0) {
                    CanvasCoordinate pathStart = DisplayUtils.convertFromLatLon(boat.getPathCoords().get(0));
                    boatDisplay.getPath().getElements().clear();
                    boatDisplay.getPath().getElements().add(new MoveTo(pathStart.getX(), pathStart.getY()));
                    for (Coordinate coord : boat.getPathCoords()) {
                        CanvasCoordinate currPoint = DisplayUtils.convertFromLatLon(coord);
                        boatDisplay.getPath().getElements().add(new LineTo(currPoint.getX(), currPoint.getY()));
                    }
                    boatDisplay.getPath().toBack();
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
        scoreBoardController.btnTrack.setVisible(true);
        scoreBoardController.chkMultipleSelect.setVisible(true);
        selectedBoat.getIcon().toFront();
        for(BoatDisplay boat : displayBoats){
            if(!boat.equals(selectedBoat)){
                boat.unFocus();
                boat.removeLaylines(root);
                boat.removeBoatLaylines(root);
            }else{
                boat.focus();
                drawLayLine(boat);
            }
        }
    }

    public void setCurrentWindArrow(ImageView currentWindArrow) {
        this.currentWindArrow = currentWindArrow;
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

    public boolean hasInitializedBoats() {
        return initializedBoats;
    }



}

