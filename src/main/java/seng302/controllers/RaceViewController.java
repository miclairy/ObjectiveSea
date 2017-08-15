package seng302.controllers;

import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.animation.Interpolator;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Path;

import javafx.scene.shape.*;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import seng302.data.BoatStatus;
import seng302.data.RaceVisionXMLParser;
import seng302.data.StartTimingStatus;
import seng302.utilities.*;
import seng302.models.*;
import seng302.views.BoatDisplay;
import seng302.views.RaceView;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.abs;
import static seng302.data.RaceStatus.PREPARATORY;
import static seng302.data.RaceStatus.STARTED;
import static seng302.data.RaceStatus.TERMINATED;
import static seng302.utilities.DisplayUtils.zoomLevel;

/**
 * Created on 6/03/17.
 * Class to manage the output display.
 */

public class RaceViewController extends AnimationTimer implements Observer {

    private enum AnnotationLevel {
        NO_ANNOTATION, IMPORTANT_ANNOTATIONS, ALL_ANNOTATIONS
    }

    private final ArrayList<Paint> WIND_COLORS = new ArrayList<>((Arrays.asList(Paint.valueOf("#92c9ff"), Paint.valueOf("#77b9f6"),
            Paint.valueOf("#5aa4d8"), Paint.valueOf("#668ecb"), Paint.valueOf("#a57da3"), Paint.valueOf("#cb7387"),
            Paint.valueOf("#e6666e"), Paint.valueOf("#ea4849"))));

    private final double WAKE_SCALE_FACTOR = 17;
    private final double SOG_SCALE_FACTOR = 200.0;
    private final int ANNOTATION_HANDLE_OFFSET = 8;
    private final double WIND_ARROW_X_PADDING = 31;
    private final double WIND_ARROW_Y_PADDING = 19;
    private final double WIND_CIRCLE_X_PADDING = 16.5;
    private final double WIND_CIRCLE_Y_PADDING = 10;

    private Race race;
    private Group root;
    private Controller controller;
    private RaceView raceView;
    private ScoreBoardController scoreBoardController;
    private ArrayList<BoatDisplay> displayBoats = new ArrayList<>();
    private double previousTime = 0;
    private Polygon boundary;
    private Circle windCircle;
    private Polyline windArrow;
    private boolean windTransitionPlaying = false;
    private double currentTimeInSeconds;
    private AnnotationLevel currentAnnotationsLevel;
    private boolean courseNeedsRedraw = false;
    private boolean initializedBoats = false;
    private Set<BoatDisplay> selectedBoats = new HashSet<>();
    private DistanceLine distanceLine = new DistanceLine();
    private boolean drawDistanceLine = false;
    private boolean firstTime = true;
    private SelectionController selectionController;
    private Penalties penalties = new Penalties();

    private double sailWidth = 5;
    private boolean isSailWidthChanging = false;

    private int flickercounter = 0;
    private int prevWindColorNum = 0;

    private boolean isTutorial = false;

    private Tutorial tutorial;

    BoatDisplay currentUserBoatDisplay;
    Shape boatHighlight = null;

    public RaceViewController(Group root, Race race, Controller controller, ScoreBoardController scoreBoardController, SelectionController selectionController) {
        this.root = root;
        this.race = race;
        this.controller = controller;
        this.raceView = new RaceView();
        this.scoreBoardController = scoreBoardController;
        this.selectionController = selectionController;
        if(Objects.equals(RaceVisionXMLParser.courseFile, "GuidedPractice-course.xml")) {
            isTutorial = true;
            tutorial = new Tutorial(controller, race);
            controller.hideStarterOverlay();
            scoreBoardController.annotationsSlider.setVisible(false);
            scoreBoardController.lblAnnotation.setVisible(false);
            initBoatHighlight();
            initializeBoats();
            initBoatPaths();
            currentAnnotationsLevel = AnnotationLevel.NO_ANNOTATION;
            changeAnnotations(currentAnnotationsLevel, true);
        }

        redrawCourse();
        race.addObserver(this);
    }

    @Override
    public void handle(long currentTime) {
        if (previousTime == 0) {
            previousTime = currentTime;
            return;
        }

        double secondsElapsed = TimeUtils.convertNanosecondsToSeconds(currentTime - previousTime);

        if(!race.isTerminated() && !isTutorial){
            controller.updateRaceClock();
        }else if (race.isTerminated()){
            if(race.getAbruptEnd()){
                controller.blurScreen(true);
                controller.showServerDisconnectError();
                this.stop();
            } else{
                controller.showStarterOverlay();
                this.stop();
            }
        }
        if(controller.hasRaceStatusChanged()){
            if(!isTutorial){
                controller.updatePreRaceScreen();
                controller.setRaceStatusChanged(false);
            }
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
        if (drawDistanceLine) redrawDistanceLines();
        selectionController.zoomTracking();

        for (BoatDisplay displayBoat: displayBoats) {
            moveBoatDisplay(displayBoat);
        }
        if(!isTutorial){
            redrawRaceLines();
        } else {
            if(tutorial != null) tutorial.runTutorial();
        }
        if (courseNeedsRedraw) redrawCourse();
        changeAnnotations(currentAnnotationsLevel, true);
        controller.updatePlacings();
        updateWindArrow();
        flickercounter++;
        orderDisplayObjects();
    }




    /**
     * Moves and individual BoatDisplay object
     * moves the onscreen boat, wake, sail, annotations to where they should be onscreen
     * @param boatDisplay the boat to be moved
     */
    private void moveBoatDisplay(BoatDisplay boatDisplay){
        CanvasCoordinate point = DisplayUtils.convertFromLatLon(boatDisplay.getBoat().getCurrentLat(), boatDisplay.getBoat().getCurrentLon());
        moveBoat(boatDisplay, point);
        moveWake(boatDisplay, point);
        moveSail(boatDisplay, point);
        if(!isTutorial){
            displayCollisions(boatDisplay, point);
        }

        manageStartTiming(boatDisplay);
        drawVirtualStartLine(boatDisplay);

        moveSOGVector(boatDisplay);
        moveVMGVector(boatDisplay);
        if(race.getRaceStatus() == STARTED) {
            addToBoatPath(boatDisplay, point);
        }
        moveBoatAnnotation(boatDisplay.getAnnotation(), point, boatDisplay);
        if(scoreBoardController.areVectorsSelected()){
            boatDisplay.showVectors();
        } else {
            boatDisplay.hideVectors();
        }
        if (scoreBoardController.isLayLinesSelected()){
            boatDisplay.getLaylines().removeDrawnLines(root);
            if (selectedBoats.contains(boatDisplay)) {
                drawLayline(boatDisplay);
            }
        } else {
            boatDisplay.getLaylines().removeDrawnLines(root);
        }
        if(boatDisplay.getBoat().getStatus() == BoatStatus.DNF){
            boatDisplay.unFocus();
        }
    }

    /**
     * draws the virtual startline if the race hasnt started and the option is selected, otherwise removes the line
     * @param boatDisplay boat display variable that contains the boat we want to draw the line for
     */
    public void drawVirtualStartLine(BoatDisplay boatDisplay){
        if(scoreBoardController.isVirtualStartlineSelected() && !race.hasStarted()) {
            drawPredictedStartLine(boatDisplay);
        } else {
            if(root.getChildren().contains(boatDisplay.getPredictedStartLine())) {
                root.getChildren().remove(boatDisplay.getPredictedStartLine());
            }
        }
    }

    /**
     * sets the order of the objects on the display. defines what object will go on top of another.
     */
    private void orderDisplayObjects(){
        distanceLine.getAnnotation().toFront();
        if(currentUserBoatDisplay != null){
            currentUserBoatDisplay.getIcon().toFront();
            currentUserBoatDisplay.getSail().toFront();
            currentUserBoatDisplay.getAnnotation().toFront();
        }
        if(selectionController.getTrackingBoat() != null){
            selectionController.getTrackingBoat().getAnnotation().toFront();
        }
    }

    /**
     * Checks if a boat is colliding and displays the animation if so
     * @param displayBoat the displayBoat to check and display collisions for
     * @param point the canvas position of the display boat where we will display the collision animation
     */
    private void displayCollisions(BoatDisplay displayBoat, CanvasCoordinate point) {
        Boat boat = displayBoat.getBoat();

        if(boat.isMarkColliding() || boat.isBoatColliding()){
            if(!displayBoat.collisionInProgress){
                collisionAnimation(point, displayBoat);
                displayBoat.setCollisionInProgress(true);
            }
            boat.setMarkColliding(false);
            boat.setBoatColliding(false);
        }

        if (boat.isFinished() && boat.isJustFinished()){
            boat.setJustFinished(false);
            long finishTime = race.getCurrentTimeInEpochMs() - race.getStartTimeInEpochMs();
            boat.setFinishTime(finishTime);
        }
    }

    /**
     * Manages the +/- start timing indicator for a boat
     * @param displayBoat the display boat to manage
     */
    private void manageStartTiming(BoatDisplay displayBoat) {
        Boat boat = displayBoat.getBoat();
        int leg = boat.getLeg();
        ArrayList<CompoundMark> courseOrder = race.getCourse().getCourseOrder();
        if (boat.getTimeStatus() != StartTimingStatus.INRACE && leg < courseOrder.size() && courseOrder.get(leg).isStartLine()) {
            if (flickercounter % 300 == 0) {
                displayBoat.getStartTiming(race);
            }
        } else {
            boat.setTimeStatus(StartTimingStatus.INRACE);
        }
    }


    /**
     * Draws and sets up BoatDisplay objects onscreen
     */
    public void initializeBoats() {
        PolarTable polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
        for (Boat boat : race.getCompetitors()){
            initializeBoat(polarTable, boat);
        }
        initializedBoats = true;
        changeAnnotations(currentAnnotationsLevel, true);
        selectionController.setDisplayBoats(Collections.unmodifiableList(displayBoats));
        for(BoatDisplay boatDisplay : displayBoats ){
            if(boatDisplay.getBoat().getId() == Main.getClient().getClientID()){
                currentUserBoatDisplay = boatDisplay;
            }
        }
    }

    /**
     * Draw and set up a single BoatDisplay
     * @param polarTable
     * @param boat
     */
    private void initializeBoat(PolarTable polarTable, Boat boat) {
        BoatDisplay displayBoat = new BoatDisplay(boat, polarTable);
        boat.addObserver(displayBoat);
        scoreBoardController.addBoatToSparkLine(displayBoat.getSeries());
        raceView.assignColor(displayBoat);
        drawBoat(displayBoat);
        displayBoats.add(displayBoat);
        selectionController.addBoatSelectionHandler(displayBoat);

        Circle grabHandle = new Circle(5);
        grabHandle.setId("annoGrabHandle");
        grabHandle.setCenterX(0);
        grabHandle.setCenterY(0);
        displayBoat.setAnnoGrabHandle(grabHandle);
        root.getChildren().add(grabHandle);

        selectionController.makeDraggable(grabHandle, displayBoat);

        CubicCurve sail = new CubicCurve(0,0, 0,0,0,0, 20*zoomLevel,0);
        sail.setId("boatSail");
        displayBoat.setSail(sail);
        root.getChildren().add(sail);
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
        drawSOGVector(boat);
        drawVMGVector(boat);
    }

    public void initBoatHighlight(){
        boatHighlight = new Circle(0,0,10);
        boatHighlight.setId("usersBoatHighlight");
        root.getChildren().add(boatHighlight);
    }

    /**
     * inits drawing of the boat paths
     */
    void initBoatPaths(){
        for (BoatDisplay boat : displayBoats){
            initBoatPath(boat);
        }
    }

    /**
     * creates an animation to visual a collision
     * @param point the point where the collision iss
     */
    void collisionAnimation(CanvasCoordinate point, BoatDisplay boat){
        Circle collisionCircle1 = createCollisionCircle(point);
        Circle collisionCircle2 = createCollisionCircle(point);

        ScaleTransition st1 = AnimationUtils.scaleTransitionCollision(collisionCircle1, 500, 20 * zoomLevel);
        st1.setOnFinished(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent AE) {
                root.getChildren().remove(collisionCircle1);
            }});

        ScaleTransition st2 = AnimationUtils.scaleTransitionCollision(collisionCircle2, 300, 30 * zoomLevel);
        st2.setOnFinished(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent AE) {
                root.getChildren().remove(collisionCircle2);
            }});

        FadeTransition ft1 = AnimationUtils.fadeOutTransition(collisionCircle1, 800);
        FadeTransition ft2 = AnimationUtils.fadeOutTransition(collisionCircle2, 600);
        ft2.setOnFinished(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent AE) {
                boat.setCollisionInProgress(false);
            }});

        ParallelTransition pt = new ParallelTransition(st1, st2, ft1, ft2);
        pt.play();
    }

    /**
     * creates and returns a circle
     * @param point canvas coord point that is used as the center of the circle
     */
    private Circle createCollisionCircle(CanvasCoordinate point){
        Circle circle = new Circle();
        circle.setRadius(1);
        circle.setId("collisionCircle");
        circle.setCenterX(point.getX());
        circle.setCenterY(point.getY());
        root.getChildren().add(circle);
        return circle;
    }



    /**
     * Initalises a Path for a boat
     * @param boatDisplay the boatDisplay to create a path for
     */
    private void initBoatPath(BoatDisplay boatDisplay){
        Path path = raceView.createBoatPath(boatDisplay.getColor());
        Boat boat = boatDisplay.getBoat();
        CanvasCoordinate point = DisplayUtils.convertFromLatLon(boat.getCurrentLat(), boat.getCurrentLon());
        path.getElements().add(new MoveTo(point.getX(), point.getY()));
        boatDisplay.setPath(path);
        root.getChildren().add(path);
    }

    /**
     * Handles redrawing of the course at the correct scale and position after a window resize
     */
    void redrawCourse(){
        courseNeedsRedraw = false;
        if(!isTutorial) {
            drawMarks();
            drawBoundary();
            redrawRaceLines();
        } else {
            changeAnnotations(0, true);
        }
        drawMap();
        drawWindArrow();
    }

    /**
     * Draws both the start end and the finish line
     */
    private void redrawRaceLines() {
        redrawRaceLine(race.getCourse().getStartLine());
        redrawRaceLine(race.getCourse().getFinishLine());
    }

    /**
     * Redraws a raceline on the visual
     * @param raceLine
     */
    private void redrawRaceLine(RaceLine raceLine) {
        if (root.getChildren().contains(raceLine.getLine())) {
            root.getChildren().remove(raceLine.getLine());
        }
        Line line = raceView.createRaceLine(raceLine.getMark1().getPosition(), raceLine.getMark2().getPosition());
        root.getChildren().add(line);
        raceLine.setLine(line);
        raceLine.getLine().toBack();
    }

    /**
     * draws the predicted startline of a boat.
     * @param boatDisplay display boat of the current boat that needs its start line to be drawn
     */
    private void drawPredictedStartLine(BoatDisplay boatDisplay) {
        if(boatDisplay.equals(currentUserBoatDisplay)) {
            if (root.getChildren().contains(boatDisplay.getPredictedStartLine())) {
                root.getChildren().remove(boatDisplay.getPredictedStartLine());
            }
            boatDisplay.getVirtualStartline(race);
            Line newPredictedStartLine = boatDisplay.getPredictedStartLine();
            root.getChildren().add(newPredictedStartLine);
            boatDisplay.getPredictedStartLine().toBack();

        }
    }


    /**
     * Handles drawing of all of the marks from the course
     */
    public void drawMarks() {
        for (Mark mark : race.getCourse().getAllMarks().values()) {
            if (mark.getIcon() != null && root.getChildren().contains(mark.getIcon())){
                CanvasCoordinate point = DisplayUtils.convertFromLatLon(mark.getPosition());
                mark.getIcon().setCenterX(point.getX());
                mark.getIcon().setCenterY(point.getY());
            }else{
                Circle circle = raceView.createMark(mark.getPosition());
                root.getChildren().add(circle);
                mark.setIcon(circle);
                selectionController.addMarkSelectionHandlers(mark);
            }

            mark.getIcon().toFront();
            mark.getIcon().setScaleX(zoomLevel);
            mark.getIcon().setScaleY(zoomLevel);
        }
    }

    /**
     * Handles drawing the boundary and adds styling from the course array of co-ordinates
     */
    private void drawBoundary(){
        if (root.getChildren().contains(boundary)){
            root.getChildren().remove(boundary);
        }
        boundary = raceView.createCourseBoundary(race.getCourse().getBoundary());
        root.getChildren().add(boundary);
        boundary.toBack();
        selectionController.addDeselectEvents(boundary);
    }

    /**
     * handles the drawing of the map image and makes it fullscreen
     */
    void drawMap(){
        String mapURL = DisplayUtils.getLocalMapURL(race);
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
        annotationFrame.setId("annotationFrame");
        annotationFrame.getChildren().clear();
        for(String string : annotationText){
            Label annotationLabel = new Label(string);
            annotationLabel.setId("annotationLabel");
            annotationFrame.getChildren().add(annotationLabel);
        }

        double annoHeight = annotationFrame.getBoundsInParent().getHeight() / 2;
        double annoWidth = annotationFrame.getBoundsInParent().getWidth() / 2;

        Line annoLine = new Line(point.getX(), point.getY(), annotationFrame.getLayoutX() + annoWidth, annotationFrame.getLayoutY() + annoHeight);
        annoLine.setId("annotationLine");
        root.getChildren().remove(displayBoat.getAnnotationLine());
        displayBoat.setAnnotationLine(annoLine);


        root.getChildren().add(annotationFrame);
        root.getChildren().add(annoLine);

        displayBoat.setAnnotation(annotationFrame);
        displayBoat.getAnnotation().toFront();
        displayBoat.getAnnotationLine().toBack();
    }

    /**
     * Move's the annotation to where the boat is now.
     * @param annotation the annotation to be moved
     * @param point where the boat has moved to
     * @param point where the boat has moved to
     */
    private void moveBoatAnnotation(VBox annotation, CanvasCoordinate point, BoatDisplay boatDisplay){

        annotation.getTransforms().clear();
        annotation.getTransforms().add(new Rotate(-selectionController.getRotationOffset(), annotation.getWidth()/2, annotation.getHeight()/2));

        if (zoomLevel <=1){
            //check if outside bounds
            double outsideX = 0;
            double outsideY = 0;
            if(annotation.getBoundsInParent().getMaxX() > Controller.getCanvasWidth()){
                outsideX += annotation.getBoundsInParent().getMaxX() - Controller.getCanvasWidth();
            } else if (annotation.getBoundsInParent().getMinX() < 0){
                outsideX += annotation.getBoundsInParent().getMinX();
            }
            if(annotation.getBoundsInParent().getMaxY() > Controller.getCanvasHeight()){
                outsideY += annotation.getBoundsInParent().getMaxY() - Controller.getCanvasHeight();
            } else if (annotation.getBoundsInParent().getMinY() < 0){
                outsideY += annotation.getBoundsInParent().getMinY();
            }
            boatDisplay.setAnnoOffsetX(boatDisplay.getAnnoOffsetX() - outsideX);
            boatDisplay.setAnnoOffsetY(boatDisplay.getAnnoOffsetY() - outsideY);
        }


        if(!boatDisplay.getAnnoHasMoved()){
            annotation.relocate(
                    (point.getX() + boatDisplay.getAnnoOffsetX() * zoomLevel),
                    (point.getY() + boatDisplay.getAnnoOffsetY() * zoomLevel)
            );
        }
        Circle grabHandle = boatDisplay.getAnnoGrabHandle();
        grabHandle.setCenterX(point.getX() + boatDisplay.getAnnoOffsetX() * zoomLevel - ANNOTATION_HANDLE_OFFSET);
        grabHandle.setCenterY(point.getY() + boatDisplay.getAnnoOffsetY() * zoomLevel - ANNOTATION_HANDLE_OFFSET);
        grabHandle.toFront();
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
                    displayBoat.annoGrabHandle.setVisible(false);
                }
                if (level == AnnotationLevel.IMPORTANT_ANNOTATIONS) {
                    displayBoat.annoGrabHandle.setVisible(true);
                    annotations.clear();
                    if(scoreBoardController.isNameSelected()){
                        annotations.add(boatName);
                    }
                    if(scoreBoardController.isSpeedSelected()){
                        annotations.add(displayBoat.getSpeed());
                    }
                    if(scoreBoardController.isTimePassedSelected()){
                        annotations.add(DisplayUtils.getTimeSinceLastMark(currTime, displayBoat.getBoat()));
                    }
                    if(scoreBoardController.isEstSelected()){
                        annotations.add(DisplayUtils.getTimeToNextMark(displayBoat.getBoat().getTimeAtNextMark(), currTime));
                    }
                    if(scoreBoardController.isStartTimeSelected()){
                        if(displayBoat.getStartTimingAnnotation() != null){
                            annotations.add(displayBoat.getStartTimingAnnotation());
                        }
                    }
                    if(scoreBoardController.areVectorsSelected()){
                        displayBoat.showVectors();
                    }
                    if (scoreBoardController.isLayLinesSelected()){
                        if (selectedBoats.contains(displayBoat)) {
                            drawLayline(displayBoat);
                        }
                    }
                    drawBoatAnnotation(displayBoat, annotations);
                } else if (level == AnnotationLevel.ALL_ANNOTATIONS) {
                    displayBoat.annoGrabHandle.setVisible(true);
                    annotations.clear();
                    annotations.add(boatName);
                    annotations.add(displayBoat.getSpeed());
                    annotations.add(DisplayUtils.getTimeSinceLastMark(currTime, displayBoat.getBoat()));
                    annotations.add(DisplayUtils.getTimeToNextMark(displayBoat.getBoat().getTimeAtNextMark(), currTime));
                    if(displayBoat.getStartTimingAnnotation() != null){
                        annotations.add(displayBoat.getStartTimingAnnotation());
                    }
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
     * Draws Initial VMGVector of boat
     * @param boat to attach the vector to
     */
    private void drawVMGVector(BoatDisplay boat){
        Color color = boat.getColor();
        Course course = race.getCourse();
        double VMG = boat.getBoat().calculateVMGToMark(course);
        double scale = VMG / SOG_SCALE_FACTOR;
        Polyline line = raceView.createVMGVector(boat.getBoat(), scale, course, color);
        root.getChildren().add(line);
        boat.setVMGVector(line);
    }

    /**
     * Moves the VMGVector of a boat to the correct position
     * @param boat the boatDisplay who's VMGVector should move
     */
    private void moveVMGVector(BoatDisplay boat){
        Color color = boat.getColor();
        Course course = race.getCourse();
        root.getChildren().remove(boat.getVMGVector());
        double VMG = boat.getBoat().calculateVMGToMark(course);
        double scale = VMG / SOG_SCALE_FACTOR;
        Polyline oldLine = boat.getVMGVector();
        Polyline newLine = raceView.createVMGVector(boat.getBoat(), scale, course, color);
        newLine.setId("vectorLine");
        newLine.setOpacity(oldLine.getOpacity());
        root.getChildren().add(newLine);
        boat.setVMGVector(newLine);
        newLine.toBack();
    }

    /**
     * Draws Initial SOGVector of boat
     * @param boat to attach the vector to
     */
    private void drawSOGVector(BoatDisplay boat){
        double scale = boat.getBoat().getCurrentSpeed() / SOG_SCALE_FACTOR;
        Color color = boat.getColor();
        Polyline line = raceView.createSOGVector(boat.getBoat(), scale, color);
        root.getChildren().add(line);
        boat.setSOGVector(line);
    }

    /**
     * Moves the SOGVector of a boat to the correct position
     * @param boat the boatDisplay who's SOGVector should move
     */
    private void moveSOGVector(BoatDisplay boat){
        double scale = boat.getBoat().getCurrentSpeed() / SOG_SCALE_FACTOR;
        Color color = boat.getColor();
        root.getChildren().remove(boat.getSOGVector());
        Polyline oldLine = boat.getSOGVector();
        Polyline newLine = raceView.createSOGVector(boat.getBoat(), scale, color);
        newLine.setOpacity(oldLine.getOpacity());
        root.getChildren().add(newLine);
        boat.setSOGVector(newLine);
        newLine.toBack();
    }

    /**
     * Draws laylines for a boat at the next gate it is heading to if it is a windward gate
     * @param boatDisplay the boat object to display laylines for
     */
    private void drawLayline(BoatDisplay boatDisplay){
        Boat boat = boatDisplay.getBoat();
        Course course = race.getCourse();
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        if (boat.getLastRoundedMarkIndex() < course.getCourseOrder().size() - 2 && boat.getLastRoundedMarkIndex() != -1) {
            Laylines laylines = boatDisplay.getLaylines();
            laylines.removeDrawnLines(root);

            CompoundMark lastMark = courseOrder.get(boat.getLastRoundedMarkIndex());
            CompoundMark nextMark = courseOrder.get(boat.getLastRoundedMarkIndex() + 1);

            boolean nextMarkIsCompound = nextMark.hasTwoMarks();

            if (nextMarkIsCompound) {
                Coordinate mark1Coord = nextMark.getMark1().getPosition();
                Coordinate mark2Coord = nextMark.getMark2().getPosition();

                double TWD = course.getWindDirection();
                laylines.calculateLaylineAngle(TWD, lastMark, nextMark, boatDisplay.getPolarTable());
                if (laylines.shouldDraw()) {

                    boolean mark1OnLeft = mark1Coord.isOnLeftOfBearingFromMidpoint(mark2Coord, TWD);
                    Coordinate leftOfLine, rightOfLine;
                    if (mark1OnLeft) {
                        leftOfLine = mark1Coord;
                        rightOfLine = mark2Coord;
                    } else {
                        leftOfLine = mark2Coord;
                        rightOfLine = mark1Coord;
                    }

                    Line layline1 = raceView.drawLayline(laylines.getRightOfTWDAngle(), rightOfLine, boatDisplay.getColor());
                    Line layline2 = raceView.drawLayline(laylines.getLeftOfTWDAngle(), leftOfLine, boatDisplay.getColor());

                    root.getChildren().add(layline1);
                    root.getChildren().add(layline2);
                    layline1.toBack();
                    layline2.toBack();

                    laylines.setDrawnlines(layline1, layline2);
                }
            }
        }
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
        icon.setScaleX(zoomLevel);
        icon.setScaleY(zoomLevel);
        icon.getTransforms().clear();
        icon.getTransforms().add(new Rotate(boat.getBoat().getHeading()));

        if(boat.equals(currentUserBoatDisplay)){
            boatHighlight.setTranslateY(point.getY());
            boatHighlight.setTranslateX(point.getX());
            boatHighlight.setScaleX(zoomLevel*1.5);
            boatHighlight.setScaleY(zoomLevel*1.5);
            boatHighlight.toFront();
        }
        icon.toFront();
    }

    /**
     * Updates the location of the sail of a particular boat onscreen
     * @param point location of the sail to move to
     * @param boat display boat with sail to move
     */
    private void moveSail(BoatDisplay boat, CanvasCoordinate point){
        boat.getSail().setStrokeWidth(1.5 * zoomLevel);
        if(!boat.getBoat().isSailsIn()){
            boat.moveSail(point, 0,0,0,0, boat.getBoat().getSailAngle(race.getCourse().getWindDirection()));
        }else{
            if(isSailWidthChanging){
                sailWidth += 0.5;
            }else{
                sailWidth-= 0.5;
            }

            if(sailWidth > 5 || sailWidth < -5){
                isSailWidthChanging = !isSailWidthChanging;
            }

            double length  = 14 * zoomLevel;
            boat.moveSail(point, length/4,3 * length / 4,sailWidth,-sailWidth, boat.getBoat().getSailAngle(race.getCourse().getWindDirection()));
        }
    }


    /**
     * Moves the wake of a boat to the correct position
     * @param boat the boatDisplay who's wake should move
     * @param point where the boat is now positioned
     */
    private void moveWake(BoatDisplay boat, CanvasCoordinate point){

        Shape wake = boat.getWake();
        double scale = boat.getBoat().getCurrentSpeed() / WAKE_SCALE_FACTOR;
        scale *= zoomLevel;
        wake.getTransforms().clear();
        wake.getTransforms().add(new Scale(scale, scale,0, 0));
        wake.setTranslateY(point.getY());
        wake.setTranslateX(point.getX());
        wake.getTransforms().add(new Rotate(boat.getBoat().getHeading(), 0, 0));
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
                    CanvasCoordinate pathStart = DisplayUtils.convertFromLatLon((Coordinate) boat.getPathCoords().get(0));
                    path.getElements().add(new MoveTo(pathStart.getX(), pathStart.getY()));
                    ListIterator pathIter = boat.getPathCoords().listIterator();
                    while(pathIter.hasNext()){
                        CanvasCoordinate currPoint = DisplayUtils.convertFromLatLon((Coordinate) pathIter.next());
                        path.getElements().add(new LineTo(currPoint.getX(), currPoint.getY()));
                    }
                    path.toBack();
                }
            }
        }
    }

    /**
     * redraws the distance lines of the selected boat or boats.
     */
    private void redrawDistanceLines(){
        removeDistanceLines();
        if (distanceLine.sameLeg() && !distanceLine.boatsFinished()) {
            updateDistanceMark();
            distanceLine.reCalcLine();
            for (Line line : distanceLine.getLines()) {
                root.getChildren().add(line);
                line.toBack();
            }
            updateDistanceLineAnnotation();
            Label annotation = distanceLine.getAnnotation();
            annotation.getTransforms().clear();
            if(selectionController.isRotationEnabled()){
                annotation.getTransforms().add(new Rotate(-selectionController.getRotationOffset()));
            }
            root.getChildren().add(annotation);
        }
    }

    private void removeDistanceLines(){
        for(Line line : distanceLine.getLines()){
            root.getChildren().remove(line);
            root.getChildren().remove(distanceLine.getAnnotation());
        }
    }

    /**
     * updates the distance line annotation to display the correct distance between the two boats, or
     * boat and course feature.
     */
    private void updateDistanceLineAnnotation(){
        double distance;
        CanvasCoordinate canvasCoord;
        if (!Objects.equals(distanceLine.getFirstBoat().getId(), distanceLine.getSecondBoat().getId())) {
            distance = distanceLine.getDistanceBetweenBoats();
            canvasCoord = distanceLine.halfwayBetweenBoatsCoord();
        } else {
            CompoundMark targetMark = distanceLine.getMark();
            Coordinate target;
            if (targetMark.hasTwoMarks()) {
                target = DisplayUtils.midPointFromTwoCoords(targetMark.getMark1().getPosition(), targetMark.getMark2().getPosition());
                distance = target.greaterCircleDistance(distanceLine.getFirstBoat().getCurrentPosition());
            } else {
                target = distanceLine.getMark().getPosition();
                distance = target.greaterCircleDistance(distanceLine.getFirstBoat().getCurrentPosition());
            }
            canvasCoord = DisplayUtils.convertFromLatLon(DisplayUtils.midPointFromTwoCoords(target, distanceLine.getFirstBoat().getCurrentPosition()));
        }
        Label distanceLineAnnotation = distanceLine.getAnnotation();
        distanceLineAnnotation.setText(String.valueOf((int) TimeUtils.convertNauticalMilesToMetres(distance) + " m"));
        distanceLineAnnotation.layoutXProperty().set(canvasCoord.getX());
        distanceLineAnnotation.layoutYProperty().set(canvasCoord.getY());
        distanceLine.setAnnotation(distanceLineAnnotation);
        distanceLineAnnotation.toFront();
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


    /**
     * Notified by either the race or the selection controller and updates the race view accordingly
     * @param obs the observed object
     */
    @Override
    public void update(Observable obs, Object object) {
        if (obs == race && object instanceof Integer) {
            Integer sig = (Integer) object;
            switch(sig) {
                case Race.UPDATED_COMPETITORS_SIGNAL:
                    Platform.runLater(() -> updateCompetitors(race.getCompetitors()));
                break;
            }
        } else if (obs == selectionController){
            Boolean tracking = (Boolean) object;
            for(BoatDisplay boat : displayBoats){
                if (selectedBoats.contains(boat) && !selectionController.getSelectedBoats().contains(boat)) {
                    updateDistanceLine(false);
                }
                if (selectedBoats.contains(boat)){
                    updateDistanceLine(scoreBoardController.isDistanceLineSelected());
                    drawLayline(boat);
                }
            }
            if (tracking){
                redrawCourse();
                redrawBoatPaths();
            }
            selectedBoats = selectionController.getSelectedBoats();
        }
    }

    /**
     * Called when a new competitor has been added to the race to deal with adding in a BoatDisplay object
     * @param competitors
     */
    private void updateCompetitors(List<Boat> competitors) {
        for (Boat boat : competitors) {
            boolean needsToBeAdded = true;
            for (BoatDisplay displayBoat : displayBoats) {
                if (displayBoat.getBoat() == boat) {
                    needsToBeAdded = false;
                }
            }
            if (needsToBeAdded) {
                if (hasInitializedBoats()) {
                    PolarTable polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
                    initializeBoat(polarTable, boat);
                }
            }
        }
        controller.displayStarters();
    }


    public boolean hasInitializedBoats() {
        return initializedBoats;
    }

    /**
     * Updates the boats selected for the lines to be drawn for
     */
    public void updateDistanceLine(Boolean draw){
        if (selectedBoats.size() > 0){
            firstTime = false;
        }
        if (!firstTime) {
            drawDistanceLine = draw;
            if (!drawDistanceLine) {
                removeDistanceLines();
            }
            int counter = 0;
            for (BoatDisplay displayBoat : selectedBoats) {
                if (selectedBoats.size() == 1) {
                    distanceLine.setFirstBoat(displayBoat.getBoat());
                    distanceLine.setSecondBoat(displayBoat.getBoat());
                }
                if (counter == 0) {
                    distanceLine.setFirstBoat(displayBoat.getBoat());
                }
                if (counter == 1) {
                    distanceLine.setSecondBoat(displayBoat.getBoat());
                }
                counter += 1;
            }
            updateDistanceMark();
        }
    }

    /**
     * draws a wind arrow on the course view
     */
    public void drawWindArrow() {

        windCircle = controller.getWindCircle();
        AnchorPane canvasAnchor = controller.getCanvasAnchor();

        if (!canvasAnchor.getChildren().contains(windArrow)){
            windArrow = raceView.drawWindArrow();
            canvasAnchor.setTopAnchor(windArrow, WIND_ARROW_Y_PADDING);
            canvasAnchor.setRightAnchor(windArrow, WIND_ARROW_X_PADDING);

            windCircle.setRadius(25);
            canvasAnchor.setTopAnchor(windCircle, WIND_CIRCLE_Y_PADDING);
            canvasAnchor.setRightAnchor(windCircle, WIND_CIRCLE_X_PADDING);

            windCircle.setId("windCircle");
            canvasAnchor.getChildren().add(windArrow);
        }
    }

    /**
     * checks if wind arrow needs updating
     */
    public void updateWindArrow() {
        windArrow.setVisible(true);
        double speed = race.getCourse().getTrueWindSpeed();
        int colorNum = calculateWindColor(speed);

        if(windArrow.getStroke().hashCode() != WIND_COLORS.get(colorNum).hashCode()){
            updateWindArrowColor(colorNum);
        }

        updateWindArrowAngle();
        controller.lblWindSpeed.setText(String.format("%.2fkn", speed));
    }

    /**
     * updates angle of wind arrow
     */
    public void updateWindArrowAngle() {
        double windDirection = (float)race.getCourse().getWindDirection();
        windArrow.setRotate(180 + windDirection + selectionController.getRotationOffset());
    }

    /**
     * calculates the number of the colour in the array best fit to wind speed
     * @param speed True Wind Speed
     * @return an int that relates to the corresponding color
     */
    public int calculateWindColor(double speed) {
        int colorNum = (int) ((speed - 15) / 5) + 1;
        if(speed > 45) colorNum = 7;
        if (speed < 15) colorNum = 1;
        return colorNum;
    }

    /**
     * updates the colour of the wind arrow with a transition
     * @param colorNum the num for related color value
     */
    public void updateWindArrowColor(int colorNum){
        double scale = 0.6;
        if(colorNum > prevWindColorNum) scale = 1.4;

        ScaleTransition st = new ScaleTransition(Duration.millis(100), windArrow);
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(scale);
        st.setToY(scale);
        st.setAutoReverse(true);
        st.setInterpolator(Interpolator.EASE_OUT);
        st.setCycleCount(2);

        windArrow.setStroke(WIND_COLORS.get(colorNum));
        prevWindColorNum = colorNum;

        if(!windTransitionPlaying){
            st.play();
            windTransitionPlaying = true;
            st.setOnFinished(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent AE){
                    windTransitionPlaying = false;
                }});
        }
    }

    /**
     * updates distance between two boats or the boat selected and mark it is heading to
     */
    private void updateDistanceMark(){
        Boat boat1 = distanceLine.getFirstBoat();
        Boat boat2 = distanceLine.getSecondBoat();
        int index = -1;
        ArrayList<CompoundMark> raceOrder = race.getCourse().getCourseOrder();
        if (boat1 != boat2) {
            if (boat1.getLeg() == boat2.getLeg()) {
                index = boat1.getLeg();
            } else {
                if (drawDistanceLine){
                    controller.setUserHelpLabel("Distance line will display when boats are on the same leg");
                }
            }
        } else {
            index = boat1.getLeg();
        }
        if (index < raceOrder.size() && index >= 0) {
            CompoundMark nextMark = raceOrder.get(index);
            distanceLine.setMark(nextMark);
        }
    }

    public BoatDisplay getCurrentUserBoatDisplay() {
        return currentUserBoatDisplay;
    }
}

