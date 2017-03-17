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

        ArrayList<Double> latLon = new ArrayList<>();
        latLon.add(32.285465 - 0.004); //Lat min
        latLon.add(-64.855621 - 0.004); //Lon min
        latLon.add(32.316308 + 0.004); //Lat Max
        latLon.add(-64.830509 + 0.004); //Lon Max

        DisplayUtils.setWidthHeight(2560, 1440);
        DisplayUtils.setScreenSize(0.75);
        DisplayUtils.setMaxMinLatLon(latLon);

        ArrayList<Double> tester;
        tester = DisplayUtils.convertFromLatLon(32.295783, -64.855621);
        double x = tester.get(0);
        double y = tester.get(1);

        assertEquals(232, (int) x);
        assertEquals(398, (int) y);

    }
}
