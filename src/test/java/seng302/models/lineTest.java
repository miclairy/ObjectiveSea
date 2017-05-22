package seng302.models;

import org.junit.Before;
import org.junit.Test;
import seng302.utilities.DisplayUtils;

import static org.junit.Assert.*;

/**
 * Created by Louis on 22/05/2017.
 *
 */
public class lineTest {

    private Boat boat1;
    private Boat boat2;
    private CompoundMark compoundMark;
    private DistanceLine distanceLine;

    @Before
    public void setUp() {
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

    /**
     * Currently testing that some lines are created without caring about the actual
     * line start and stop points.
     */
    @Test
    public void distanceLineTest() {
        assertEquals(distanceLine.getLines().size(), 0);
        distanceLine.reCalcLine();
        assertNotEquals(distanceLine.getLines().size(), 0);
    }

    @Test
    public void distanceBetweenTest(){
        distanceLine.reCalcLine();
        double distanceBetween = distanceLine.getDistanceBetweenBoats();
        assert(distanceBetween > -1);
        assertEquals(833, distanceBetween, 1);
    }
}
