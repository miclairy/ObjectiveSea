package seng302;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by cjd137 on 7/03/17.
 * Class to obtain the course file with the mark locations.
 */

public class Course {
	
    private static final String DATA_PATH = "data/";
    private static final String STARTERS_FILE = "course.txt";

    public static ArrayList<Mark> importMarks(){
        ArrayList<Mark> marks = new ArrayList<>();
        String filePath = DATA_PATH + STARTERS_FILE;

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            
            String line = br.readLine();
            while (line != null){
                marks.add(new Mark(line));
                line = br.readLine();
            }

        } catch (FileNotFoundException e) {
            System.err.printf("Starter file could not be found at %s\n", filePath);
        } catch (IOException e) {
            System.err.printf("Error reading course file. Check it is in the correct format.");
        }

        return marks;
    }
}
