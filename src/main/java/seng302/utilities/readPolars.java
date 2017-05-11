package seng302.utilities;
import javafx.util.Pair;
import seng302.data.RaceVisionXMLParser;
import seng302.models.Polars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;


/**
 * Created by gemma on 12/04/2017.
 */
public class readPolars {
    private static final String polarFile = "boatPolars.txt";
    private static final String PolarFileLocation = "/defaultFiles/boatPolars.txt";

    public static ArrayList<Polars> getPolars() {
        return polars;
    }

    private static ArrayList<Polars> polars = new ArrayList<>();


    public static void polars() throws Exception{
        String thisLine;
        if (polars.isEmpty()){
        try {
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(polarFile));
            } catch (FileNotFoundException e){
                RaceVisionXMLParser.exportResource(PolarFileLocation, polarFile);
                br = new BufferedReader(new FileReader(polarFile));
            }
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
        } catch(Exception e) {
            e.printStackTrace();
        }
    }}

}

