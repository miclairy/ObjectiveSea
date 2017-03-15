package seng302;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {

    @FXML
    private Button button;

    public void hello(){
        System.out.println("Hello World");
    }


}