package seng302.controllers;

import seng302.models.*;
import seng302.utilities.DisplayUtils;
import seng302.utilities.MathUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by mjt169 on 25/07/17.
 * Class to check for and manage collisions
 */
public class CollisionManager {

    private Double BOAT_SENSITIVITY = 16.0;
    private Double MARK_SENSITIVITY = 10.0;
    private Integer AT_FAULT_DELTA = 60;

    private Set<Collision> currentCollisions = new CopyOnWriteArraySet<>();

    public CollisionManager() {};

    /**
     * Checks all boats in the race to see if they are colliding with each other or course marks
     * Adds any collisions to a set.
     * @param race
     */
    public void checkForCollisions(Race race){
        List<Boat> boats = new ArrayList<>();
        boats.addAll(race.getCompetitors());
        for (int i = 0; i < boats.size(); i++) {
            Boat boat = boats.get(i);
            for (int j = i; j < boats.size(); j++) {
                Boat otherBoat = boats.get(j);
                checkForCollisionBetweenBoats(boat, otherBoat);
            }
            for (Mark mark : race.getCourse().getAllMarks().values()) {
                if (collisionOfBounds(boat.getCurrentPosition(), mark.getPosition(), MARK_SENSITIVITY)) {
                    Collision collision = new Collision();
                    collision.addBoat(boat.getId());
                    currentCollisions.add(collision);
                }
            }
        }
    }

    /**
     * Check whether two boats are colliding and create a Collision if necessary
     * @param boat1 the first boat to check
     * @param boat2 the second boat to check
     */
    private void checkForCollisionBetweenBoats(Boat boat1, Boat boat2) {
        if (collisionOfBounds(boat1.getCurrentPosition(), boat2.getCurrentPosition(), BOAT_SENSITIVITY)) {
            Collision collision = new Collision();
            collision.addBoat(boat1.getId());
            collision.addBoat(boat2.getId());
            if (boatHeadingTowardsOther(boat1, boat2)) {
                collision.addAtFaultBoat(boat1.getId());
            }
            if (boatHeadingTowardsOther(boat2, boat1)) {
                collision.addAtFaultBoat(boat2.getId());
            }
            currentCollisions.add(collision);
        }
    }

    /**
     * Determines whether boat1 is heading in a direction approximately towards boat2
     * @param boat1 the boat whose heading we are checking
     * @param boat2 the boat to check if we boat1 is heading towards
     */
    private boolean boatHeadingTowardsOther(Boat boat1, Boat boat2) {
        double boat1To2Heading = boat1.getCurrentPosition().headingToCoordinate(boat2.getCurrentPosition());
        return MathUtils.pointBetweenTwoAngle(boat1To2Heading, AT_FAULT_DELTA, boat1.getHeading());
    }


    /**
     * takes two circles and calculates if there is a collision
     * @param object1LatLon Coordinate of the first object
     * @param object2LatLon Coordinate of the second object
     * @return boolean of collision
     */
    private boolean collisionOfBounds(Coordinate object1LatLon, Coordinate object2LatLon, double sensitivity){
        CanvasCoordinate object1 = DisplayUtils.convertFromLatLon(object1LatLon);
        CanvasCoordinate object2 = DisplayUtils.convertFromLatLon(object2LatLon);
        double dx = object1.getX() - object2.getX();
        double dy = object2.getY() - object1.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < sensitivity;
    }

    public boolean boatIsInCollision(Boat boat) {
        for (Collision collision : currentCollisions) {
            if (collision.boatIsInCollision(boat.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean boatIsCollidingWithMark(Boat boat) {
        for (Collision collision : currentCollisions) {
            if (collision.boatIsInCollision(boat.getId())) {
                if (collision.isSingleBoatCollision()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<Collision> getCollisions() {
        return Collections.unmodifiableSet(currentCollisions);
    }

    public void removeCollision(Collision collision) {
        currentCollisions.remove(collision);
    }
}
