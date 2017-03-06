package seng302;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
/**
 * Created by mjt169 on 6/03/17.
 * Collection of methods for reading in data from files
 */

public class RaceVisionFileReader {

    private static final String DATA_PATH = "data/";
    private static final String STARTERS_FILE = "starters.txt";

    public static ArrayList<String> importStarters(){
        ArrayList<String> starters = new ArrayList<String>();
        String filePath = DATA_PATH + STARTERS_FILE;

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();
            while (line != null){
                starters.add(line);
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            System.err.printf("Starter file could not be found at %s\n", filePath);
        } catch (IOException e) {
            System.err.printf("Error reading start file. Check it is in the correct format.");
        }

        return starters;
    }
}
