package seng302.utilities;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;


/**
 * Created by gemma on 12/04/2017.
 */
public class PolarReader {
    public static ArrayList<Integer> getTWS() {
        return TWS;
    }

    public static ArrayList<ArrayList<Pair<Double, Double>>> getPolars() {
        return polars;
    }

    private static ArrayList<Integer> TWS = new ArrayList<>();
    private static ArrayList<ArrayList<Pair<Double, Double>>> polars = new ArrayList<>();
    public static void polars() throws Exception{
        String thisLine = null;

        try {
            File file = new File("src\\main\\resources\\defaultFiles\\boatPolars.txt"); //C:\Users\gemma\Desktop\team-28\src\main\resources\defaultFiles\boatPolars.txt
            String fileName = file.getAbsolutePath();
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            thisLine = br.readLine();
            while ((thisLine = br.readLine()) != null) {
                String[] line = thisLine.split(",");
                ArrayList<Pair<Double, Double>> temp = new ArrayList<>();
                int tws = Integer.parseInt(line[0]);
                TWS.add(tws);
                for(int i = 1; i < line.length; i+=2){
                    double x = Double.parseDouble(line[i]);
                    double y = Double.parseDouble(line[i+1]);
                    Pair<Double,Double> pair = new Pair<>(x,y);
                    temp.add(pair);
                }
                polars.add(temp);

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}

