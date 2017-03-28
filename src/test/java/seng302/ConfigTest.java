package seng302;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created on 23/03/17.
 */
public class ConfigTest {

    @Test
    public void initializeTest(){
        Config.initializeConfig();
        Assert.assertEquals(6, Config.NUM_BOATS_IN_RACE);
    }

}