package seng302.data;

/**
 * Created by lga50 on 17/07/17.
 *
 */
public enum BoatAction {

    BOAT_AUTOPILOT(1), SAILS_IN(2), SAILS_OUT(3), TACK_GYBE(4), UPWIND(5), DOWNWIND(6);

    private final int type;

    BoatAction(int type){
        this.type = type;
    }

    public int getType(){
        return this.type;
    }
}
