package com.gainengine.model;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class PerformedExercise {
    
    
    private Exercise ex;
    private List<WorkoutSet> sets=new ArrayList<>();
    private boolean deleted;


    public PerformedExercise(Exercise ex){
        
        this.ex=ex;
        deleted=false;
    }

    public List<WorkoutSet> getSets(){
        return this.sets;
    }

    public Exercise getExercise(){
        return ex;
    }

    public UUID getId(){
        return ex.getId();
    }

    public String getName(){
        return ex.getName();
    }

    public String getDisplayName(){
        return (!deleted) ? ex.getName() : "[deleted] "+ex.getName();
    }

    public int getTotalSets(){
        return sets.size();
    }

    public void addSet(double weight,int reps){
        WorkoutSet workSet=new WorkoutSet(weight,reps);
        sets.add(workSet);
    }

    public void addSet(){
        WorkoutSet workSet=new WorkoutSet();
        sets.add(workSet);
    }

    public void addSet(WorkoutSet s){
        sets.add(s);
    }

    public void removeSet(int idx){
        validateIndex(idx);
        sets.remove(idx);
    }

    public void updateSet(int idx,double weight,int reps){
        validateIndex(idx);
        WorkoutSet s = sets.get(idx);
        s.setReps(reps);
        s.setWeight(weight);
    }

    private void validateIndex(int idx){ //validation logic extraction. To comply with DRY
        //we throw the excpetion in the helper
        if (idx < 0 || idx >= sets.size()) {
            throw new IllegalArgumentException("Set " + (idx + 1) + " doesn't exist.");
        }
    }

    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append("--").append(getName()).append("--\n");
        if (sets.isEmpty()) sb.append("\tNo sets for this exercise yet\n");
        for (int i=0;i<sets.size();i++){
            sb.append(" Set ").append(i+1).append(": ").append(sets.get(i)).append("\n");  
        }
        
        for (int i=0;i<getName().length();i++){
            sb.append("-");
        }
        sb.append("----");
        return sb.toString();
    }

    public int calculateTotalVolume(){
        int sum=0;
        for (WorkoutSet s: sets){
            sum+=s.getWeight()*s.getReps();
        }
        return sum;

    }

    public void markDeleted(){
        deleted=true;
    }

    
    
}
