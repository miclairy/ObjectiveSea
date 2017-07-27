package src.main.java.data;

/**
 * Created by Michael Trotter on 4/29/2017.
 */
public enum BoatStatus {
    UNDEFINED(0), PRERACE(1), RACING(2), FINISHED(3), DNS(4), DNF(5), DISQUALIFIED(6), OCS(7); //OCS = On Course Side, i.e. crossed start line early

    private final int value;

    BoatStatus(int value){ this.value = value; }

    public int getValue() {return value;}
}
