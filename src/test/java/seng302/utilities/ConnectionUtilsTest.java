package seng302.utilities;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by gla42 on 6/04/17.
 */
public class ConnectionUtilsTest {

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
}