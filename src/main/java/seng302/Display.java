package seng302;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.effect.DropShadow;

import java.util.*;

/**
 * Created on 6/03/17.
 * Class to manage the output display.
 * For now this will be simple text-based output to terminal
 */

public class Display extends Thread {

    private Race race;
    private Group root;
    private final ArrayList<Color> COLORS = new ArrayList<>((Arrays.asList(Color.WHITE, Color.web("#A0D468"), Color.web("#FC6E51"),
            Color.web("#FFCE54"), Color.web("#48CFAD"), Color.web("#4FC1E9"), Color.web("#656D78"))));

    public Display(Group root, Race race) {
        this.root = root;
        this.race = race;
        race.setEvents();
        drawCourse();
        drawBoats();
    }

    @Override
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
            redrawBoats();

            try {
                this.sleep(50); //speed up multiple of 2
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * Draws all of the marks from the course
     */
    public void drawCourse(){
        DropShadow ds = new DropShadow();
        ds.setOffsetY(0.0f);
        ds.setOffsetX(0.0f);
        ds.setColor(Color.web("#89cac1"));
        for(CompoundMark mark : race.getCourse().getCourseOrder()){
            if(mark instanceof Gate){
                Gate gate  = (Gate) mark;
                ArrayList<CartesianPoint> points = new ArrayList<>();
                points.add(DisplayUtils.convertFromLatLon(gate.getEnd1Lat(), gate.getEnd1Lon()));
                points.add(DisplayUtils.convertFromLatLon(gate.getEnd2Lat(), gate.getEnd2Lon()));
                for(CartesianPoint point : points){
                    Circle circle = new Circle(point.getX(), point.getY(), 4f);
                    circle.setFill(Color.WHITE);
                    circle.setStroke(Color.web("#cdfaf4"));
                    circle.strokeWidthProperty().set(2.0);
                    circle.setEffect(ds);
                    if(gate.isStart() | gate.isFinish()){
                        Line line = new Line(point.getX(),point.getY(), points.get(1).getX(), points.get(1).getY());
                        line.setStroke(Color.web("#70aaa2"));
                        root.getChildren().add(line);
                    }
                    root.getChildren().add(circle);
                }
            }else{
                CartesianPoint point = DisplayUtils.convertFromLatLon(mark.getLat(), mark.getLon());
                Circle circle = new Circle(point.getX(), point.getY(), 4f);
                circle.setFill(Color.WHITE);
                circle.setStroke(Color.web("#cdfaf4"));
                circle.strokeWidthProperty().set(2.0);
                circle.setEffect(ds);
                root.getChildren().add(circle);
            }
            drawWindArrow();
        }
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
     * Draws them boat icons and fills them with colour
     */

    public void drawBoats(){
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
    public void redrawBoats(){
        for (Boat boat : race.getCompetitors()) {
            CartesianPoint point = DisplayUtils.convertFromLatLon(boat.getCurrentLat(), boat.getCurrentLon());
            boat.getIcon().relocate(point.getX(), point.getY());
        }
    }
}

