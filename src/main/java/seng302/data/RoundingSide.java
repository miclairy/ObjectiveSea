package seng302.data;

/**
 * Created by atc60 on 15/08/17.
 */
public enum RoundingSide {
    PORT("Port"), STBD("STBD"), PORT_STBD("PS"), STBD_PORT("SP");

    private String roundingSideString;

    RoundingSide(String roundingSideString){
        this.roundingSideString = roundingSideString;
    }

    public static RoundingSide parseRoundingSide(String roundingSideString){
        roundingSideString = roundingSideString.toLowerCase();
        for(RoundingSide roundingSide : RoundingSide.values()){
            if(roundingSide.getRoundingSideString().toLowerCase().equals(roundingSideString)){
                return roundingSide;
            }
        }
        return null;
    }

    public String getRoundingSideString() {
        return roundingSideString;
    }
}
