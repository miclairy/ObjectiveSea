package seng302.utilities;

import org.junit.Test;
import seng302.utilities.TimeUtils;

import static org.junit.Assert.*;

/**
 * Created by Michael Trotter on 3/25/2017.
 */
public class TimeUtilsTest {

    @Test
    public void convertMinutesToSecondsTest() throws Exception {
        assertEquals(90, TimeUtils.convertMinutesToSeconds(1.5), 0);
    }

    @Test
    public void convertSecondsToHoursTest() throws Exception {
        assertEquals(0.5, TimeUtils.convertSecondsToHours(1800), 0);
    }

    @Test
    public void convertNanosecondsToSecondsTest() throws Exception {
        assertEquals(2, TimeUtils.convertNanosecondsToSeconds(2e9f), 0);
    }

    @Test
    public void convertHoursToSecondsTest() throws Exception {
        assertEquals(5400, TimeUtils.convertHoursToSeconds(1.5), 0);
    }

    @Test
    public void calcDistanceTest() throws Exception {
        assertEquals(0, TimeUtils.calcDistance(10, 10, 10, 10), 0.0005);
        assertEquals(0, TimeUtils.calcDistance(0, 0, 0, 0), 0);
        assertEquals(4809.027, TimeUtils.calcDistance(10, 100, 10, 100), 0.0005);

    }
}