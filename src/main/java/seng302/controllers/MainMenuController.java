package seng302.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setButtonAnimations();
        btnGrid.setVisible(true);
        liveGameGrid.setVisible(false);
    }

    @FXML
    public void loadLiveGameGrid(){
        liveGameGrid.setVisible(true);
        AnimationUtils.slideOutTransition(btnGrid);
        AnimationUtils.slideInTransition(liveGameGrid);

    }

    @FXML
    public void backToMainMenu(){
        btnGrid.setVisible(true);
        AnimationUtils.slideOutTransition(liveGameGrid);
        AnimationUtils.slideInTransition(btnGrid);
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
