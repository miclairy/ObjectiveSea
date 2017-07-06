package seng302.Views;

import org.junit.Assert;
import org.junit.Test;
import seng302.models.Boat;
import seng302.models.Course;
import seng302.models.Polar;
import seng302.models.PolarTable;
import seng302.utilities.PolarReader;
import seng302.views.BoatDisplay;

import java.util.ArrayList;

/**
 * Created by Gemma on 6/07/17.
 */
public class BoatDisplayTest {

    @Test
    public void getTimeToNextMarkTest(){
        Boat boat = new Boat(1,"","",1);
        ArrayList<Polar> polars = PolarReader.getPolarsForAC35Yachts();
        Course course = new Course();
        PolarTable pl = new PolarTable(polars,course);
        BoatDisplay boatDisplay = new BoatDisplay(boat, null);
        String time = boatDisplay.getTimeToNextMark(2000000,0);
        String time1 = boatDisplay.getTimeToNextMark(2000,0);

        Assert.assertTrue(time == "...");
        Assert.assertTrue(time1 != "...");
    }
}
