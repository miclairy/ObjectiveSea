package seng302;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by mjt169 on 6/03/17.
 * Edited by cjd137 on 7/09/17.
 * Collection of methods for reading in data from files
 */

public class RaceVisionFileReader {

    private static final String DATA_PATH = "data/";
    private static final String STARTERS_FILE = "starters.txt";
    private static final String COURSE_FILE = "course.txt";

    public static ArrayList<Mark> importMarks() {
        ArrayList<Mark> marks = new ArrayList<>();
        String filePath = DATA_PATH + COURSE_FILE;

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            
            String line = br.readLine();
            while (line != null){
                marks.add(new Mark(line));
                line = br.readLine();
            }

        } catch (FileNotFoundException e) {
            System.err.printf("Course ile could not be found at %s\n", filePath);
        } catch (IOException e) {
            System.err.printf("Error reading course file. Check it is in the correct format.");
        }

        return marks;
    }
    
    public static ArrayList<Boat> importStarters(){
        ArrayList<Boat> starters = new ArrayList<>();
        String filePath = DATA_PATH + STARTERS_FILE;

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            ArrayList<Boat> allBoats = new ArrayList<>();

            String line = br.readLine();
            while (line != null){
                allBoats.add(new Boat(line));
                line = br.readLine();
            }

            Random ran = new Random();
            for (int i = 0; i < Config.NUM_BOATS_IN_RACE; i++){
                starters.add(allBoats.remove(ran.nextInt(allBoats.size())));
            }

        } catch (FileNotFoundException e) {
            System.err.printf("Starters file could not be found at %s\n", filePath);
        } catch (IOException e) {
            System.err.printf("Error reading starters file. Check it is in the correct format.");
        }

        return starters;
    }
}
