package seng302;

import java.util.ArrayList;

/**
 * Created by mjt169 on 6/03/17.
 * Class to manage the output display.
 * For now this will be simple text-based output to terminal
 */

public class Display {

    public static void printStartersList(ArrayList<String> starters) {
        System.out.println("Boats in this race:");
        for (String boat : starters) {
            System.out.println(boat);
        }
    }

}
