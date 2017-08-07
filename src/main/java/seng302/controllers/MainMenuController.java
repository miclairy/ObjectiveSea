package seng302.controllers;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import seng302.utilities.AnimationUtils;
import seng302.utilities.ConnectionUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable{
    @FXML Button btnLiveGame;
    @FXML Button btnPractise;
    @FXML Button btnTutorial;
    @FXML Button btnHost;
    @FXML Button btnSpectate;
    @FXML Button btnJoin;
    @FXML Button btnBack;
    @FXML Button btnSinglePlay;
    @FXML Button btnPractiseStart;
    @FXML Button btnBackPrac;
    @FXML Button btnCourseStart;
    @FXML Button btnBackHost;
    @FXML GridPane liveGameGrid;
    @FXML GridPane btnGrid;
    @FXML GridPane practiceGrid;
    @FXML GridPane courseGrid;
    @FXML TextField txtIPAddress;
    @FXML TextField txtPortNumber;
    @FXML Label lblIP;
    @FXML Label lblPort;
    @FXML ImageView AC35;
    @FXML ImageView Athens;
    @FXML ImageView LakeTekapo;
    @FXML ImageView LakeTaupo;
    @FXML ImageView AC33;

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
        btnGrid.setVisible(true);
        liveGameGrid.setVisible(false);
        practiceGrid.setVisible(false);
        courseGrid.setVisible(false);
    }

    private void showJoinProgressIndicator(){
        btnJoin.setText("");
        joinProgressIndicator.setPrefSize(15.0,15.0);
    }
    private void hideJoinProgressIndicator(){
        btnJoin.setText("JOIN");

        joinProgressIndicator.setPrefSize(0.0,0.0);
    }


    public void setApp(Main main){
        this.main = main;
    }

    @FXML private void loadLiveGameGrid(){
        liveGameGrid.setVisible(true);
        AnimationUtils.slideOutTransition(btnGrid);
        AnimationUtils.slideInTransition(liveGameGrid);
    }

    @FXML private void backToMainMenu(){
        btnGrid.setVisible(true);
        AnimationUtils.slideOutTransition(liveGameGrid);
        AnimationUtils.slideInTransition(btnGrid);
    }

    @FXML private void loadPractiseGrid(){
        practiceGrid.setVisible(true);
        AnimationUtils.slideOutTransition(btnGrid);
        AnimationUtils.slideInTransition(practiceGrid);
    }

    @FXML private void backToMainMenuPrac(){
        btnGrid.setVisible(true);
        AnimationUtils.slideOutTransition(practiceGrid);
        AnimationUtils.slideInTransition(btnGrid);
    }

    @FXML private void backToLiveGame(){
        liveGameGrid.setVisible(true);
        AnimationUtils.slideOutTransition(courseGrid);
        AnimationUtils.slideInTransition(liveGameGrid);
    }

    @FXML private void loadTutorial() throws Exception {
        btnSinglePlay.setDisable(true);
        main.startHostedRace(selectedCourse, DEFAULT_PORT);
        Thread.sleep(200);
        main.loadRaceView(true);
    }

    /**
     * Allows user to host a game at the DEFAULT_PORT and current public IP
     * @throws Exception
     */
    @FXML private void loadOfflinePlay() throws Exception{
        btnSinglePlay.setDisable(true);
        main.startHostedRace(selectedCourse, DEFAULT_PORT);
        Thread.sleep(200);
        main.loadRaceView(true);
    }

    /**
     * Allows user to host a game at the entered port and current public IP
     * @throws Exception
     */
    @FXML private void startHostGame() throws Exception{
        main.startHostedRace(selectedCourse, Integer.parseInt(txtPortNumber.getText()));
        Thread.sleep(200);
        main.loadRaceView(true);
    }

    @FXML private void hostGame(){
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
            boolean clientStarted = main.startClient(ipAddress, portNumber, isParticipant);
            if(clientStarted){
                Thread.sleep(200);
                main.loadRaceView(false);
            }
        }else{
            if(ConnectionUtils.IPRegExMatcher(txtIPAddress.getText()) && !txtIPAddress.getText().isEmpty()){
                txtIPAddress.setStyle("-fx-text-inner-color: red;");
            }
            if(validatePort() && !txtPortNumber.getText().isEmpty()){
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
        addButtonListeners(btnLiveGame);
        addButtonListeners(btnPractise);
        addButtonListeners(btnTutorial);
        addButtonListeners(btnHost);
        addButtonListeners(btnSpectate);
        addButtonListeners(btnJoin);
        addButtonListeners(btnBack);
        addButtonListeners(btnSinglePlay);
        addButtonListeners(btnPractiseStart);
        addButtonListeners(btnBackPrac);
        addButtonListeners(btnCourseStart);
        addButtonListeners(btnBackHost);
    }

    private void setImageAnimations(){
        addImageListeners(AC35);
        addImageListeners(LakeTekapo);
        addImageListeners(LakeTaupo);
        addImageListeners(AC33);
        addImageListeners(Athens);
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

}
