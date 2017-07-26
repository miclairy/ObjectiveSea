package seng302.models;

/**
 * Created by cjd137 on 21/07/17.
 * A class for assigning penalties to boats
 */
public class Penalties {

    /**
     * When a boat collides with another boat, both get damaged (collider more than collidee)
     * @param boatCollider The boat in the wrong
     * @param boatCollidee The boat that was hit
     */
    public void boatCollision(Boat boatCollider, Boat boatCollidee) {
        boatCollider.addDamage(20);
        boatCollidee.addDamage(5);
    }

    /**
     * The accrued boat penalty for obstructing another boat, the penalty is minus two boat lengths
     * @param boat the bad boat
     */
    public void boatPenalty(Boat boat) {
        boat.addPenalty(boat.getCurrentSpeed()/0.02); //lose two boat lengths of time
    }

    /**
     * When a boat collides with a mark
     * @param boatCollider the boat which hit the mark
     */
    public void markCollision(Boat boatCollider) {
        boatCollider.addDamage(5);
    }

    /**
     * When a boat goes out of bounds, the penalty is minus two boat lengths
     * @param boat the bad boat
     */
    public void boundaryPenalty(Boat boat) {
        boat.addPenalty(boat.getCurrentSpeed()/0.02);
    }

    /**
     * A penalty for starting a race too early, the penalty is minus two boat lengths
     * @param boat the bad boat
     */
    public void earlyStartPenalty(Boat boat) {
        boat.addPenalty(boat.getCurrentSpeed()/0.02);
    }

}
