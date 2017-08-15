package seng302.views;

import java.util.*;

/**
 * Created by atc60 on 15/08/17.
 */
public class ArrowOrderGraph {

    private Map<Arrow, ArrayList<Arrow>> arrowGraph;
    private Set<Arrow> currentArrows;
    private Arrow startingArrow;

    ArrowOrderGraph(){
        arrowGraph = new HashMap<>();
        currentArrows = new HashSet<>();
    }

    public void moveToNextArrows(){
        if(currentArrows.size() == 0){
            currentArrows.add(startingArrow);
        } else{
            Set<Arrow> nextArrows = new HashSet<>();
            for(Arrow arrow : currentArrows){
                for(Arrow nextArrow : arrowGraph.get(arrow)){
                    nextArrows.add(nextArrow);
                }
            }
            currentArrows = nextArrows;
        }
    }

    public void addArrowEdge(Arrow from, Arrow to){
        if(!arrowGraph.containsKey(from)){
            arrowGraph.put(from, new ArrayList<>());
        }
        if(!arrowGraph.containsKey(to)){
            arrowGraph.put(to, new ArrayList<>());
        }
        arrowGraph.get(from).add(to);
    }

    public void setStartingArrow(Arrow startingArrow) {
        this.startingArrow = startingArrow;
    }

    public Set<Arrow> getCurrentArrows() {
        return Collections.unmodifiableSet(currentArrows);
    }

    public void addEdges(List<Arrow> arrowList) {
        for(int i = 1; i < arrowList.size(); i++){
            addArrowEdge(arrowList.get(i-1), arrowList.get(i));
        }
    }

    public Set<Arrow> getAllArrows(){
        return arrowGraph.keySet();
    }
}
