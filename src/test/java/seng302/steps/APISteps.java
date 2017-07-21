package seng302.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import javafx.scene.input.KeyCode;
import seng302.controllers.RaceUpdater;
import seng302.controllers.Server;
import seng302.controllers.ServerListener;
import seng302.data.BoatAction;
import seng302.data.ClientPacketBuilder;
import seng302.data.ClientSender;
import seng302.data.RaceStatus;
import seng302.models.Boat;
import seng302.models.Course;
import seng302.models.Race;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by mjt169 on 21/07/17.
 */
public class APISteps {

    private Server server;
    private Boat sallysBoat;
    private Race race;
    private Set<Socket> sockets = new HashSet<>();
    private ServerSocket serverSocket;
    private static int port = 1234;

    @Given("^Sally has a boat$")
    public void sallyHasABoat() throws Throwable {
        RaceUpdater mockRaceRunner = mock(RaceUpdater.class);
        sallysBoat = new Boat(102, "Sally's Boat", "SB", 12);
        sallysBoat.setHeading(30.0);
        sallysBoat.setCurrentSpeed(10);
        race = mock(Race.class);
        Course course = mock(Course.class);
        when(course.getWindDirection()).thenReturn(0.0);
        when(race.getId()).thenReturn("1");
        when(race.getRaceStatus()).thenReturn(RaceStatus.STARTED);
        when(race.getCourse()).thenReturn(course);
        when(race.getBoatById(102)).thenReturn(sallysBoat);
        when(mockRaceRunner.getRace()).thenReturn(race);
        server = new Server(port++, mockRaceRunner);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    @When("^Sally presses the \"([^\"]*)\" key$")
    public void sallyPressesTheKey(String key) throws Throwable {
        ClientPacketBuilder packetBuilder = new ClientPacketBuilder();
        serverSocket = new ServerSocket(port);
        Socket socket = new Socket("localhost", port++);
        Socket yetAnotherSocket = serverSocket.accept();
        ServerListener listener = new ServerListener(yetAnotherSocket);
        listener.setRace(race);
        listener.addObserver(server);
        Thread serverListenerThread = new Thread(listener);
        serverListenerThread.start();
        ClientSender sender = new ClientSender(socket);
        KeyCode keyCode = KeyCode.valueOf(key.toUpperCase());
        byte[] packet = packetBuilder.createBoatCommandPacket(BoatAction.getTypeFromKeyCode(keyCode), 102);
        sender.sendToServer(packet);
        sockets.add(socket);
        sockets.add(yetAnotherSocket);
    }

    @Then("^the heading of Sally's boat has been changed$")
    public void theHeadingOfSallySBoatHasBeenChanged() throws Throwable {
        Thread.sleep(100);
        assertEquals(330.0, sallysBoat.getHeading(), 0);
        tearDown();
    }

    @Then("^the sails should be brought in so that the speed becomes (\\d+)$")
    public void theSailsShouldBeBroughtInSoThatTheSpeedBecomes(int speed) throws Throwable {
        Thread.sleep(10);
        assertEquals(0.0, sallysBoat.getSpeed());
        //TODO check sails are actually brought in
        tearDown();
    }

    @Then("^the boats speed should be the same as the vmg speed$")
    public void theBoatsSpeedShouldBeTheSameAsTheVmgSpeed() throws Throwable {
        Thread.sleep(10);
        assertEquals(10.0, sallysBoat.getSpeed(), 0.0);
        //TODO changed the expected to the sallysboat.getVMGSpeed() when it is available
        tearDown();
    }

    @Then("^the boats heading should be increased$")
    public void theBoatsHeadingShouldBeIncreased() throws Throwable {
        Thread.sleep(10);
        assertEquals(33.0, sallysBoat.getHeading(), 0.0);
        tearDown();
    }

    @Then("^the boats heading should be decreased$")
    public void theBoatsHeadingShouldBeDecreased() throws Throwable {
        Thread.sleep(10);
        assertEquals(27.0, sallysBoat.getHeading(), 0.0);
        tearDown();
    }


    /**
     * Stop server and close sockets
     * @throws IOException
     */
    private void tearDown() throws IOException {
        server.stop();
        for (Socket socket : sockets) {
            socket.close();
        }
        serverSocket.close();
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
