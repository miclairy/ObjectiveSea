package seng302.utilities;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by gla42 on 6/04/17.
 */
public class ConfigTest {

    @Test
    public void IPRegExMatcher() throws Exception {
        Assert.assertTrue(ConnectionUtils.IPRegExMatcher("192.168.0.1"));
        Assert.assertTrue(ConnectionUtils.IPRegExMatcher("0.0.0.0"));
        Assert.assertTrue(ConnectionUtils.IPRegExMatcher("255.255.255.255"));
        Assert.assertFalse(ConnectionUtils.IPRegExMatcher("256.1.2.32"));
        Assert.assertFalse(ConnectionUtils.IPRegExMatcher("-1.0.0.0"));
        Assert.assertFalse(ConnectionUtils.IPRegExMatcher("124.132.1"));
        Assert.assertFalse(ConnectionUtils.IPRegExMatcher("124.674.3.2.1"));
        Assert.assertFalse(ConnectionUtils.IPRegExMatcher("123...2"));
    }

    @Test
    public void URLMatcher() throws Exception {
        Assert.assertTrue(Config.URLMatcher("livedata.americascup.com"));
        Assert.assertFalse(Config.URLMatcher("livedata"));
        Assert.assertTrue(Config.URLMatcher("W3zugfE-bdu.co.nz/blah"));
    }

    /** We can't test against specific values as the whole point of the config file is that it can be changed
    @Test
    public void initializeTest(){
        Config.initializeConfig();
        Assert.assertEquals(6, Config.NUM_BOATS_IN_RACE);
        Assert.assertEquals("livedata.americascup.com", Config.SOURCE_ADDRESS);
        Assert.assertEquals(4941, Config.SOURCE_PORT);
    }
    */
}