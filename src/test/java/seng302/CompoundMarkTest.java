package seng302;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Michael Trotter on 3/17/2017.
 */
public class CompoundMarkTest {

    @Test
    public void initializeMarkTest(){
        CompoundMark mark = new CompoundMark("Normal Mark", 20, 30);
        CompoundMark start = new CompoundMark("Start Mark", 30, 30);
        CompoundMark finish = new CompoundMark("Finish Mark", 40, 50);

        start.setMarkAsStart();
        finish.setMarkAsFinish();

        assertTrue(start.isStartLine());
        assertFalse(start.isFinishLine());

        assertTrue(finish.isFinishLine());
        assertFalse(finish.isStartLine());

        assertFalse(mark.isFinishLine());
        assertFalse(mark.isStartLine());
    }

}