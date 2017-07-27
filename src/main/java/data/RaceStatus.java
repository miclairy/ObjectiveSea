package src.main.java.data;

/**
 * Created by Michael Trotter on 4/29/2017.
 */
public enum RaceStatus {
    NOT_ACTIVE(0), WARNING(1), PREPARATORY(2), STARTED(3), FINISHED(4), RETIRED(5),
    ABANDONED(6), TERMINATED(8), RACE_START_TIME_NOT_SET(9), PRESTART(10);

    private final int value;

    RaceStatus(int value){ this.value = value; }

    public int getValue() {return value;}

    public boolean isRaceEndedStatus(){
        return this.equals(FINISHED) || this.equals(RETIRED) || this.equals(ABANDONED) || this.equals(TERMINATED);
    }

    public static RaceStatus fromInteger(int value){
        switch(value){
            case 0:
                return NOT_ACTIVE;
            case 1:
                return WARNING;
            case 2:
                return PREPARATORY;
            case 3:
                return STARTED;
            case 4:
                return FINISHED;
            case 5:
                return RETIRED;
            case 6:
                return ABANDONED;
            case 8:
                return TERMINATED;
            case 9:
                return RACE_START_TIME_NOT_SET;
            case 10:
                return PRESTART;
            default:
                return null;
        }
    }
}
