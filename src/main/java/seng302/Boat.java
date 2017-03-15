package seng302;

import javafx.scene.shape.Shape;

/**
 * Created by mjt169 on 6/03/17.
 * Class to encapsulate properties associated with a boat.
 */

public class Boat {

    private String name;
    private double speed;
    private int finishingPlace;
    private Shape icon;

    public Boat(String name, double speed){
        this.name = name;
        this.speed = speed;
    }

    public String getName(){
        return this.name;
    }

    public double getSpeed(){
        return this.speed;
    }

    public int getFinishingPlace(){
        return this.finishingPlace;
    }

    public void setFinishingPlace(int place){
        this.finishingPlace = place;
    }

    public Shape getIcon() {
        return icon;
    }

    public void setIcon(Shape icon) {
        this.icon = icon;
    }
}
