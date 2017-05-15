package seng302.utilities;
import javafx.util.Pair;
import seng302.data.RaceVisionXMLParser;
import seng302.models.Polars;

import java.io.*;
import java.util.ArrayList;


/**
 * Created by gemma on 12/04/2017.
 */
public class PolarReader {
    private static final String POLAR_FILE_LOCATION = "/defaultFiles/boatPolars.txt";

    public static ArrayList<Polars> getPolars() {
        return polars;
    }

    private static ArrayList<Polars> polars = new ArrayList<>();


    public static void readPolars() {
        String thisLine;
        if (polars.isEmpty()){
            try {
                BufferedReader br;
                br = new BufferedReader(new InputStreamReader(PolarReader.class.getResourceAsStream(POLAR_FILE_LOCATION)));
                String titleLine = br.readLine(); // Reads in first line of titles
                String[] titles = titleLine.split(",");

                while ((thisLine = br.readLine()) != null) {
                    String[] line = thisLine.split(",");
                    int TWS = Integer.parseInt(line[0]);
                    Polars tempPolar = new Polars(TWS);
                    for(int i = 1; i < line.length; i+=2){
                        double x = Double.parseDouble(line[i]); //angle
                        double y = Double.parseDouble(line[i+1]); //speed
                        if(titles[i].equals("UpTwa")){
                            tempPolar.setUpWindOptimum(new Pair<>(x,y));
                        } else if(titles[i].equals("DnTwa")){
                            tempPolar.setDownWindOptimum(new Pair<>(x,y));
                        } else {
                            tempPolar.addTWAandBSP(x,y);
                        }
                    }
                    polars.add(tempPolar);

                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

}

