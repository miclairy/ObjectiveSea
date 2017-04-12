package seng302.utilities;

import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by gemma on 12/04/2017.
 */
public class PolarReaderTest {

    @Test
    public void getTWS() throws Exception {
        PolarReader.polars();
        assertEquals(9, PolarReader.getTWS().size());
    }

    @Test
    public void getPolars() throws Exception {
        assertEquals(9, PolarReader.getPolars().size());
    }


}