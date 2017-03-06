package seng302;

import java.util.ArrayList;

public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "RaceVision\n" );

        ArrayList<String> starters = RaceVisionFileReader.importStarters();
        Display.printStartersList(starters);
    }
}
