package seng302;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by atc60 on 16/03/17.
 */
public class CourseTest {

    @Test
    public void gcDistanceTest(){
        double lat1 = 50;
        double lat2 = 60;
        double lon1 = 30;
        double lon2 = 60;

        assertEquals(1179, (int)Math.round(Course.greaterCircleDistance(50, 60, 30, 60)));
    }
}
