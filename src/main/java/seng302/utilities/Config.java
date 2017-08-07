package seng302.utilities;

import seng302.data.RaceVisionXMLParser;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Created on 6/03/17.
 * A set of configuration options used throughout the software, read from a config file at startup.
 */
public class Config {

    private static final String DEFAULT_CONFIG_PATH = "/defaultFiles/config.txt";
    private static final String EXPECTED_CONFIG_PATH = "config.txt";
    public static double MOCK_SPEED_SCALE;
    public static String SOURCE_ADDRESS;
    public static int SOURCE_PORT;
    /**
     * This function finds a config file located at DEFAULT_CONFIG_PATH and sets any properties it finds in the file.
     * Example format for property-value pair: NUMBOATS=6
     */
    public static void initializeConfig(){
        try {
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(EXPECTED_CONFIG_PATH));
            } catch (FileNotFoundException e){
                RaceVisionXMLParser raceVisionXMLParser = new RaceVisionXMLParser();
                raceVisionXMLParser.exportResource(DEFAULT_CONFIG_PATH, EXPECTED_CONFIG_PATH);
                br = new BufferedReader(new FileReader(EXPECTED_CONFIG_PATH));
            }
            String line = br.readLine();
            while (line != null){
                StringTokenizer st = new StringTokenizer(line);

                String token = st.nextToken("=");
                switch(token) {
                    case "MOCK_SPEED_SCALE":
                        MOCK_SPEED_SCALE = Double.parseDouble(st.nextToken());
                        break;
                    case "SOURCE":
                        String check = st.nextToken();
                        if(ConnectionUtils.IPRegExMatcher(check) || URLMatcher(check)) {
                            SOURCE_ADDRESS = check;
                        } else {
                            throw new IOException("Incorrectly formatted Address");
                        }
                        break;
                    case "SOURCEPORT":
                        int sourcePortCheck = Integer.parseInt(st.nextToken());
                        if(sourcePortCheck >= 0 && sourcePortCheck < 65536) {
                            SOURCE_PORT = sourcePortCheck;
                        } else {
                            throw new IOException("Incorrectly formatted Port");
                        }
                        break;
                    default:
                        throw new IOException("Invalid Token.");
                }

                line = br.readLine();
            }
        } catch (IOException e) {
            System.err.printf("Error reading config file. Check it is in the correct format: %s", e);
            e.printStackTrace();
        }
    }

    public static Boolean URLMatcher(String URL){
        return URL.contains(".") || URL.equals("localhost");
    }
}
