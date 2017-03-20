package seng302;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by cjd137 on 17/03/2017.
 */

public class DisplayUtilsTest {

    @Test
    public void getXYCoords(){
        double minLat = (32.285465 - 0.004); //Lat min
        double minLon = (-64.855621 - 0.004); //Lon min
        double maxLat = (32.316308 + 0.004); //Lat Max
        double maxLon = (-64.830509 + 0.004); //Lon Max

        double multiplier = 0.75;
        DisplayUtils.setWidthHeight(2560*multiplier, 1440*multiplier);
        DisplayUtils.setMaxMinLatLon(minLat, minLon, maxLat, maxLon);

        CartesianPoint testPoint = DisplayUtils.convertFromLatLon(32.295783, -64.855621);

        assertEquals(232, (int) testPoint.getX());
        assertEquals(682, (int) testPoint.getY());
    }
}
