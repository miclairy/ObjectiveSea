package seng302;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Michael Trotter on 3/17/2017.
 */
public class GateTest {

    @Test
    public void testGateCreate(){
        Gate gate = new Gate("Gate",10.3,10,20.3,20);
        double midpointLat = gate.getLat();
        double midpointLon = gate.getLon();

        assertEquals(midpointLat, 10.3, 0);
        assertEquals(midpointLon, 10, 0);
    }


}