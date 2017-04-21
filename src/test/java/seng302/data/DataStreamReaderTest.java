package seng302.data;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class DataStreamReaderTest {
    private static ServerSocket testFeedSocket;

    private static final String TEST_FEED_ADDRESS = "127.0.0.1";
    private static final int TEST_FEED_PORT = 1234;
    private static final double DELTA = 1e-6;

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

    @Test
    public void readXMLTest() throws IOException{
        //Test when integrating data generation
    }

    @Test
    public void byteArrayRangeToIntTest(){
        byte[] testByteArray = new byte[]{8, 52, 1, 0, 2};
        Assert.assertEquals(8, DataStreamReader.byteArrayRangeToInt(testByteArray, 0, 1));
        Assert.assertEquals(131073, DataStreamReader.byteArrayRangeToInt(testByteArray, 2, 5));
        Assert.assertEquals(78856, DataStreamReader.byteArrayRangeToInt(testByteArray, 0, 4));
    }

    @Test(expected=IllegalArgumentException.class)
    public void byteArrayRangeToIntExceptionTest(){
        byte[] testByteArray = new byte[]{8, 52, 1, 0, 2};
        DataStreamReader.byteArrayRangeToInt(testByteArray, 0, 5);
    }

    @Test
    public void intToLatLonTest(){
        Assert.assertEquals(180, DataStreamReader.intToLatLon((int)Math.pow(2, 31)), DELTA);
        Assert.assertEquals(-180, DataStreamReader.intToLatLon((int)-Math.pow(2, 31)), DELTA);
        Assert.assertEquals(0, DataStreamReader.intToLatLon(0), DELTA);
        Assert.assertEquals(41.90951585, DataStreamReader.intToLatLon(500000000), DELTA);
        Assert.assertEquals(-33.5276126, DataStreamReader.intToLatLon(-400000000), DELTA);
    }

    @Test
    public void intToHeadingTest(){
        Assert.assertEquals(0, DataStreamReader.intToHeading(0x0000), DELTA);
        Assert.assertEquals(90, DataStreamReader.intToHeading(0x4000), DELTA);
        Assert.assertEquals(180, DataStreamReader.intToHeading(0x8000), DELTA);
        Assert.assertEquals(270, DataStreamReader.intToHeading(0xC000), DELTA);

        Assert.assertEquals(241.5948486, DataStreamReader.intToHeading(0xABCD), DELTA);
        Assert.assertEquals(23.99963378, DataStreamReader.intToHeading(0x1111), DELTA);
    }
}
