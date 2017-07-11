package seng302.controllers;

import com.sun.javafx.UnmodifiableArrayList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import seng302.models.Mark;
import seng302.utilities.DisplayUtils;
import seng302.views.BoatDisplay;

import java.util.*;

import static java.lang.Math.abs;
import static seng302.utilities.DisplayUtils.DRAG_TOLERANCE;
import static seng302.utilities.DisplayUtils.isOutsideBounds;
import static seng302.utilities.DisplayUtils.zoomLevel;

/**
 * Created by clbmi on 10/07/2017.
 */
public class SelectionController extends Observable {

    private Group root;
    private ScoreBoardController scoreBoardController;
    private List<BoatDisplay> displayBoats;
    private Controller controller;
    private Set<BoatDisplay> selectedBoats = new HashSet<>();
    private ImageCursor boatCursor = new ImageCursor(new Image("graphics/boat-select-cursor.png"), 7, 7);
    private Mark selectedMark = null;
    private BoatDisplay trackingBoat = null;
    private boolean isTrackingPoint = false;
    private double rotationOffset = 0;
    private boolean isRotationEnabled = false;
    private boolean courseNeedsRedraw = false;

    public SelectionController(Group root, ScoreBoardController scoreBoardController, Controller controller) {
        this.root = root;
        this.scoreBoardController = scoreBoardController;
        this.controller = controller;
    }

    void trackBoat() {
        BoatDisplay selectedBoat = getTrackingBoat();
        if (selectedBoat != null) {
            if (isTrackingPoint()) {
                controller.setZoomSliderValue(1);
                setTrackingPoint(false);
            } else {
                controller.setZoomSliderValue(3);
                setTrackingPoint(true);
                setMapVisibility(false);
            }
            courseNeedsRedraw = true;
            setChanged();
            notifyObservers();
        }
    }


    void zoomTracking() {
        if (isTrackingPoint && selectedMark != null){
            DisplayUtils.moveToPoint(selectedMark.getPosition());
            courseNeedsRedraw = true;
       }
        if (isTrackingPoint && trackingBoat != null) {
            trackingBoat.getIcon().toFront();
            DisplayUtils.moveToPoint(trackingBoat.getBoat().getCurrentPosition());
            courseNeedsRedraw = true;
           if(isRotationEnabled){
                if(zoomLevel > 1){
                    rotationOffset = -trackingBoat.getBoat().getHeading();
                    updateRotation();
                }
            }
        }
        setChanged();
        notifyObservers();
    }


    /**
     * adds Event handlers to areas of the course than don't contain boat, so deselect of boat
     * can be detected
     */
    void addDeselectEvents(Polygon boundary){
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

        }
        setChanged();
        notifyObservers();
    }

    /**
     * adds event handler to marks so we can detet if selected by the user
     * @param mark
     */
    void addMarkSelectionHandlers(Mark mark){

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
                courseNeedsRedraw = true;
                setMapVisibility(false);
            }else{
                controller.setZoomSliderValue(1);
                selectedMark = null;
                isTrackingPoint = false;

                DisplayUtils.resetOffsets();
                courseNeedsRedraw = true;
                setMapVisibility(true);

            }
            setChanged();
            notifyObservers();
        });

        circle.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            root.setCursor(boatCursor);
        });

        circle.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            root.setCursor(Cursor.DEFAULT);
        });
    }

    /**
     * updates the rotation of the canvas to the rotationOffset class variable
     */
    private void updateRotation(){
        root.getTransforms().clear();
        root.getTransforms().add(new Rotate(rotationOffset, controller.getCanvasWidth()/2, controller.getCanvasHeight()/2));
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
                boat.getSeries().getNode().setOpacity(1);
            }
        }
    }

    /**
     * Assigns drag listeners to Drag handle objects
     * @param dragHandle the object that is dragged
     * @param boatDisplay the boat the object is attached to
     */
    void makeDraggable(Node dragHandle, BoatDisplay boatDisplay){
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
     * adds event hadnlers so we can detect if the user has selected a boat
     * @param boat
     */
    void addBoatSelectionHandler(BoatDisplay boat){
        Shape boatImage = boat.getIcon();
        boatImage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            trackingBoat = boat;
            if (selectedBoats.isEmpty() || event.isShiftDown()) {
                if (selectedBoats.contains(boat)){
                    selectedBoats.remove(boat);
                } else {
                    selectedBoats.add(boat);

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
            setChanged();
            notifyObservers();

        });


        boatImage.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            root.setCursor(boatCursor);
        });

        boatImage.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            root.setCursor(Cursor.DEFAULT);
        });
    }


    public Set<BoatDisplay> getSelectedBoats() {
        return Collections.unmodifiableSet(selectedBoats);
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

    public boolean isRotationEnabled() {
        return isRotationEnabled;
    }

    public boolean isCourseNeedsRedraw() {
        return courseNeedsRedraw;
    }

    public void setRotationOffset(double rotationOffset) {
        this.rotationOffset = rotationOffset;
    }

    @FXML
    public void zoomToggle(boolean zoomed){
        isRotationEnabled = zoomed;
        rotationOffset = 0;
        updateRotation();
    }

    public void setDisplayBoats(List<BoatDisplay> displayBoats) {
        this.displayBoats = displayBoats;
    }
}
