package seng302.data;

/**
 * Created by mjt169 on 25/07/17.
 */
public enum YachtEventCode {
    COLLISION(1), COLLISION_PENALTY(2);

    private int code;

    YachtEventCode(int code) {
        this.code = code;
    }

    public int code(){
        return this.code;
    }

}
