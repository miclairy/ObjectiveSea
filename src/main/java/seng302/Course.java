package seng302;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by cjd137 on 7/03/17.
 * Class to figure out the mark locations and degrees.
 */

public class Course {

    private ArrayList<Mark> marks;

    public Course(ArrayList<Mark> marks) {
        this.marks = marks;
    }

    public ArrayList<Mark> getMarks(){
        return this.marks;
    }
	

}
