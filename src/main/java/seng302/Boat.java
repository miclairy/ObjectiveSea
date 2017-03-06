package seng302;

/**
 * Created by mjt169 on 6/03/17.
 * Class to encapsulate properties associated with a boat.
 */

public class Boat {

    private String name;
    private int finishingPlace;

    public Boat(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public int getFinishingPlace(){
        return this.finishingPlace;
    }

    public void setFinishingPlace(int place){
        this.finishingPlace = place;
    }
}
