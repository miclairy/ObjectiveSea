package seng302.controllers;

import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import seng302.utilities.AnimationUtils;
import seng302.utilities.Config;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @FXML GridPane liveGameGrid;
    @FXML GridPane btnGrid;
    @FXML GridPane practiceGrid;
    @FXML TextField txtIPAddress;
    @FXML TextField txtPortNumber;
    @FXML Label lblIP;
    @FXML Label lblPort;

    private Main main;

    /**
     * Initilizer for the Main Menu Controller. Runs upon creation
     * @param location url locaton
     * @param resources bundles of fun
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setButtonAnimations();
        setLabelPromptAnimations();
        btnGrid.setVisible(true);
        liveGameGrid.setVisible(false);
        practiceGrid.setVisible(false);
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

    /**
     * Allows user to host a game at the port 2828 and current public IP
     * @throws Exception
     */
    @FXML private void loadOfflinePlay() throws Exception{
        main.startPrivateRace(2828);
        while(!Client.isConnected()){
        }
        Thread.sleep(200);
        main.loadRaceView(true);
    }

    /**
     * Allows user to host a game at the entered port and current public IP
     * @throws Exception
     */
    @FXML private void hostGame() throws Exception{
        validatePort();
        if(validatePort()){
            main.startPrivateRace(Integer.parseInt(txtPortNumber.getText()));
            while(!Client.isConnected()){
            }
            Thread.sleep(200);
            main.loadRaceView(true);
            txtPortNumber.setStyle("-fx-text-inner-color: 2a2a2a;");
        }else{
            txtPortNumber.setStyle("-fx-text-inner-color: red;");
        }
    }

    /**
     * Joins a race at the desired IP and Port and creates a client instance
     * @throws Exception
     */
    @FXML private void joinGame() throws Exception{
        if(Config.IPRegExMatcher(txtIPAddress.getText()) && validatePort()){
            String ipAddress = txtIPAddress.getText();
            int portNumber = Integer.parseInt(txtPortNumber.getText());
            boolean clientStarted = main.startClient(ipAddress, portNumber, true);
            if(clientStarted){
                Thread.sleep(200);
                main.loadRaceView(false);
            }
        }else{
            if(!Config.IPRegExMatcher(txtIPAddress.getText())){
                txtIPAddress.setStyle("-fx-text-inner-color: red;");
            }
            if(!validatePort()){
                txtPortNumber.setStyle("-fx-text-inner-color: red;");
            }
        }
    }

    /**
     * Joins a race at the desired IP and Port and creates a client instance
     * @throws Exception
     */
    @FXML private void spectateGame() throws Exception{
        if(Config.IPRegExMatcher(txtIPAddress.getText()) && validatePort()){
            String ipAddress = txtIPAddress.getText();
            int portNumber = Integer.parseInt(txtPortNumber.getText());
            boolean clientStarted = main.startClient(ipAddress, portNumber, false);
            if(clientStarted){
                Thread.sleep(200);
                main.loadRaceView(false);
            }
        }else{
            if(Config.IPRegExMatcher(txtIPAddress.getText())){
                txtIPAddress.setStyle("-fx-text-inner-color: red;");
            }
            if(validatePort()){
                txtPortNumber.setStyle("-fx-text-inner-color: red;");
            }
        }
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
                AnimationUtils.shiftPromptLabel(label);
            }else if(field.getText().isEmpty()){
                AnimationUtils.shiftPromptLabelBack(label);
            }
        });
    }

    /**
     * attaches click and hover listeners to buttons
     * @param button the button to attach the listener
     */
    private void addButtonListeners(Button button){
        button.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> AnimationUtils.scaleButtonHover(button));

        button.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> AnimationUtils.scaleButtonHoverExit(button));
    }

    /**
     * checks to determine whether the port number is a valid regex. Colors the port number accordingly.
     * @return whether the port is valid or not
     */
    private boolean validatePort(){
        try {
            int port = Integer.parseInt(txtPortNumber.getText());
            txtPortNumber.setStyle("-fx-text-inner-color: #2a2a2a;");
            if(port > 1024 && port < 65536){
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
