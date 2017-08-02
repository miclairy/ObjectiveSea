package seng302.controllers;

import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import seng302.utilities.AnimationUtils;

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
    @FXML ImageView LakeTekapo;
    @FXML ImageView LakeTaupo;
    @FXML ImageView AC33;

    private String selectedCourse = "AC35-course.xml"; //default to the AC35


    DropShadow ds = new DropShadow( 20, Color.AQUA );


    private Main main;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setButtonAnimations();
        setImageAnimations();
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

    @FXML private void backToLiveGame(){
        liveGameGrid.setVisible(true);
        AnimationUtils.slideOutTransition(courseGrid);
        AnimationUtils.slideInTransition(liveGameGrid);
    }

    @FXML private void loadOfflinePlay() throws Exception{
        main.startPrivateRace(selectedCourse);
        while(!Client.isConnected()){
        }
        Thread.sleep(200);
        main.loadRaceView(true);
    }

    @FXML private void startHostGame() throws Exception{
        main.startPrivateRace(selectedCourse);
        while(!Client.isConnected()){
        }
        Thread.sleep(200);
        main.loadRaceView(true);
    }

    @FXML private void hostGame(){
        courseGrid.setVisible(true);
        AnimationUtils.slideOutTransition(liveGameGrid);
        AnimationUtils.slideInTransition(courseGrid);
    }

    @FXML private void joinGame() throws Exception{
        validateIP();
        validatePort();
        if(validateIP() && validatePort()){
            String ipAddress = txtIPAddress.getText();
            int portNumber = Integer.parseInt(txtPortNumber.getText());
            main.startClient(ipAddress, portNumber, true);
            Thread.sleep(200);
            main.loadRaceView(false);
        }
    }

    @FXML private void spectateGame() throws Exception{
        validateIP();
        validatePort();
        if(validateIP() && validatePort()){
            String ipAddress = txtIPAddress.getText();
            int portNumber = Integer.parseInt(txtPortNumber.getText());
            main.startClient(ipAddress, portNumber, false);
            Thread.sleep(200);
            main.loadRaceView(false);
        }
    }

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
    }

    private void setLabelPromptAnimations(){
        addShiftPromptListener(txtIPAddress, lblIP);
        addShiftPromptListener(txtPortNumber, lblPort);
    }

    private void addShiftPromptListener(TextField field, Label label){
        field.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue && field.getText().isEmpty()) {
                AnimationUtils.shiftPromptLabel(label);
            }else if(field.getText().isEmpty()){
                AnimationUtils.shiftPromptLabelBack(label);
            }
        });
    }

    private void focusState(boolean value) {

    }

    private void addButtonListeners(Button button){
        button.addEventHandler(MouseEvent.MOUSE_ENTERED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        AnimationUtils.scaleButtonHover(button);
                    }
                });

        button.addEventHandler(MouseEvent.MOUSE_EXITED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        AnimationUtils.scaleButtonHoverExit(button);
                    }
                });
    }

    private void addImageListeners(ImageView imageView) {


        imageView.setOnMouseClicked( ( MouseEvent event ) ->
        {
            imageView.requestFocus();
        } );
        imageView.focusedProperty().addListener(( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) ->
        {
            if ( newValue )
            {
                imageView.setEffect( ds );
                selectedCourse = imageView.getId() + "-course.xml";
            }
            else
            {
                imageView.setEffect( null );
            }
        });

    }

    private boolean validateIP(){
        String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(txtIPAddress.getText());
        if (matcher.find()) {
            txtIPAddress.setStyle("-fx-text-inner-color: #2a2a2a;");
            return true;
        } else{
            txtIPAddress.setStyle("-fx-text-inner-color: red;");
            return false;
        }
    }

    private boolean validatePort(){
        try {
            int port = Integer.parseInt(txtPortNumber.getText());
            txtPortNumber.setStyle("-fx-text-inner-color: #2a2a2a;");
            if(port > 1024 && port < 65536){
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            txtPortNumber.setStyle("-fx-text-inner-color: red;");
            return false;
        }
    }

}
