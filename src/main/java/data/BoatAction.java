package src.main.java.data;

import javafx.scene.input.KeyCode;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lga50 on 17/07/17.
 *
 */
public enum BoatAction {

    BOAT_VMG(1, KeyCode.SPACE), SAILS_IN(2, KeyCode.SHIFT), SAILS_OUT(3, KeyCode.SHIFT), TACK_GYBE(4, KeyCode.ENTER),
    UPWIND(5, KeyCode.PAGE_UP, KeyCode.UP), DOWNWIND(6, KeyCode.PAGE_DOWN, KeyCode.DOWN);

    private final int type;
    private final Set<KeyCode> keycodes = new HashSet<>();

    BoatAction(int type, KeyCode keycode){
        this.type = type;
        keycodes.add(keycode);
    }

    BoatAction(int type, KeyCode keycode, KeyCode altKeycode){
        this.type = type;
        keycodes.add(keycode);
        keycodes.add(altKeycode);
    }

    public int getType(){
        return this.type;
    }


    public static int getTypeFromKeyCode(KeyCode code){
        int returnCode = -1;
        for (BoatAction action : BoatAction.values()){
            for(KeyCode keycode : action.keycodes) {
                if (keycode.equals(code)) {
                    returnCode = action.type;
                }
            }
        }
        return returnCode;
    }

    public static BoatAction getBoatActionFromInt(int type){
        BoatAction returnAction = null;
        for (BoatAction action : BoatAction.values()){
            if (action.getType() == type){
                returnAction = action;
            }
        }
        return returnAction;
    }
}
