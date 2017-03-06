package seng302;

import java.util.ArrayList;

/**
 * Created by mjt169 on 6/03/17.
 * Class to manage the output display.
 * For now this will be simple text-based output to terminal
 */

public class Display {

    public static void printStartersList(ArrayList<Boat> starters) {
        System.out.println("Boats in this race:");
        for (Boat boat : starters) {
            System.out.println(boat.getName());
        }
    }

    public static void printFinishersList(ArrayList<Boat> finishers) {
        finishers.sort(BoatUtils.orderByPlacing);

        System.out.println("Finishing order:");
        for (Boat boat : finishers) {
            System.out.printf("%d. %s\n", boat.getFinishingPlace(), boat.getName());
        }
    }

}
