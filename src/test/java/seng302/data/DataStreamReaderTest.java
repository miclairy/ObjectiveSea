package seng302.data;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * Created by raych on 17/04/2017.
 */
public class DataStreamReaderTest {
    private static ServerSocket testFeedSocket;

    private static final String TEST_FEED_ADDRESS = "127.0.0.1";
    private static final int TEST_FEED_PORT = 1234;

    @BeforeClass
    public static void beforeClass() throws IOException{
        testFeedSocket = new ServerSocket(TEST_FEED_PORT);
    }

    @Test
    public void setUpConnectionTest(){
        DataStreamReader testReader = new DataStreamReader(TEST_FEED_ADDRESS, TEST_FEED_PORT);

        Assert.assertNull(testReader.getClientSocket());

        testReader.setUpConnection();

        Assert.assertNotNull(testReader.getClientSocket());
        InetAddress address = testReader.getClientSocket().getInetAddress();

        Assert.assertEquals(TEST_FEED_PORT, testReader.getClientSocket().getPort());
        Assert.assertEquals(TEST_FEED_ADDRESS, address.getHostAddress());
    }
}
