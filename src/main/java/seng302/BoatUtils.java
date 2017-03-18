package seng302;

import java.util.Collection;
import java.util.Comparator;

/**
 * Created by mjt169 on 6/03/17.
 * A collection of utility functions to simplify working with boats
 */
public class BoatUtils {

    /**
     *  Comparator for sorting boats by their finishing place
     */
    public static Comparator<Boat> orderByPlacing = new Comparator<Boat>() {
        @Override
        public int compare(Boat boat1, Boat boat2) {
            return Integer.compare(boat1.getFinishingPlace(), boat2.getFinishingPlace());
        }
    };

}
