package seng302.models;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import seng302.utilities.DisplayUtils;
import static org.junit.Assert.*;

/**
 * Created by Louis on 22/05/2017.
 *
 */
public class lineTest {

    private static Boat boat1;
    private static Boat boat2;
    private static CompoundMark compoundMark;
    private static DistanceLine distanceLine;

    @BeforeClass
    public static void setUp() {
        boat1 = new Boat(0, "TestBoat", "testNickname", 10);
        boat1.setCurrentSpeed(10);
        boat1.setPosition(10,10);
        boat2 = new Boat(1, "TestBoat", "testNickname", 10);
        boat2.setCurrentSpeed(10);
        boat2.setPosition(20,20);
        Mark mark1 = new Mark(4, "Mark 1", new Coordinate(2, 2));
        compoundMark = new CompoundMark(2, "Mark", mark1);
        distanceLine = new DistanceLine();
        distanceLine.setFirstBoat(boat1);
        distanceLine.setSecondBoat(boat2);
        distanceLine.setMark(compoundMark);
        DisplayUtils.setMaxMinLatLon(30, 20, 150, 150);
    }

    @Test
    public void distanceLineTest() {
        assertEquals(0, distanceLine.getLines().size());
        distanceLine.reCalcLine();
        assertEquals(4, distanceLine.getLines().size());
    }

//    @Test
//    public void distanceBetweenTest(){
//        distanceLine.reCalcLine();
//        double distanceBetween = distanceLine.getDistanceBetweenBoats();
//        assert(distanceBetween > -1);
//        assertEquals(833, distanceBetween, 1);
//    }
}
