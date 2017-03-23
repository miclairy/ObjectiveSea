package seng302;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.util.*;

/**
 * Created on 6/03/17.
 * Class to manage the output display.
 */

public class Display extends Thread{

    private Race race;
    private Group root;
    private final ArrayList<Color> COLORS = new ArrayList<>((Arrays.asList(Color.WHITE, Color.web("#A0D468"), Color.web("#FC6E51"),
            Color.web("#FFCE54"), Color.web("#48CFAD"), Color.web("#4FC1E9"), Color.web("#656D78"))));
    private Polygon boundary;

    public Display(Group root, Race race) {
        this.root = root;
        this.race = race;
        race.setEvents();
        drawCourse();
        drawBoatAnnotations();
        drawBoats();

    }


    public void run(){
        double timeIncrement = 0.000277778; //hours = 1 second
        boolean finished = false;
        while (!finished){
            finished = true;
            for (Boat boat : race.getCompetitors()){
                boat.updateLocation(timeIncrement, race.getCourse());
                if (!boat.isFinished()){
                    finished = false;
                }
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    redrawBoats();

                    Controller.updatePlacings();
                    redrawCourse();
                }
            });
            try {
                Thread.sleep(50); //speed up multiple of 2
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Draws all of the marks from the course
     */
    private void drawCourse(){
        drawBoundary();
        drawMarks();
        drawWindArrow();
    }

    public void drawMarks(){
        DropShadow ds = new DropShadow();
        ds.setOffsetY(0.0f);
        ds.setOffsetX(0.0f);
        ds.setColor(Color.web("#6db1b7"));
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
                    circle.setFill(Color.WHITE);
                    circle.setStroke(Color.web("#cdfaf4"));
                    circle.strokeWidthProperty().set(2.0);
                    circle.setEffect(ds);
                    root.getChildren().add(circle);
                    gate.addIcon(circle);
                }
            }else{
                CartesianPoint point = DisplayUtils.convertFromLatLon(mark.getLat(), mark.getLon());
                Circle circle = new Circle(point.getX(), point.getY(), 4f);
                circle.setFill(Color.WHITE);
                circle.setStroke(Color.web("#cdfaf4"));
                circle.strokeWidthProperty().set(2.0);
                circle.setEffect(ds);
                root.getChildren().add(circle);
                mark.addIcon(circle);
            }
        }
    }

    public void drawBoundary(){
        DropShadow ds = new DropShadow();
        ds.setOffsetY(0.0f);
        ds.setOffsetX(0.0f);
        ds.setSpread(0.4);
        ds.setColor(Color.web("#6db1b7"));
        boundary = new Polygon();
        for(Coordinate coord : race.getCourse().getBoundary()){
            CartesianPoint point = DisplayUtils.convertFromLatLon(coord.getLat(), coord.getLon());
            boundary.getPoints().add(point.getX());
            boundary.getPoints().add(point.getY());
        }
        boundary.setId("boundary");
        boundary.setEffect(ds);
        boundary.setFill(Color.web("#88e6ef"));
        boundary.setStroke(Color.web("#407a97"));
        root.getChildren().add(boundary);
        boundary.toBack();
    }

    public void drawWindArrow(){
        double windDirection = race.getCourse().getWindDirection();
        ImageView imv = new ImageView();
        Image windArrow = new Image("graphics/arrow.png");
        imv.setImage(windArrow);
        imv.setFitHeight(40);
        imv.setFitWidth(40);
        imv.setX(imv.getX() + 15);
        imv.setY(imv.getY() + 15);
        imv.setRotate(windDirection);
        root.getChildren().add(imv);
    }
    /**
     * Draws the boat icons and fills them with colour
     */

    private void drawBoats(){
        int i = 1;
        for (Boat boat : race.getCompetitors()) {
            Circle boatImage = new Circle(Math.abs(boat.getCurrentLat() * i), Math.abs(boat.getCurrentLon()) , 5.0f);
            boatImage.setFill(COLORS.get(i));
            boatImage.setStroke(Color.WHITE);
            root.getChildren().add(boatImage);
            boat.setIcon(boatImage);
            i++;
        }
    }

    /**
     * Update each boat icon's position on screen, translating from the boat's latlon to cartesian coordinates
     */
    private void redrawBoats(){
        for (Boat boat : race.getCompetitors()) {
            redrawBoatAnnotations(boat);
            CartesianPoint point = DisplayUtils.convertFromLatLon(boat.getCurrentLat(), boat.getCurrentLon());
            boat.getIcon().relocate(point.getX(), point.getY());
            boat.getIcon().toFront();
        }
    }


    public void drawBoatAnnotations(){
        for(Boat boat : race.getCompetitors()){
            CartesianPoint point = DisplayUtils.convertFromLatLon(boat.getCurrentLat(), boat.getCurrentLon());
            Text annotation = new Text();
            annotation.setText(boat.getNickName().toString());
            annotation.setId("annotation");
            annotation.setX(point.getX() + 10);
            annotation.setY(point.getY() + 15);
            boat.setAnnotation(annotation);
            root.getChildren().add(annotation);
            drawBoatWake(boat);
        }
    }

    public void drawBoatWake(Boat boat){

        Polygon wake = new Polygon();
        wake.getPoints().addAll(new Double[]{
                5.0, 20.0,
                0.0, 0.0,
                10.0, 0.0
                });

        root.getChildren().add(wake);
        boat.setWake(wake);
        //wake.setFill(Color.web("#84daff"));
    }

    public void redrawBoatAnnotations(Boat boat){
        double adjustX = 10;
        CartesianPoint point = DisplayUtils.convertFromLatLon(boat.getCurrentLat(), boat.getCurrentLon());
        boat.getAnnotation().relocate((point.getX() + 10), point.getY() + 15);
        redrawWake(boat, point);
        if(DisplayUtils.checkBounds(boat.getAnnotation())){
            adjustX -= boat.getAnnotation().getBoundsInParent().getWidth();
            boat.getAnnotation().relocate((point.getX() + adjustX), point.getY() + 15);
        }
    }

    private void redrawWake(Boat boat, CartesianPoint point){
        boat.getWake().getTransforms().clear();
        double scale = boat.getSpeed() * 1/50;
        boat.getWake().setScaleY(scale);
        boat.getWake().setScaleX(scale);
        double wakeHeight = boat.getWake().getLayoutBounds().getHeight();
        boat.getWake().setTranslateX(point.getX()); //+ (scale * wakeHeight) * 0.025);
        boat.getWake().setTranslateY(point.getY() - (scale * wakeHeight / 2) - 4); //- (scale * wakeHeight) * 0.725);
        boat.getWake().getTransforms().add(new Rotate(boat.getHeading() -180, 5.0, 20.0));

        boat.getWake().toFront();

    }

    private void redrawCourse(){
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
        redrawBoundary();
    }

    public void redrawBoundary(){
        root.getChildren().remove(boundary);
        drawBoundary();
    }
}

