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

    }

    private void markCollision() {

    }

    private void markPenalty() {

    }

    private void boundaryPenalty() {

    }

    private void earlyStartPenalty() {

    }

}
