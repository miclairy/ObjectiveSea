package seng302.controllers;

import javafx.scene.input.KeyCode;
import seng302.models.Boat;
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
    private enum TutorialStage { UPWINDDOWNWIND, TACK, TACKFAIL, GYBE, SAILSIN, SAILSOUT, VMG, END }
    private int tutorialUpwindDownwindCounter = 0;
    private int UPWIND_DOWNWIND_TIME = 60;
    private Boat tutorialBoat;

    private TutorialStage tutorialStage = TutorialStage.UPWINDDOWNWIND;


    public Tutorial(Controller controller, Race race){
        this.controller = controller;
        this.race = race;
        if (!race.getCompetitors().isEmpty()) tutorialBoat = race.getCompetitors().get(0);
    }

     public void runTutorial(){
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
                controller.showTutorialOverlay("Complete", "Tutorial is complete. Come back again.\nI hate to see you leaving :( \n\ni love you <3");
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
        if (tutorialUpwindDownwindCounter > UPWIND_DOWNWIND_TIME){
            tutorialStage = TutorialStage.VMG;
        }
    }

    private void vmgTutorial(){
        double boatHeading = tutorialBoat.getHeading();
        List<KeyCode> keycodes = new ArrayList<KeyCode>();
        Client.clearTutorialAction();
        keycodes.add(KeyCode.SPACE);
        controller.showTutorialOverlay("AutoPilot", "Press the SPACE key to move your boat to the VMG line. " +
                "\n\nThis line is the optimum angle from the wind allowing your boat to go its fastest speed.");
        Client.setTutorialActions(keycodes, () -> {
            if(tutorialBoat.getTargetHeading() != boatHeading){
                //while(tutorialBoat.getTargetHeading() != boatHeading){}
                tutorialStage = TutorialStage.TACK;
            }else{
                tackFailTutorial(keycodes, TutorialStage.TACK);
            }
        });
    }

    private void tackFailTutorial(List<KeyCode> keycodes, TutorialStage nextTutorialStage){
        double boatHeading = tutorialBoat.getHeading();
        Client.clearTutorialAction();

        controller.showTutorialOverlay("NEIN", "Your boat did not move. You are heading towards the wind in the no-sail zone (45 degrees either side of the wind direction).\n\n" +
                "Try use the UP and DOWN keys to move yourself to the away from the wind direction and try the key again");
        Client.setTutorialActions(keycodes, () -> {
            if(tutorialBoat.getTargetHeading() != boatHeading){
                //while(tutorialBoat.getTargetHeading() != boatHeading){}
                tutorialStage = nextTutorialStage;
            }else{
                tackFailTutorial(keycodes, nextTutorialStage);
            }
        });


    }

    private void tackGybeTutorial(boolean hasGybe){
        List<KeyCode> keycodes = new ArrayList<KeyCode>();
        Client.clearTutorialAction();
        keycodes.add(KeyCode.ENTER);
        if(!hasGybe){
            controller.showTutorialOverlay("Tack/Gybe", "Press the ENTER key to Tack or Gybe. If you are the right angle to the wind, the boat should perform a tack or gybe maneuver.");
            Client.setTutorialActions(keycodes, () -> tutorialStage = TutorialStage.GYBE);
        }else{
            controller.showTutorialOverlay("Tack/Gybe", "Lets do that again \n\nPress the ENTER key to Tack or Gybe. If you are the right angle to the wind, the boat should perform a tack or gybe maneuver.");
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
