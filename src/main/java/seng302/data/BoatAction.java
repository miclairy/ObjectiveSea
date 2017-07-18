package seng302.data;

import javafx.scene.input.KeyCode;

/**
 * Created by lga50 on 17/07/17.
 *
 */
public enum BoatAction {

    BOAT_AUTOPILOT(1, KeyCode.SPACE), SAILS_IN(2, KeyCode.SHIFT), SAILS_OUT(3, KeyCode.SHIFT), TACK_GYBE(4, KeyCode.ENTER),
    UPWIND(5, KeyCode.PAGE_UP), DOWNWIND(6, KeyCode.PAGE_DOWN);

    private final int type;
    private final KeyCode keycode;

    BoatAction(int type, KeyCode keycode){
        this.type = type;
        this.keycode = keycode;
    }

    public int getType(){
        return this.type;
    }

    public KeyCode getKeycode() {
        return keycode;
    }

    public static int getEnumByInt(KeyCode code){
        int returnCode = -1;
        for (BoatAction action : BoatAction.values()){
            if (action.keycode.equals(code)){
                returnCode = action.type;
            }
        }
        return returnCode;
    }
}
