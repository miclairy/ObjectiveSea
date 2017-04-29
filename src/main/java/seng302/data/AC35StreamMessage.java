package seng302.data;

/**
 * Created by Michael Trotter on 4/29/2017.
 */
public enum AC35StreamMessage {
    XML_MESSAGE(26), BOAT_LOCATION_MESSAGE(37, 56), MARK_ROUNDING_MESSAGE(38, 21), RACE_STATUS_MESSAGE(12);

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

}
