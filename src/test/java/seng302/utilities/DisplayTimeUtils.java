package seng302.utilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import src.main.java.models.Boat;
import src.main.java.views.BoatDisplay;
import java.time.Instant;
import java.util.Objects;

/**
 * Created by lga50 on 6/04/17.
 *
 */
public class DisplayTimeUtils {
    private String regex = "^(\\d\\d:\\d\\d)";
    private BoatDisplay boatDisplay;


    @Before
    public void setUp(){
        Boat boat = new Boat(-1, "New Zealand", "NZ",  20.00);
        boatDisplay = new BoatDisplay(boat, null);
    }

    @Test
    public void TestTimeTill(){
        long currentTime = Instant.now().toEpochMilli();
        long timeAtMark = Instant.now().toEpochMilli() + 1000000;
        String formattedTime = DisplayUtils.getTimeToNextMark(timeAtMark, currentTime);
        assert(formattedTime.matches(regex));

        String time = DisplayUtils.getTimeToNextMark(4000000,0);
        String time1 = DisplayUtils.getTimeToNextMark(2000,0);

        Assert.assertTrue(time.equals("..."));
        Assert.assertTrue(!time1.equals("..."));
    }

    @Test
    public void TestTimeAfter(){
        long currentTime = Instant.now().toEpochMilli();
        String formattedTime = DisplayUtils.getTimeSinceLastMark(currentTime, boatDisplay.getBoat());
        assert(Objects.equals(formattedTime, "..."));
    }
}