package seng302.controllers;

import javafx.scene.input.KeyCode;
import seng302.models.Race;

import java.util.ArrayList;
import java.util.List;

import static seng302.data.RaceStatus.PREPARATORY;
import static seng302.data.RaceStatus.STARTED;

/**
 * Created by sjh298 on 8/08/17.
 */
public class Tutorial {

    private Controller controller;
    private Race race;
    private enum TutorialStage { UPWINDDOWNWIND, TACK, GYBE, SAILSIN, SAILSOUT, VMG, END }
    private int tutorialUpwindDownwindCounter = 0;
    private TutorialStage tutorialStage = TutorialStage.UPWINDDOWNWIND;


    public Tutorial(Controller controller, Race race){
        this.controller = controller;
        this.race = race;
    }

     public void runTutorial(){

        if(race.getRaceStatus().equals(PREPARATORY)){
            controller.showTutorialOverlay("Welcome!","Watch this box to learn the keys!");
            Client.setTutorialActions(new ArrayList<KeyCode>(), null);

        } else if(race.getRaceStatus().equals(STARTED)) {

            switch(tutorialStage){
                case UPWINDDOWNWIND:
                   upwindTutorial();
                    break;
                case VMG:
                    vmgTutorial();
                    break;
                case TACK:
                    tackGybeTutorial(false);
                    break;
                case GYBE:
                    tackGybeTutorial(true);
                    break;
                case SAILSIN:
                    sailsTutorial(false);
                    break;
                case SAILSOUT:
                    sailsTutorial(true);
                    break;
                case END:
                    Client.clearTutorialAction();
                    controller.showTutorialOverlay("Complete", "Tutorial is complete.");

            }


        }

    }
    private void upwindTutorial(){
        controller.showTutorialOverlay("Upwind/Downwind", "press the UP and DOWN arrow keys to turn the boat to the upwind or downwind direction. \n\nNote the wind direction shown in the side indicator.");
        List<KeyCode> keycodes = new ArrayList<KeyCode>();
        keycodes.add(KeyCode.PAGE_DOWN);
        keycodes.add(KeyCode.DOWN);
        keycodes.add(KeyCode.UP);
        keycodes.add(KeyCode.PAGE_UP);
        Client.setTutorialActions(keycodes, () -> tutorialUpwindDownwindCounter++);
        if (tutorialUpwindDownwindCounter > 60){
            tutorialStage = TutorialStage.VMG;
        }
    }

    private void vmgTutorial(){
        List<KeyCode> keycodes = new ArrayList<KeyCode>();
        Client.clearTutorialAction();
        keycodes.add(KeyCode.SPACE);
        controller.showTutorialOverlay("AutoPilot", "Press the SPACE key to move your boat to the VMG line. \n\nThis line is the optimum angle from the wind allowing your boat to go its fastest speed.");
        Client.setTutorialActions(keycodes, () -> tutorialStage = TutorialStage.TACK);
    }

    private void tackGybeTutorial(boolean hasGybe){
        List<KeyCode> keycodes = new ArrayList<KeyCode>();
        Client.clearTutorialAction();
        keycodes.add(KeyCode.ENTER);
        if(!hasGybe){
            controller.showTutorialOverlay("Tack/Gybe", "Press the ENTER key to Tack or Gybe. If you are the right angle to the wind, the boat should perform a tack or gybe maneuver.");
            Client.setTutorialActions(keycodes, () -> tutorialStage = TutorialStage.GYBE);
        }else{
            controller.showTutorialOverlay("Tack/Gybe", "Try that again \n\nPress the ENTER key to Tack or Gybe. If you are the right angle to the wind, the boat should perform a tack or gybe maneuver.");
            Client.setTutorialActions(keycodes, () -> tutorialStage = TutorialStage.SAILSIN);
        }

    }

    private void sailsTutorial(boolean isOut){
        List<KeyCode> keycodes = new ArrayList<KeyCode>();
        Client.clearTutorialAction();
        keycodes.add(KeyCode.SHIFT);
        if(!isOut){
            controller.showTutorialOverlay("Sails In", "Press the SHIFT key to bring your sails in. \n\nThis should cause your boat to luff and lose all velocity.");
            Client.setTutorialActions(keycodes, () -> tutorialStage = TutorialStage.SAILSOUT);
        } else{
            controller.showTutorialOverlay("Sails Out", "Press the SHIFT key to put your sails out. \n\nThis should cause your boat to start gaining velocity.");
            Client.setTutorialActions(keycodes, () -> tutorialStage = TutorialStage.END);
        }

    }
}
