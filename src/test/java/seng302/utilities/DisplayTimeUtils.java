package seng302.utilities;

import org.junit.Before;
import org.junit.Test;
import seng302.models.Boat;
import seng302.views.BoatDisplay;
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
        String formattedTime = boatDisplay.getTimeToNextMark(timeAtMark, currentTime);
        assert(formattedTime.matches(regex));
    }

    @Test
    public void TestTimeAfter(){
        long currentTime = Instant.now().toEpochMilli();
        long timeAtMark = Instant.now().toEpochMilli() + 1000000;
        String formattedTime = boatDisplay.getTimeSinceLastMark(currentTime);
        assert(Objects.equals(formattedTime, "..."));
    }
}