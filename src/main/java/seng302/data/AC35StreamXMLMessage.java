package seng302.data;

/**
 * Created by Michael Trotter on 4/29/2017.
 */
public enum AC35StreamXMLMessage {

    REGATTA_XML_MESSAGE(5), RACE_XML_MESSAGE(6), BOAT_XML_MESSAGE(7);

    private final int type;

    AC35StreamXMLMessage(int type){
        this.type = type;
    }

    public int getType(){
        return this.type;
    }

}
