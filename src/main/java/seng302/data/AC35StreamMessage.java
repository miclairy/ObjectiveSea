package seng302.data;

/**
 * Created by Michael Trotter on 4/29/2017.
 *
 */
public enum AC35StreamMessage {
    REGISTRATION_REQUEST(55, 4), REGISTRATION_ACCEPT(56, 4), XML_MESSAGE(26), BOAT_LOCATION_MESSAGE(37, 56),
    MARK_ROUNDING_MESSAGE(38, 21), RACE_STATUS_MESSAGE(12), UNKNOWN(0), BOAT_ACTION_MESSAGE(100, 1),
    YACHT_EVENT_CODE(29, 22);

    private final int type, length;

    AC35StreamMessage(int type, int length){
        this.type = type;
        this.length = length;
    }

    AC35StreamMessage(int type){
        this.type = type;
        this.length = -1;
    }

    public int getValue(){
        return this.type;
    }

    public int getLength(){
        return this.length;
    }

    public static AC35StreamMessage fromInteger(int messageTypeValue) {
        switch(messageTypeValue) {
            case 12:
                return RACE_STATUS_MESSAGE;
            case 26:
                return XML_MESSAGE;
            case 37:
                return BOAT_LOCATION_MESSAGE;
            case 38:
                return MARK_ROUNDING_MESSAGE;
            case 100:
                return BOAT_ACTION_MESSAGE;
            case 55:
                return REGISTRATION_REQUEST;
            case 56:
                return REGISTRATION_ACCEPT;
            case 29:
                return YACHT_EVENT_CODE;
            default:
                return UNKNOWN;
        }
    }
}
