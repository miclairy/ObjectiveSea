package seng302;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by mjt169 on 6/03/17.
 * A set of configuration options used throughout the software
 */
public class Config {

    private static final String CONFIG_PATH = "data/config.txt";
    public static int NUM_BOATS_IN_RACE;
    public static int TIME_SCALE;

    public static void initializeConfig(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(CONFIG_PATH));
            String line = br.readLine();
            while (line != null){
                StringTokenizer st = new StringTokenizer(line);

                String token = st.nextToken("=");
                switch(token) {
                    case "NUMBOATS":
                        NUM_BOATS_IN_RACE = Integer.parseInt(st.nextToken());
                        break;
                    case "TIMESCALE":
                        TIME_SCALE = (int)(Double.parseDouble(st.nextToken()) * 60000); //convert mins to milleseconds
                        break;
                    default:
                        throw new IOException("Invalid Token.");
                }

                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            System.err.printf("Config file could not be found at %s\n", CONFIG_PATH);
        } catch (IOException e) {
            System.err.printf("Error reading config file. Check it is in the correct format: %s", e);
        }
    }
}
