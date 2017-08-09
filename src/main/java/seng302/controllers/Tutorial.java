package seng302.controllers;

import javafx.scene.input.KeyCode;
import seng302.models.Boat;
import seng302.models.PolarTable;
import seng302.models.Race;
import seng302.utilities.PolarReader;

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
    public enum TutorialStage { UPWINDDOWNWIND, VMG, TACK, GYBE, SAILSIN, SAILSOUT, END, TACKFAIL }
    private int tutorialUpwindDownwindCounter = 0;
    private int UPWIND_DOWNWIND_TIME = 60;
    private Boat tutorialBoat;
    private PolarTable polarTable;


    private TutorialStage tutorialStage = TutorialStage.UPWINDDOWNWIND;
    private TutorialStage stepFailed;
    private List<KeyCode> keysFailed;



    public Tutorial(Controller controller, Race race){
        this.controller = controller;
        this.race = race;
        if (!race.getCompetitors().isEmpty()) tutorialBoat = race.getCompetitors().get(0);
        polarTable = new PolarTable(PolarReader.getPolarsForAC35Yachts(), race.getCourse());
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
                controller.showTutorialOverlay("Complete", "Tutorial is complete. Become familiar with the controls before you race online.\n\n" +
                        "UP or DOWN - upwind or downwind\n" +
                        "ENTER - tack or gybe\n" +
                        "SHIFT - sails in or out\n" +
                        "SPACE - VMG autopilot\n\n" +
                        "Press the button under the scoreboard to return to the Menu.");
                break;
            case TACKFAIL:
                tackFailTutorial();
        }

    }
    private void upwindTutorial(){
        controller.showTutorialOverlay("Upwind/Downwind", "Welcome to the Objective Sea Tutorial. Follow along to master all the keys. \n\n" +
                "Press the UP and DOWN arrow keys to turn the boat to the upwind or downwind direction. \n\n" +
                "Note the wind direction shown on the indicator to the right.");
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
        controller.showTutorialOverlay("AutoPilot", "The VMG is the angle between your boat and the wind that allows for the maximum speed.\n\n" +
                "Press SPACE to automatically move your boat to the nearest VMG angle.\n\n" +
                "If you are within 45 degrees of the wind direction, you may be in a no-sail zone. You will not automatically move to a VMG at this angle.");
        Client.setTutorialActions(keycodes, () -> {
            double vmhHeading = tutorialBoat.getVMGHeading(race.getCourse(), polarTable);
            if(vmhHeading != boatHeading){
                //while(tutorialBoat.getTargetHeading() != boatHeading){}
                tutorialStage = TutorialStage.TACK;
            }else{
                stepFailed = tutorialStage;
                keysFailed = keycodes;
                tutorialStage = TutorialStage.TACKFAIL;
            }
        });
    }

    private void tackFailTutorial(){
        double boatHeading = tutorialBoat.getHeading();
        Client.clearTutorialAction();

        controller.showTutorialOverlay("Try Again", "Your boat did not move. \n\nYou are heading towards the wind in the no-sail zone (45 degrees either side of the wind direction).\n\n" +
                "Try use the UP and DOWN keys to move yourself to the away from the wind direction and try the key again");
        Client.setTutorialActions(keysFailed, () -> {
            double vmhHeading = tutorialBoat.getVMGHeading(race.getCourse(), polarTable);
            if(vmhHeading != boatHeading){
                //while(tutorialBoat.getTargetHeading() != boatHeading){}
                tutorialStage = TutorialStage.values()[stepFailed.ordinal() + 1];
            }
        });


    }

    private void tackGybeTutorial(boolean hasGybe){
        double boatHeading = tutorialBoat.getHeading();
        List<KeyCode> keycodes = new ArrayList<KeyCode>();
        Client.clearTutorialAction();
        keycodes.add(KeyCode.ENTER);
        if(!hasGybe){
            controller.showTutorialOverlay("Tack/Gybe", "Tacking and Gybing are manoeuvre that flips the heading of your boat towards the opposite VMG.\n\n" +
                    "if you are not in the no-sail zone, press ENTER now to tack or gybe.");
            Client.setTutorialActions(keycodes, () -> {
                double vmhHeading = tutorialBoat.getVMGHeading(race.getCourse(), polarTable);
                if(vmhHeading != boatHeading){
                    //while(tutorialBoat.getTargetHeading() != boatHeading){}
                    tutorialStage = TutorialStage.GYBE;
                }else{
                    stepFailed = tutorialStage;
                    keysFailed = keycodes;
                    tutorialStage = TutorialStage.TACKFAIL;
                }
            });
        }else{
            controller.showTutorialOverlay("Tack/Gybe", "Nice work. Lets do that again.\n\nTacking and Gybing are manoeuvre that flips the heading of your boat towards the opposite VMG.\n\n" +
                    "if you are not in the no-sail zone, press ENTER now to tack or gybe.");
            Client.setTutorialActions(keycodes, () -> {
                double vmhHeading = tutorialBoat.getVMGHeading(race.getCourse(), polarTable);
                if(vmhHeading != boatHeading){
                    //while(tutorialBoat.getTargetHeading() != boatHeading){}
                    tutorialStage = TutorialStage.SAILSIN;
                }else{
                    stepFailed = tutorialStage;
                    keysFailed = keycodes;
                    tutorialStage = TutorialStage.TACKFAIL;
                }
            });
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
