package seng302.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import seng302.utilities.AnimationUtils;

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
    @FXML GridPane liveGameGrid;
    @FXML GridPane btnGrid;
    @FXML GridPane practiceGrid;
    @FXML TextField txtIPAddress;
    @FXML TextField txtPortNumber;

    private Main main;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setButtonAnimations();
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

    @FXML private void loadOfflinePlay() throws Exception{
        main.startPrivateRace();
        while(!Client.isConnected()){
        }
        Thread.sleep(200);
        main.loadRaceView();
    }

    @FXML private void hostGame() throws Exception{
        main.startPrivateRace();
        while(!Client.isConnected()){
        }
        Thread.sleep(200);
        main.loadRaceView();
    }

    @FXML private void joinGame() throws Exception{
        String ipAddress = txtIPAddress.getText();
        int portNumber = Integer.parseInt(txtPortNumber.getText());
        main.startClient(ipAddress, portNumber);
        Thread.sleep(200);
        main.loadRaceView();
    }

    private void setButtonAnimations(){
        addButtonListeners(btnLiveGame);
        addButtonListeners(btnPractise);
        addButtonListeners(btnTutorial);
        addButtonListeners(btnHost);
        addButtonListeners(btnSpectate);
        addButtonListeners(btnJoin);
        addButtonListeners(btnBack);
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
}
