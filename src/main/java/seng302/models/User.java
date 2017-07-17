package seng302.models;

import java.util.UUID;
import seng302.views.*;

/**
 * Class to store attributes of the current user
 */
public class User {

    private BoatDisplay boat;
    private UUID ID;

    public User(){

    }

    public BoatDisplay getBoat() {
        return boat;
    }

    public void setBoat(BoatDisplay boat) {
        this.boat = boat;
    }

    public UUID getID() {
        return ID;
    }

    public void setID(UUID ID) {
        this.ID = ID;
    }

}
