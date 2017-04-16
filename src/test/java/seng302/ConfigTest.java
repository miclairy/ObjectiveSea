package seng302;

import org.junit.Assert;
import org.junit.Test;
import seng302.utilities.Config;

/**
 * Created on 23/03/17.
 */
public class ConfigTest {

    @Test
    public void initializeTest(){
        Config.initializeConfig();
        Assert.assertEquals(6, Config.NUM_BOATS_IN_RACE);
    }

    @Test
    public void sourceAddressTest(){
        Config.initializeConfig();
        Assert.assertEquals("livedata.americascup.com", Config.SOURCE_ADDRESS);
    }

}