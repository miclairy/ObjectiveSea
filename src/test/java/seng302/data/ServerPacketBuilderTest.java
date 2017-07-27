package seng302.data;

import org.junit.Test;
import seng302.models.Boat;
import seng302.models.Course;
import seng302.models.Race;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static seng302.data.AC35StreamField.*;
import static seng302.data.AC35StreamField.START_TIME;
import static seng302.data.AC35StreamField.WIND_SPEED;
import static seng302.data.Receiver.byteArrayRangeToInt;
import static seng302.data.Receiver.byteArrayRangeToLong;

/**
 * Created by Michael Trotter on 7/24/2017.
 */
public class ServerPacketBuilderTest {

    private int CRC_LENGTH = 4;
    private int HEADER_LENGTH = 15;

    @Test
    public void createRaceUpdateMessage() throws Exception {
        ServerPacketBuilder builder = new ServerPacketBuilder();
        Course course = new Course();
        course.setWindDirection(10);
        course.setTrueWindSpeed(20);
        Boat boat1 = new Boat(100, "Test Boat 1", "TB1", 10);
        Boat boat2 = new Boat(100, "Test Boat 2", "TB2", 10);
        List<Boat> boats = new ArrayList<>();
        boats.add(boat1);
        boats.add(boat2);
        Race race = new Race("Test Race", course, boats);
        race.setId("1");
        race.updateRaceStatus(RaceStatus.STARTED);
        byte[] fullPacket = builder.createRaceUpdateMessage(race);
        byte[] body = Arrays.copyOfRange(fullPacket, HEADER_LENGTH, fullPacket.length - CRC_LENGTH); //extract body

        int raceStatus = byteArrayRangeToInt(body, RACE_STATUS.getStartIndex(), RACE_STATUS.getEndIndex());
        int raceCourseWindDirection = byteArrayRangeToInt(body, WIND_DIRECTION.getStartIndex(), WIND_DIRECTION.getEndIndex());
        long currentTime = byteArrayRangeToLong(body, CURRENT_TIME.getStartIndex(), CURRENT_TIME.getEndIndex());
        long expectedStartTime = byteArrayRangeToLong(body, START_TIME.getStartIndex(), START_TIME.getEndIndex());
        long windSpeed = byteArrayRangeToInt(body, WIND_SPEED.getStartIndex(), WIND_SPEED.getEndIndex());

        assertEquals(RaceStatus.STARTED, RaceStatus.fromInteger(raceStatus));
        assertEquals(1820, raceCourseWindDirection);
        assertEquals(10288, windSpeed);
        assertEquals(race.getCurrentTimeInEpochMs(), currentTime);
        assertEquals(race.getStartTimeInEpochMs(), expectedStartTime);

        int boat1ID = byteArrayRangeToInt(body, 24, 28);
        int boat1Status = byteArrayRangeToInt(body, 28, 29);
        int boat1Leg = byteArrayRangeToInt(body, 29, 32);
        assertEquals(boat1.getId(), (Integer) boat1ID);
        assertEquals(boat1.getStatus(), BoatStatus.values()[boat1Status]);
        assertEquals(boat1.getLeg(), boat1Leg);

        int offset = 20;
        int boat2ID = byteArrayRangeToInt(body, 24 + offset, 28 + offset);
        int boat2Status = byteArrayRangeToInt(body, 28 + offset, 29 + offset);
        int boat2Leg = byteArrayRangeToInt(body, 29 + offset, 32 + offset);
        assertEquals(boat1.getId(), (Integer) boat2ID);
        assertEquals(boat1.getStatus(), BoatStatus.values()[boat2Status]);
        assertEquals(boat1.getLeg(), boat2Leg);
    }

    @Test
    public void createMarkRoundingMessage() {
    }

    @Test
    public void buildXmlMessage() {
    }

    @Test
    public void createBoatLocationMessage() {
    }

    @Test
    public void createRegistrationAcceptancePacket() {
    }

}
