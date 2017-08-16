package seng302.controllers;

import seng302.data.BoatStatus;
import seng302.data.ClientListener;
import seng302.models.Boat;
import seng302.models.Race;
import seng302.utilities.DisplaySwitcher;
import seng302.utilities.GameSounds;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by cjd137 on 16/08/17.
 */
public class SoundController implements Runnable {

    private boolean finalGameSound = false;
    private boolean firstGameSound = true;
    private boolean firstPlaceTaken = false;
    private boolean lastPlace = false;
    private int clientID;
    private Race race = Client.getRace();
    private int noOfBoats = race.getCompetitors().size();

    public SoundController(int clientID) {
        this.clientID = clientID;
    }


    public void checkPlacing(Boat boat) {
        if (!finalGameSound && boat.getId().equals(clientID)) {
            if (boat.getCurrPlacing() == 0 && boat.getStatus().equals(BoatStatus.FINISHED) && !firstPlaceTaken) {
                DisplaySwitcher.getGameSounds().firstPlace();
                DisplaySwitcher.getGameSounds().playGameSound();
                finalGameSound = true;
            } else if (boat.getCurrPlacing() != 0 && boat.getCurrPlacing() != noOfBoats - 1 &&
                    boat.getStatus().equals(BoatStatus.FINISHED) && firstPlaceTaken) {
                DisplaySwitcher.getGameSounds().everyoneButFirstPlace();
                DisplaySwitcher.getGameSounds().playGameSound();
                finalGameSound = true;
            }
            if ((boat.getCurrPlacing() == noOfBoats - 1 && boat.getStatus().equals(BoatStatus.FINISHED)) ||
                    (boat.getStatus().equals(BoatStatus.DISQUALIFIED) || boat.getStatus().equals(BoatStatus.DNF)) && !lastPlace) {
                DisplaySwitcher.getGameSounds().lastPlace();
                DisplaySwitcher.getGameSounds().playGameSound();
                lastPlace = true;
            }
        }
        if (!firstPlaceTaken && boat.getStatus().equals(BoatStatus.FINISHED)) {
            firstPlaceTaken = true;
        }
    }

    public void initialReadySound() {
        if(firstGameSound) {
            DisplaySwitcher.getGameSounds().preRace();
            DisplaySwitcher.getGameSounds().playGameSound();
            firstGameSound = false;
        }
    }

    public void hasHitBoat(Boat boat) {
        if ((System.currentTimeMillis() - boat.getTimeSinceLastCollision() > 5000) && boat.isBoatCollideSound()) {
            if ((boat.getBoatHealth() <= 20) && boat.getId().equals(clientID)) {
                DisplaySwitcher.getGameSounds().boatDamage();
                DisplaySwitcher.getGameSounds().playGameSound();
            } else {
                DisplaySwitcher.getGameSounds().hitBoat();
                DisplaySwitcher.getGameSounds().playGameSound();
            }
            boat.setBoatCollideSound(false);
            boat.setTimeSinceLastCollision(System.currentTimeMillis());
        }
    }

    public void hasHitMark(Boat boat) {
        if ((System.currentTimeMillis() - boat.getTimeSinceLastCollision() > 5000) && boat.isMarkCollideSound()) {
            if (boat.getBoatHealth() <= 20 && boat.getId().equals(clientID)) {
                DisplaySwitcher.getGameSounds().boatDamage();
                DisplaySwitcher.getGameSounds().playGameSound();
            } else {
                DisplaySwitcher.getGameSounds().hitMark();
                DisplaySwitcher.getGameSounds().playGameSound();
            }
            boat.setMarkCollideSound(false);
            boat.setTimeSinceLastCollision(System.currentTimeMillis());
        }
    }

    @Override
    public void run() {
        while(true) {
            for (Boat boat : race.getCompetitors()) {
                initialReadySound();
                checkPlacing(boat);
                hasHitBoat(boat);
                hasHitMark(boat);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
