package seng302.models;

import javafx.util.Pair;

import java.util.ArrayList;

/**
 * Created by Gemma on 10/05/17.
 * Class to hold the boat readPolars neatly
 */
public class Polars {

    private int TWS;
    private Pair<Double,Double> upWindOptimum = new Pair<Double,Double>(0.0,0.0);
    private Pair<Double,Double> downWindOptimum = new Pair<Double,Double>(0.0,0.0);
    private ArrayList<Pair<Double,Double>> TWAandBSP = new ArrayList<>();

    public Polars(int TWS) {
        this.TWS = TWS;
    }

    public void addTWAandBSP(Double TWA, Double BSP){
        Pair<Double,Double> angleAndSpeed = new Pair<>(TWA,BSP);
        TWAandBSP.add(angleAndSpeed);
    }


    public int getTWS() {
        return TWS;
    }

    public Pair<Double, Double> getUpWindOptimum() {
        return upWindOptimum;
    }

    public Pair<Double, Double> getDownWindOptimum() {
        return downWindOptimum;
    }

    public ArrayList<Pair<Double, Double>> getTWAandBSP() {
        return TWAandBSP;
    }

    /**
     * A setter for the optimum upwind TWA and BSP of the boats, not all readPolars will have this
     * @param upWindOptimum
     */
    public void setUpWindOptimum(Pair<Double, Double> upWindOptimum) {
        this.upWindOptimum = upWindOptimum;
    }

    /**
     * A setter for the optimum downiwnd TWA and BSP of the boats, not all readPolars will have this
     * @param downWindOptimum
     */
    public void setDownWindOptimum(Pair<Double, Double> downWindOptimum) {
        this.downWindOptimum = downWindOptimum;
    }

    /**
     * Checks if the supplied boat polar already gave optimum (in this case no interpolaiton needed)
     * @return true if TWA and Bsp supplied
     */
    public Boolean hasUpwindOptimum(){
        if(upWindOptimum.getKey() != 0 && upWindOptimum.getValue() != 0){
            return true;
        }
        return false;
    }

    /**
     * Checks if the supplied boat polar already gave optimum (in this case no interpolaiton needed)
     * @return true if TWA and Bsp supplied
     */
    public Boolean hasDownwindOptimum(){
        if(downWindOptimum.getKey() != 0 && downWindOptimum.getValue() != 0){
            return true;
        }
        return false;
    }
}
