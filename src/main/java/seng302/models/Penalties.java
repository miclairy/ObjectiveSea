package seng302.models;

/**
 * Created by cjd137 on 21/07/17.
 */
public class Penalties {
    
    private void boatCollision(Boat boatCollider, Boat boatCollidee) {
        boatCollider.addDamage(20);
        boatCollidee.addDamage(5);
    }

    private void boatPenalty(Boat boat) {
        boat.addPenalty(boat.getCurrentSpeed()/0.02); //lose two boat lengths of time
    }

    private void markCollision(Boat boatCollider) {
        boatCollider.addDamage(5);
    }

    private void boundaryPenalty(Boat boat) {
        boat.addPenalty(boat.getCurrentSpeed()/0.02);
    }

    private void earlyStartPenalty(Boat boat) {
        boat.addPenalty(boat.getCurrentSpeed()/0.02);
    }

}
