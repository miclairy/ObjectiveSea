package seng302.controllers;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import seng302.models.ClientOptions;
import seng302.models.GameMode;
import seng302.utilities.AnimationUtils;
import seng302.utilities.ConnectionUtils;
import seng302.utilities.DisplaySwitcher;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable{
    @FXML Button btnOfflinePlay;
    @FXML Button btnTutorial;
    @FXML Button btnHost;
    @FXML Button btnSpectate;
    @FXML Button btnJoin;
    @FXML Button btnBack;
    @FXML Button btnOnlineBack;
    @FXML Button btnSinglePlay;
    @FXML Button btnPractiseStart;
    @FXML Button btnCourseStart;
    @FXML Button btnBackHost;
    @FXML Button btnCreateGame;
    @FXML Button btnJoinGame;
    @FXML Button btnCompete;
    @FXML GridPane liveGameGrid;
    @FXML GridPane onlinePane;
    @FXML GridPane offlinePane;
    @FXML GridPane courseGrid;
    @FXML GridPane joinRacePane;
    @FXML TextField txtIPAddress;
    @FXML TextField txtPortNumber;
    @FXML Label lblIP;
    @FXML Label lblPort;
    @FXML ImageView imvSidePane;
    @FXML ImageView AC35;
    @FXML ImageView Athens;
    @FXML ImageView LakeTekapo;
    @FXML ImageView LakeTaupo;
    @FXML ImageView AC33;
    @FXML ImageView Malmo;
    @FXML AnchorPane menuAnchor;
    @FXML TableView tblAvailableRaces;

    private String selectedCourse = "AC35-course.xml"; //default to the AC35

    DropShadow ds = new DropShadow( 20, Color.web("#8eb0b7"));

    @FXML ProgressIndicator joinProgressIndicator;

    private Main main;
    private final int DEFAULT_PORT = 2828;

    /**
     * Initilizer for the Main Menu Controller. Runs upon creation
     * @param location url locaton
     * @param resources bundles of fun
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setButtonAnimations();
        setImageAnimations();
        setLabelPromptAnimations();
        onlinePane.setVisible(true);
        liveGameGrid.setVisible(false);
        offlinePane.setVisible(false);
        courseGrid.setVisible(false);
        joinRacePane.setVisible(false);
        clipChildren(menuAnchor, 2*10);
        tblAvailableRaces.setPlaceholder(new Label("No Available Races"));
    }

    public void setApp(Main main){
        this.main = main;
    }

    @FXML private void loadLiveGameGrid() {
        liveGameGrid.setVisible(true);
        AnimationUtils.slideOutTransition(onlinePane);
        AnimationUtils.slideInTransition(liveGameGrid);
    }

    @FXML private void backToMainMenu() {
        onlinePane.setVisible(true);
        AnimationUtils.slideOutTransition(liveGameGrid);
        AnimationUtils.slideInTransition(onlinePane);
    }

    @FXML private void loadOfflinePane() {
        AnimationUtils.switchPaneFade(onlinePane, offlinePane);
    }

    @FXML private void loadOnlinePane() {
        AnimationUtils.switchPaneFade(offlinePane, onlinePane);
    }

    @FXML private void loadJoinPane(){
        AnimationUtils.switchPaneFade(onlinePane, joinRacePane);
    }

    @FXML private void backToOnline(){
        AnimationUtils.switchPaneFade(joinRacePane, onlinePane);
    }

    @FXML private void backToLiveGame() {
        liveGameGrid.setVisible(true);
        AnimationUtils.slideOutTransition(courseGrid);
        AnimationUtils.slideInTransition(liveGameGrid);
    }

    @FXML private void loadTutorial() throws Exception {
        DisplaySwitcher.getGameSounds().stopEndlessMusic();
        btnSinglePlay.setDisable(true);
        ClientOptions clientOptions = new ClientOptions(GameMode.TUTORIAL);
        main.startHostedRace("GuidedPractice-course.xml", DEFAULT_PORT, true, clientOptions);
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
        main.startHostedRace(selectedCourse, DEFAULT_PORT, false, clientOptions);
        Thread.sleep(200);
        main.loadRaceView(clientOptions);
        loadSinglePlayerMusic();
    }


    @FXML private void loadPracticeStart() throws Exception {
        btnSinglePlay.setDisable(true);
        ClientOptions clientOptions = new ClientOptions(GameMode.PRACTICE);
        main.startHostedRace("PracticeStart-course.xml", DEFAULT_PORT, false, clientOptions);
        Thread.sleep(200);
        main.loadRaceView(clientOptions);
        loadSinglePlayerMusic();
    }

    /**
     * Allows user to host a game at the entered port and current public IP
     * @throws Exception
     */
    @FXML private void startHostGame() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        Integer port = Integer.parseInt(txtPortNumber.getText());
        clientOptions.setServerPort(port);
        main.startHostedRace(selectedCourse, port, false, clientOptions);
        Thread.sleep(200);
        main.loadRaceView(clientOptions);
        loadRealGameSounds();
    }

    @FXML private void hostGame() {
        if(validatePort()) {
            courseGrid.setVisible(true);
            AnimationUtils.slideOutTransition(liveGameGrid);
            AnimationUtils.slideInTransition(courseGrid);
            txtPortNumber.setStyle("-fx-text-inner-color: #2a2a2a;");
        }else{
            if(!txtPortNumber.getText().isEmpty()){
                txtPortNumber.setStyle("-fx-text-inner-color: red;");
            }
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Port Number ");
            alert.setHeaderText("Invalid port number");
            alert.setContentText("Please enter a valid port number\n");
            alert.showAndWait();
        }
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
            if(clientStarted){
                Thread.sleep(200);
                main.loadRaceView(clientOptions);
                loadRealGameSounds();
            }
        } else {
            if(!ConnectionUtils.IPRegExMatcher(txtIPAddress.getText()) && !txtIPAddress.getText().isEmpty()){
                txtIPAddress.setStyle("-fx-text-inner-color: red;");
            }
            if(!validatePort() && !txtPortNumber.getText().isEmpty()){
                txtPortNumber.setStyle("-fx-text-inner-color: red;");
            }
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Port & IP ");
            alert.setHeaderText("Invalid Port or IP Address");
            alert.setContentText("The IP Address or Port Number you entered\n" +
                    "is invalid\n");

            alert.showAndWait();
        }
    }

    /**
     * Joins a race as a spectator
     * @throws Exception
     */
    @FXML private void joinAsSpectator() throws Exception {
        joinGame(false);
    }

    /**
     * Joins a race as a participant
     * @throws Exception
     */
    @FXML private void joinAsParticipant() throws Exception {
        joinGame(true);
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

    }

    private void setImageAnimations(){
        addImageListeners(AC35);
        addImageListeners(LakeTekapo);
        addImageListeners(LakeTaupo);
        addImageListeners(AC33);
        addImageListeners(Malmo);
        addImageListeners(Athens);
        addButtonListeners(AC35);
        addButtonListeners(LakeTaupo);
        addButtonListeners(LakeTekapo);
        addButtonListeners(AC33);
        addButtonListeners(Athens);
        addButtonListeners(Malmo);
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

    private void addImageListeners(ImageView imageView) {
        imageView.setOnMouseClicked( ( MouseEvent event ) ->{ imageView.requestFocus();});
        imageView.focusedProperty().addListener(( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) -> {
            if ( newValue ){
                imageView.setEffect( ds );
                selectedCourse = imageView.getId() + "-course.xml";
            }else{
                imageView.setEffect( null );
            }
        });
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

    private void clipChildren(Region region, double arc) {

        final Rectangle outputClip = new Rectangle();
        outputClip.setArcWidth(arc);
        outputClip.setArcHeight(arc);
        region.setClip(outputClip);
        region.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
            outputClip.setWidth(newValue.getWidth());
            outputClip.setHeight(newValue.getHeight());
        });
    }
}
