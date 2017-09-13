package seng302.controllers;

import javafx.animation.AnimationTimer;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import seng302.data.registration.ServerFullException;
import seng302.models.*;
import seng302.utilities.*;
import seng302.views.AvailableRace;
import seng302.views.CourseMap;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable{
    @FXML private Button btnOfflinePlay;
    @FXML private Button btnTutorial;
    @FXML private Button btnSpectate;
    @FXML private Button btnOnlineBack;
    @FXML private Button btnSinglePlay;
    @FXML private Button btnPractiseStart;
    @FXML private Button btnManual;
    @FXML private Button btnCreateGame;
    @FXML private Button btnJoinGame;
    @FXML private Button btnCompete;
    @FXML private Button btnOnlineBackFromHost;
    @FXML private Button btnLoadMap;
    @FXML private Button btnBackToOptions;
    @FXML private Button btnStartRace;
    @FXML private Button btnSettings;
    @FXML private GridPane onlinePane;
    @FXML private GridPane offlinePane;
    @FXML private GridPane joinRacePane;
    @FXML private GridPane hostOptionsPane;
    @FXML private GridPane selectMapPane;
    @FXML private GridPane settingsGrid;
    @FXML private TextField txtIPAddress;
    @FXML private TextField txtPortNumber;
    @FXML private Label lblIP;
    @FXML private Label lblPort;
    @FXML private AnchorPane menuAnchor;
    @FXML private TableView<AvailableRace> tblAvailableRaces;
    @FXML private TableColumn<AvailableRace, String> columnMap;
    @FXML private TableColumn<AvailableRace, Integer> columnParticipants;
    @FXML private Slider boatsInRaceSlider;
    @FXML private Label lblBoatsNum;
    @FXML private Slider speedScaleSlider;
    @FXML private Slider musicSlider;
    @FXML private Slider fxSlider;
    @FXML private ImageView musicOnImage;
    @FXML private ImageView musicOffImage;
    @FXML private ImageView soundFxOnImage;
    @FXML private ImageView soundFxOffImage;
    @FXML private Label lblSpeedNum;
    @FXML private Shape circleSpeed;
    @FXML private Shape circleBoats;
    @FXML private Polygon mapPolygon;
    @FXML private ImageView imvBackground;

    @FXML private Label lblMarks;
    @FXML private Label lblMapName;
    @FXML private Label lblTime;

    private ArrayList<CourseMap> availableCourseMaps = new ArrayList<>();
    private int currentMapIndex = 0;
    private CourseMap currentCourseMap;
    private CourseMap previousCourseMap;
    private boolean manuallyJoinGame = false;
    public static double paneHeight;
    public static double paneWidth;
    private AnimationTimer timer;
    private MainMenuClient client;
    private Thread mainMenuClientThread;
    private static GameSounds gameSounds;
    private static double musicSliderValue = 1.0;
    private static double fxSliderValue = 1.0;
    private static boolean soundFxIsMute;

    private String selectedCourse = "AC35-course.xml"; //default to the AC35

    private Main main;
    private final int DEFAULT_PORT = 2828;

    /**
     * Initilizer for the Main Menu Controller. Runs upon creation
     * @param location url locaton
     * @param resources bundles of fun
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DisplayUtils.setIsRaceView(false);
        setButtonAnimations();
        setLabelPromptAnimations();
        setPaneVisibility();
        setUpSliders();
        setUpDeselection();
        paneHeight = 400;
        paneWidth = 400;
        setUpMaps();
        clipChildren(menuAnchor, 2*10);
        tblAvailableRaces.setPlaceholder(new Label("No Available Races"));
        columnMap.setStyle( "-fx-alignment: CENTER;");
        columnParticipants.setStyle( "-fx-alignment: CENTER;");
    }

    private void setPaneVisibility(){
        onlinePane.setVisible(true);
        offlinePane.setVisible(false);
        joinRacePane.setVisible(false);
        hostOptionsPane.setVisible(false);
        selectMapPane.setVisible(false);
        menuAnchor.setVisible(true);
        settingsGrid.setVisible(false);
    }

    public void setApp(Main main, GameSounds sounds) throws ServerFullException, NoConnectionToServerException {
        this.main = main;
        this.gameSounds = sounds;
        this.client = new MainMenuClient();
        setUpSoundSliders();
        setUpSoundImages();
        mainMenuClientThread = new Thread(client);
        mainMenuClientThread.start();
    }

    @FXML private void loadHostOptionsPane(){
        AnimationUtils.switchPaneFade(onlinePane, hostOptionsPane);
    }

    @FXML private void loadOfflinePane() {
        AnimationUtils.switchPaneFade(onlinePane, offlinePane);
    }

    @FXML private void loadOnlinePane() {
        AnimationUtils.switchPaneFade(offlinePane, onlinePane);
    }

    @FXML private void loadMapPane(){
        AnimationUtils.switchPaneFade(hostOptionsPane, selectMapPane);
        currentCourseMap = availableCourseMaps.get(currentMapIndex);
        updateMap();
    }

    @FXML private void backToOptions(){
        AnimationUtils.switchPaneFade(selectMapPane, hostOptionsPane);
        if(currentCourseMap != null){
            for(Mark mark : currentCourseMap.getMarks().values()){
                menuAnchor.getChildren().remove(mark.getIcon());
            }
            menuAnchor.getChildren().remove(currentCourseMap.getFinishLine());
            menuAnchor.getChildren().remove(currentCourseMap.getStartLine());
            currentCourseMap.removeArrowedRoute();
            timer.stop();
        }}

    @FXML private void loadJoinPane() {
        setUpAvailableRaceTable();
        AnimationUtils.switchPaneFade(onlinePane, joinRacePane);
        tblAvailableRaces.setItems(client.getAvailableRaces());
    }

    private void setUpAvailableRaceTable(){
        columnMap.setCellValueFactory(cellData -> cellData.getValue().mapNameProperty());
        columnParticipants.setCellValueFactory(cellData -> cellData.getValue().numBoatsProperty().asObject());
    }

    @FXML private void backToOnline(){
        AnimationUtils.switchPaneFade(joinRacePane, onlinePane);
    }

    @FXML private void backFromHost(){AnimationUtils.switchPaneFade(hostOptionsPane, onlinePane);
    }

    @FXML private void loadTutorial() throws Exception {
        DisplaySwitcher.getGameSounds().stopEndlessMusic();
        btnSinglePlay.setDisable(true);
        ClientOptions clientOptions = new ClientOptions(GameMode.TUTORIAL);
        stopMainMenuClientThread();
        main.startLocalRace("GuidedPractice-course.xml", DEFAULT_PORT, true, clientOptions);
        Thread.sleep(200);
        main.loadRaceView(clientOptions);
        loadTutorialMusic();
    }

    /**
     * Allows user to host a game at the DEFAULT_PORT and current public IP
     * @throws Exception
     */
    @FXML private void loadOfflinePlay() throws Exception{
        btnSinglePlay.setDisable(true);
        ClientOptions clientOptions = new ClientOptions(GameMode.SINGLEPLAYER);
        stopMainMenuClientThread();
        main.startLocalRace(selectedCourse, DEFAULT_PORT, false, clientOptions);
        Thread.sleep(200);
        main.loadRaceView(clientOptions);
        loadSinglePlayerMusic();
    }


    @FXML private void loadPracticeStart() throws Exception {
        btnSinglePlay.setDisable(true);
        ClientOptions clientOptions = new ClientOptions(GameMode.PRACTICE);
        stopMainMenuClientThread();
        main.startLocalRace("PracticeStart-course.xml", DEFAULT_PORT, false, clientOptions);
        Thread.sleep(200);
        main.loadRaceView(clientOptions);
        loadSinglePlayerMusic();
    }

    /**
     * Allows user to host a game at the entered port and current public IP
     * @throws Exception
     */
    @FXML private void startHostGame() throws Exception {
        Double speed = speedScaleSlider.getValue();
        Integer minCompetitors = (int) boatsInRaceSlider.getValue();
        ClientOptions clientOptions = new ClientOptions(GameMode.MULTIPLAYER);
        stopMainMenuClientThread();
        main.startHostedRace(currentCourseMap.getXML(), speed, minCompetitors, clientOptions, currentMapIndex);
        timer.stop();
        Thread.sleep(200);
        main.loadRaceView(clientOptions);
        loadRealGameSounds();
    }

    /**
     * Joins a race at the desired IP and Port and creates a client instance
     * @throws Exception
     */
    @FXML private void joinGame(boolean isParticipant) throws Exception{
        if(ConnectionUtils.IPRegExMatcher(txtIPAddress.getText()) && validatePort()){
            String ipAddress = txtIPAddress.getText();
            int portNumber = Integer.parseInt(txtPortNumber.getText());
            ClientOptions clientOptions =
                    new ClientOptions(ipAddress, portNumber, GameMode.MULTIPLAYER, isParticipant, false);
            boolean clientStarted = main.startClient(clientOptions);
            startGame(clientStarted, clientOptions);
        } else {
            if(!ConnectionUtils.IPRegExMatcher(txtIPAddress.getText()) && !txtIPAddress.getText().isEmpty()){
                txtIPAddress.setStyle("-fx-text-inner-color: #ff5459;");
            }
            if(!validatePort() && !txtPortNumber.getText().isEmpty()){
                txtPortNumber.setStyle("-fx-text-inner-color: #ff5459;");
            }
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Port & IP ");
            alert.setHeaderText("Invalid Port or IP Address");
            alert.setContentText("The IP Address or Port Number you entered\n" +
                    "is invalid\n");

            alert.showAndWait();
        }
    }

    private void startGame(boolean clientStarted, ClientOptions clientOptions) throws InterruptedException, UnsupportedAudioFileException, IOException, LineUnavailableException {
        if(clientStarted){
            stopMainMenuClientThread();
            Thread.sleep(200);
            main.loadRaceView(clientOptions);
            loadRealGameSounds();
        }
    }

    /**
     * Joins a race as a spectator
     * @throws Exception
     */
    @FXML private void joinAsSpectator() throws Exception {
        if(manuallyJoinGame){
            joinGame(false);
        }else{
            AvailableRace race = tblAvailableRaces.getSelectionModel().getSelectedItem();
            ClientOptions clientOptions =
                    new ClientOptions(race.getIpAddress(), race.getPort(), GameMode.MULTIPLAYER, false, false);
            boolean clientStarted = main.startClient(clientOptions);
            startGame(clientStarted, clientOptions);
        }
    }

    /**
     * Joins a race as a participant
     * @throws Exception
     */
    @FXML private void joinAsParticipant() throws Exception {
        if(manuallyJoinGame){
            joinGame(true);
        }else{
            AvailableRace race = tblAvailableRaces.getSelectionModel().getSelectedItem();
            String ipAddress = race.getIpAddress();
            if (Objects.equals(ipAddress, ConnectionUtils.getPublicIp())){
                ipAddress = "localhost";
            }
            ClientOptions clientOptions =
                    new ClientOptions(ipAddress, race.getPort(), GameMode.MULTIPLAYER, true, false);
            boolean clientStarted = main.startClient(clientOptions);
            startGame(clientStarted, clientOptions);
        }
    }

    /**
     * attaches listeners to buttons to allow for hover and click animations
     */
    private void setButtonAnimations(){
        addButtonListeners(btnCreateGame);
        addButtonListeners(btnJoinGame);
        addButtonListeners(btnOfflinePlay);
        addButtonListeners(btnTutorial);
        addButtonListeners(btnCompete);
        addButtonListeners(btnSpectate);
        addButtonListeners(btnOnlineBack);
        addButtonListeners(btnPractiseStart);
        addButtonListeners(btnSinglePlay);
        addButtonListeners(btnLoadMap);
        addButtonListeners(btnOnlineBackFromHost);
        addButtonListeners(btnBackToOptions);
        addButtonListeners(btnStartRace);
        addButtonListeners(btnManual);
        addButtonListeners(btnSettings);
    }

    private void setLabelPromptAnimations(){
        addShiftPromptListener(txtIPAddress, lblIP);
        addShiftPromptListener(txtPortNumber, lblPort);
    }

    /**
     * Attaches a shift key listener to the given label
     * @param field the text field to listen
     * @param label the label to prompt
     */
    private void addShiftPromptListener(TextField field, Label label){
        field.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue && field.getText().isEmpty()) {
                AnimationUtils.shiftPromptLabel(label, -1);
            }else if(field.getText().isEmpty()){
                AnimationUtils.shiftPromptLabel(label, 1);
            }
        });
    }

    /**
     * attaches click and hover listeners to buttons
     * @param node the button to attach the listener
     */
    private void addButtonListeners(Node node){
        node.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> AnimationUtils.scaleButtonHover(node));

        node.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> AnimationUtils.scaleButtonHoverExit(node));
    }

    /**
     * checks to determine whether the port number is a valid regex. Colors the port number accordingly.
     * @return whether the port is valid or not
     */
    private boolean validatePort(){
        try {
            int port = Integer.parseInt(txtPortNumber.getText());
            txtPortNumber.setStyle("-fx-text-inner-color: #2a2a2a;");
            if(ConnectionUtils.validatePort(port)){
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void loadSinglePlayerMusic() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        DisplaySwitcher.getGameSounds().stopEndlessMusic();
        DisplaySwitcher.getGameSounds().singlePlayerMusic();
        DisplaySwitcher.getGameSounds().playEndlessMusic();
    }

    private void loadTutorialMusic() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        DisplaySwitcher.getGameSounds().stopEndlessMusic();
        DisplaySwitcher.getGameSounds().tutorialMusic();
        DisplaySwitcher.getGameSounds().playEndlessMusic();
    }

    private void loadRealGameSounds() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        DisplaySwitcher.getGameSounds().stopEndlessMusic();
        DisplaySwitcher.getGameSounds().oceanWaves();
        DisplaySwitcher.getGameSounds().playEndlessMusic();
        DisplaySwitcher.getGameSounds().startSeaGullNoise();
    }

    /**
     * clips side pane image to match the round edges of the pane
     * @param region the area to clip
     * @param arc the rounding of the corners
     */
    private void clipChildren(Region region, double arc) {
        Rectangle outputClip = new Rectangle();
        outputClip.setArcWidth(arc);
        outputClip.setArcHeight(arc);
        region.setClip(outputClip);
        region.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
            outputClip.setWidth(newValue.getWidth());
            outputClip.setHeight(newValue.getHeight());
        });
    }

    /**
     * sets up sliders so labels and circles move with slider thumb
     */
    private void setUpSliders() {
        boatsInRaceSlider.boundsInParentProperty().addListener((observable, oldValue, newValue) -> {
            updateBoatsLabel();
        });
        boatsInRaceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateBoatsLabel();
        });

        speedScaleSlider.boundsInParentProperty().addListener((observable, oldValue, newValue) -> {
            updateSpeedLabel();
        });
        speedScaleSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateSpeedLabel();
        });
    }
    /**
     * binds the lable text to the slider value and shifts circle to new loctation
     */
    private void updateSpeedLabel() {
        Bounds bounds = speedScaleSlider.lookup(".thumb").getBoundsInParent();

        circleSpeed.setTranslateX(bounds.getMinX() + 10);
        lblSpeedNum.setTranslateX(bounds.getMinX() + 10);
        lblSpeedNum.textProperty().setValue(
                String.valueOf((int) speedScaleSlider.getValue()));

        if (speedScaleSlider.getValue() >= 10) {
            lblSpeedNum.setScaleX(0.8);
            lblSpeedNum.setScaleY(0.8);
        } else {
            lblSpeedNum.setScaleX(1);
            lblSpeedNum.setScaleY(1);
        }
    }

    /**
     * binds the lable text to the slider value and shifts circle to new loctation
     */
    private void updateBoatsLabel(){
        Bounds bounds = boatsInRaceSlider.lookup(".thumb").getBoundsInParent();

        circleBoats.setTranslateX(bounds.getMinX() + 10);
        lblBoatsNum.setTranslateX(bounds.getMinX() + 10);

        lblBoatsNum.textProperty().setValue(
                String.valueOf((int) boatsInRaceSlider.getValue()));
    }

    /**
     * creates maps from XML files
     */
    private void setUpMaps(){
        availableCourseMaps.add(new CourseMap("AC35","26:04"));
        availableCourseMaps.add(new CourseMap("AC33","28:59"));
        availableCourseMaps.add(new CourseMap("Lake Tekapo","26:05"));
        availableCourseMaps.add(new CourseMap("Lake Taupo","25:21"));
        availableCourseMaps.add(new CourseMap("Malmo","28:20"));
        availableCourseMaps.add(new CourseMap("Athens","17:42"));
    }

    /**
     * called when next arrow pressed, changes map in menu
     */
    @FXML private void nextMap(){
        previousCourseMap = availableCourseMaps.get(currentMapIndex);
        if(currentMapIndex == availableCourseMaps.size() - 1){
            currentMapIndex = 0;
        }else{
            currentMapIndex += 1;
        }
        updateMap();
    }

    /**
     * called when back arrow pressed, changes map in menu
     */
    @FXML private void previousMap(){
        previousCourseMap = availableCourseMaps.get(currentMapIndex);
        if(currentMapIndex == 0){
            currentMapIndex = availableCourseMaps.size() - 1;
        }else{
            currentMapIndex -= 1;
        }
        updateMap();
    }

    /**
     * updates the map in the map selection pane when arrow clicked
     */
    private void updateMap(){
        currentCourseMap = availableCourseMaps.get(currentMapIndex);
        drawRoute();
        drawMarks();
        lblMapName.setText(currentCourseMap.getMapName());
        lblMarks.setText(currentCourseMap.getNumberOfMarks().toString());
        lblTime.setText(currentCourseMap.getEstTimeToRace());
        mapPolygon.getPoints().clear();
        mapPolygon.getPoints().addAll(currentCourseMap.getMapBoundary().getPoints());
    }

    /**
     * draws marks onto the map displayed in the menu map selection pane
     */
    private void drawMarks(){
        if(previousCourseMap != null){
            for(Mark mark : previousCourseMap.getMarks().values()){
                menuAnchor.getChildren().remove(mark.getIcon());
            }
            menuAnchor.getChildren().remove(previousCourseMap.getStartLine());
            menuAnchor.getChildren().remove(previousCourseMap.getFinishLine());
        }

        menuAnchor.getChildren().add(currentCourseMap.getStartLine());
        menuAnchor.getChildren().add(currentCourseMap.getFinishLine());
        for(Mark mark : currentCourseMap.getMarks().values()){
            menuAnchor.getChildren().add(mark.getIcon());
        }
    }

    /**
     * draws the route of a boat onto the map and creates an animation loop highlighting it
     */
    private void drawRoute(){
        if(previousCourseMap != null){
            previousCourseMap.removeArrowedRoute();
            timer.stop();
        }
        currentCourseMap.setUpArrowRoute(menuAnchor);
        timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                currentCourseMap.updateArrowRoute();
            }
        };
        timer.start();
    }

    /**
     * changes from the table view to a manual view with text fields for port and IP
     * entry
     */
    @FXML private void displayManualOptions(){
        manuallyJoinGame = !manuallyJoinGame;
        txtIPAddress.setVisible(manuallyJoinGame);
        txtPortNumber.setVisible(manuallyJoinGame);
        lblIP.setVisible(manuallyJoinGame);
        lblPort.setVisible(manuallyJoinGame);
        tblAvailableRaces.setVisible(!manuallyJoinGame);
        if(manuallyJoinGame){
            btnManual.setText("Auto");
        }else{
            btnManual.setText("Manual");
        }

    }

    /**
     * Shows sound settings panel
     */

    @FXML private void showSettings(){
        AnimationUtils.fadeNode(settingsGrid, settingsGrid.isVisible());
        musicOnImage.setVisible(!(gameSounds.getVolume() == 0.0));
        musicOffImage.setVisible(gameSounds.getVolume() == 0.0);
        soundFxOnImage.setVisible(!soundFxIsMute);
        soundFxOffImage.setVisible(soundFxIsMute);
        musicSlider.setValue(musicSliderValue);
        fxSlider.setValue(fxSliderValue);
    }

    private void setUpSoundSliders(){
        musicSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(musicOnImage.isVisible()) {
                System.out.println((Double)newValue);
                gameSounds.setVolume((Double) newValue);
                if (newValue.equals(0.0)) {
                    toggleMusicImages(false);
                }
            }
            musicSliderValue = (Double) newValue;
        });

        fxSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(soundFxOnImage.isVisible()) {
                gameSounds.setFxVolume((Double) newValue);
                if (newValue.equals(0.0)) {
                    toggleSoundFxImages(false);
                }
            }
            fxSliderValue = (Double) newValue;
        });
    }

    private void toggleMusicImages(boolean showMusicOnImage){
        musicOnImage.setVisible(showMusicOnImage);
        musicOffImage.setVisible(!showMusicOnImage);
    }

    private void toggleSoundFxImages(boolean showSoundFxOnImage){
        soundFxOnImage.setVisible(showSoundFxOnImage);
        soundFxOffImage.setVisible(!showSoundFxOnImage);
    }

    private void setUpSoundImages(){
        musicOnImage.setOnMouseClicked((MouseEvent event) ->{
            muteMusic();
        });
        musicOffImage.setOnMouseClicked((MouseEvent event) ->{
            unMuteMusic();
        });
        soundFxOnImage.setOnMouseClicked((MouseEvent event) ->{
            muteSoundFx();
        });
        soundFxOffImage.setOnMouseClicked((MouseEvent event) ->{
            unMuteSoundFx();
        });
    }

    private void muteMusic(){
        gameSounds.setVolume(0.0);
        toggleMusicImages(false);
    }

    private void unMuteMusic(){
        gameSounds.setVolume(musicSliderValue);
        toggleMusicImages(true);
    }

    private void muteSoundFx(){
        soundFxIsMute = true;
        gameSounds.setFxVolume(0.0);
        toggleSoundFxImages(false);
    }

    private void unMuteSoundFx(){
        soundFxIsMute = false;
        gameSounds.setFxVolume(fxSliderValue);
        toggleSoundFxImages(true);
    }

    public static double getCanvasHeight(){
        return paneHeight;
    }

    public static double getCanvasWidth(){
        return paneWidth;
    }

    private void stopMainMenuClientThread() {
        if (mainMenuClientThread != null) {
            mainMenuClientThread.stop();
        }
    }

    private void setUpDeselection(){
        imvBackground.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if(settingsGrid.isVisible()){
                AnimationUtils.fadeNode(settingsGrid, true);
            }
        });
    }
}

