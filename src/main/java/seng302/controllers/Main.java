package seng302.controllers;

/**
 * Main class. Loads data and starts GUI.
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.geometry.Rectangle2D;
import seng302.data.DataStreamReader;
import seng302.data.MockStream;
import seng302.utilities.Config;
import seng302.data.RaceVisionFileReader;
import seng302.models.Boat;
import seng302.models.Course;
import seng302.models.Race;
import java.util.List;


public class Main extends Application {

    private static Race race;

    /**
     * Loads in the course and creates the race to run.
     */
    @Override
    public void init(){

        DataStreamReader dataStreamReader = new DataStreamReader(Config.SOURCE_ADDRESS, Config.SOURCE_PORT);
        Thread dataStreamReaderThread = new Thread(dataStreamReader);
        race = new Race();
        dataStreamReader.setRace(race);
        dataStreamReaderThread.start();

        //block until we have received the required XMLs from the stream
        while(!dataStreamReader.intialDataReceived()){
            try {
                Thread.sleep(2);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
        }

        //read everything in
        String courseFile = getParameters().getNamed().get("course");
        String boatsFile = getParameters().getNamed().get("boats");
        String regattaFile = getParameters().getNamed().get("regatta");
        List<Boat> boatsInRace = RaceVisionFileReader.importStarters(boatsFile);
        Course course = RaceVisionFileReader.importCourse(courseFile);
        //for now if we fail to read in a course or boats, then exit the program immediately
        if (boatsInRace.isEmpty() || course == null) {
            Platform.exit();
        }
        String name = "Default name";
        race.initialize(name, course, boatsInRace);
        RaceVisionFileReader.importRegatta(regattaFile, race);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource("main_window.fxml"));
        primaryStage.setTitle("Race Vision");
        primaryStage.getIcons().add(new Image("graphics/icon.png"));
        primaryStage.setScene(new Scene(parent));
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setHeight(primaryScreenBounds.getHeight());
        primaryStage.setWidth(primaryScreenBounds.getWidth());
        primaryStage.show();
    }

    public static void main( String[] args )
    {
        Config.initializeConfig();
        setupMockStream();
        launch(args);
    }

    /**
     * Creates a MockStream object, puts it in it's own thread and starts the thread
     */
    private static void setupMockStream(){
        MockRaceRunner runner = new MockRaceRunner();
        Thread runnerThread = new Thread(runner);
        runnerThread.start();
        MockStream mockStream;
        mockStream = new MockStream(2828, runner);
        Thread upStream = new Thread(mockStream);
        upStream.start();
    }

    public static Race getRace() {
        return Main.race;
    }

}

