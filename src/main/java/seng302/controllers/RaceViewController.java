package seng302.controllers;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Path;

import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import seng302.data.BoatStatus;
import seng302.data.StartTimingStatus;
import seng302.utilities.DisplayUtils;
import seng302.utilities.PolarReader;
import seng302.utilities.TimeUtils;
import seng302.models.*;
import seng302.views.BoatDisplay;
import seng302.views.RaceView;

import java.text.Annotation;
import java.util.*;

import static java.lang.Math.abs;
import static seng302.data.RaceStatus.STARTED;
import static seng302.data.RaceStatus.TERMINATED;
import static seng302.utilities.DisplayUtils.DRAG_TOLERANCE;
import static seng302.utilities.DisplayUtils.isOutsideBounds;
import static seng302.utilities.DisplayUtils.zoomLevel;

/**
 * Created on 6/03/17.
 * Class to manage the output display.
 */

public class RaceViewController extends AnimationTimer implements Observer {

    private enum AnnotationLevel {
        NO_ANNOTATION, IMPORTANT_ANNOTATIONS, ALL_ANNOTATIONS
    }
    private final double WAKE_SCALE_FACTOR = 17;
    private final double SOG_SCALE_FACTOR = 200.0;
    private final int ANNOTATION_HANDLE_OFFSET = 8;



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
    private Set<BoatDisplay> selectedBoats = new HashSet<>();
    private DistanceLine distanceLine = new DistanceLine();
    private ImageCursor boatCursor = new ImageCursor(new Image("graphics/boat-select-cursor.png"), 7, 7);
    private boolean drawDistanceLine = false;
    private boolean firstTime = true;

    private BoatDisplay trackingBoat = null;
    private Mark selectedMark = null;
    private boolean isTrackingPoint = false;
    private double rotationOffset = 0;
    private boolean isRotationEnabled = false;
    private int flickercounter = 0;

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
        if (drawDistanceLine) redrawDistanceLines();
        if (isTrackingPoint && selectedMark != null){
            DisplayUtils.moveToPoint(selectedMark.getPosition());
            redrawCourse();
        }
        if (isTrackingPoint && trackingBoat != null) {
            trackingBoat.getIcon().toFront();
            DisplayUtils.moveToPoint(trackingBoat.getBoat().getCurrentPosition());
            redrawCourse();
            if(isRotationEnabled){
                if(zoomLevel > 1){
                    rotationOffset = -trackingBoat.getBoat().getHeading();
                    updateRotation();
                }
            }
        }
        for (BoatDisplay boat: displayBoats) {
            CanvasCoordinate point = DisplayUtils.convertFromLatLon(boat.getBoat().getCurrentLat(), boat.getBoat().getCurrentLon());
            moveBoat(boat, point);
            moveWake(boat, point);
            if(race.getCourse().getCourseOrder().get(boat.getBoat().getLeg()).isStartLine()){
            if(flickercounter % 300 == 0){
                boat.getStartTiming(race);}
            } else {
                boat.getBoat().setTimeStatus(StartTimingStatus.INRACE);
            }
            moveSOGVector(boat);
            moveVMGVector(boat);
            if(race.getRaceStatus() == STARTED) {
                addToBoatPath(boat, point);
            }
            moveBoatAnnotation(boat.getAnnotation(), point, boat);
            if(scoreBoardController.areVectorsSelected()){
                boat.showVectors();
            }else{
                boat.hideVectors();
            }
            if (scoreBoardController.isLayLinesSelected()){
                boat.getLaylines().removeDrawnLines(root);
                if (selectedBoats.contains(boat)) {
                    createLayline(boat);
                }
            }else{
                boat.getLaylines().removeDrawnLines(root);
            }
        }
        if (courseNeedsRedraw) redrawCourse();
        changeAnnotations(currentAnnotationsLevel, true);
        controller.updatePlacings();
        controller.setWindDirection();
        flickercounter++;
        distanceLine.getAnnotation().toFront();
    }

    /**
     * Draws and sets up BoatDisplay objects onscreen
     */
    public void initializeBoats() {
        PolarTable polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
        for (Boat boat : race.getCompetitors()){
            BoatDisplay displayBoat = new BoatDisplay(boat, polarTable);
            boat.addObserver(displayBoat);
            scoreBoardController.addBoatToSparkLine(displayBoat.getSeries());
            raceView.assignColor(displayBoat);
            displayBoats.add(displayBoat);
            drawBoat(displayBoat);
            addBoatSelectionHandler(displayBoat);

            Circle grabHandle = new Circle(5);
            grabHandle.setId("annoGrabHandle");
            grabHandle.setCenterX(0);
            grabHandle.setCenterY(0);

            displayBoat.setAnnoGrabHandle(grabHandle);
            root.getChildren().add(grabHandle);
            makeDraggable(grabHandle, displayBoat);
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
        boatImage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            trackingBoat = boat;
            if (selectedBoats.isEmpty() || event.isShiftDown()) {
                if (selectedBoats.contains(boat)){
                    selectedBoats.remove(boat);
                } else {
                    selectedBoats.add(boat);
                    updateDistanceLine(scoreBoardController.isDistanceLineSelected());
                }

            } else {
                selectedBoats.clear();
                selectedBoats.add(boat);
            }
            if (selectedBoats.isEmpty()){
                deselectBoat();
            } else {
                setBoatFocus();
            }
            selectedMark = null;

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
        drawSOGVector(boat);
        drawVMGVector(boat);
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
            boat.getLaylines().removeDrawnLines(root);
            selectedBoats.remove(boat);
            boat.getSeries().getNode().setOpacity(1);
            trackingBoat = null;
            selectedMark = null;
            isTrackingPoint = false;
            rotationOffset =0;
            updateRotation();
            updateDistanceLine(false);
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
                trackingBoat = null;
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
     * Assigns drag listeners to Drag handle objects
     * @param dragHandle the object that is dragged
     * @param boatDisplay the boat the object is attached to
     */
    public void makeDraggable(Node dragHandle, BoatDisplay boatDisplay){
        dragHandle.requestFocus();

        root.onKeyPressedProperty().bind(dragHandle.onKeyPressedProperty());

        dragHandle.setOnMouseDragged(me -> {
            root.setCursor(Cursor.CLOSED_HAND);
            VBox annotation = boatDisplay.getAnnotation();

            if(zoomLevel > 1 || (zoomLevel <=1 && !isOutsideBounds(annotation))){
                //inside bounds
                if(abs(me.getX() - Delta.x) < DRAG_TOLERANCE &&
                        abs(me.getY() - Delta.y) < DRAG_TOLERANCE) {
                    double scaledChangeX = ((me.getX() - Delta.x)/zoomLevel);
                    double scaledChangeY = ((me.getY() - Delta.y)/zoomLevel);
                    boatDisplay.setAnnoOffsetX(boatDisplay.getAnnoOffsetX() + scaledChangeX);
                    boatDisplay.setAnnoOffsetY(boatDisplay.getAnnoOffsetY() + scaledChangeY);
                }
            }
            Delta.x = me.getX();
            Delta.y = me.getY();

            DisplayUtils.externalDragEvent = true;
        });

        dragHandle.setOnMouseExited(me ->{
            root.setCursor(Cursor.DEFAULT);
        });

        dragHandle.setOnMouseEntered(me ->{
            root.setCursor(Cursor.OPEN_HAND);
        });

        dragHandle.setOnMousePressed(me ->{
            root.setCursor(Cursor.CLOSED_HAND);
            dragHandle.setScaleX(1.2);
            dragHandle.setScaleY(1.2);
            boatDisplay.getAnnotation().setScaleX(1.2);
            boatDisplay.getAnnotation().setScaleY(1.2);
        });

        dragHandle.setOnMouseReleased(me ->{
            root.setCursor(Cursor.OPEN_HAND);

            dragHandle.setScaleX(1);
            dragHandle.setScaleY(1);
            boatDisplay.getAnnotation().setScaleX(1);
            boatDisplay.getAnnotation().setScaleY(1);
        });
    }

    private static class Delta {
        public static double x;
        public static double y;
    }

    /**
     * Move's the annotation to where the boat is now.
     * @param annotation the annotation to be moved
     * @param point where the boat has moved to
     * @param point where the boat has moved to
     */
    private void moveBoatAnnotation(VBox annotation, CanvasCoordinate point, BoatDisplay boatDisplay){
        if(isRotationEnabled){
            annotation.getTransforms().clear();
            annotation.getTransforms().add(new Rotate(-rotationOffset, annotation.getWidth()/2, annotation.getHeight()/2));
        }

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
                        annotations.add(displayBoat.getTimeSinceLastMark(currTime));
                    }
                    if(scoreBoardController.isEstSelected()){
                        annotations.add(displayBoat.getTimeToNextMark(displayBoat.getBoat().getTimeAtNextMark(), currTime));
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
                            createLayline(displayBoat);
                        }
                    }
                    drawBoatAnnotation(displayBoat, annotations);
                } else if (level == AnnotationLevel.ALL_ANNOTATIONS) {
                    displayBoat.annoGrabHandle.setVisible(true);
                    annotations.clear();
                    annotations.add(boatName);
                    annotations.add(displayBoat.getSpeed());
                    annotations.add(displayBoat.getTimeSinceLastMark(currTime));
                    annotations.add(displayBoat.getTimeToNextMark(displayBoat.getBoat().getTimeAtNextMark(), currTime));
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
        double VMG = boat.getBoat().calculateVMG(course);
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
        double VMG = boat.getBoat().calculateVMG(course);
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
        double scale = boat.getBoat().getSpeed() / SOG_SCALE_FACTOR;
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
        double scale = boat.getBoat().getSpeed() / SOG_SCALE_FACTOR;
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
    private void createLayline(BoatDisplay boatDisplay){
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

                laylines.calculateLaylineAngle(course.getWindDirection(), lastMark, nextMark, boatDisplay.getPolarTable());
                if (laylines.shouldDraw()) {
                    Line layline1 = raceView.drawLayline(laylines.getAngle1(), mark1Coord, boatDisplay.getColor());
                    Line layline2 = raceView.drawLayline(laylines.getAngle2(), mark2Coord, boatDisplay.getColor());

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
        scale *= zoomLevel;
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
     * Redraws a raceline on the visual
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
            icon.setScaleX(zoomLevel);
            icon.setScaleY(zoomLevel);
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
            if(isRotationEnabled){
                annotation.getTransforms().add(new Rotate(-rotationOffset));
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

    private void setBoatFocus(){
        scoreBoardController.btnTrack.setVisible(true);
        for (BoatDisplay boatDisplay : selectedBoats) {
            boatDisplay.getIcon().toFront();
        }
        for(BoatDisplay boat : displayBoats){
            if(!selectedBoats.contains(boat)){
                boat.unFocus();
                boat.getLaylines().removeDrawnLines(root);
                boat.getSeries().getNode().setOpacity(0.2);
            }else{
                boat.focus();
                createLayline(boat);
                boat.getSeries().getNode().setOpacity(1);
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


    /**
     *  Going to be used to toggle the zoom level of the map (currently only two levels will exist, on or off).
     */
    @FXML
    public void zoomToggle(boolean zoomed){
        isRotationEnabled = zoomed;
        rotationOffset = 0;
        updateRotation();
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

    public BoatDisplay getTrackingBoat() {
        return trackingBoat;
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
}

