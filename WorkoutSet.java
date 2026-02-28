

class WorkoutSet {

    private int reps;
    private double weight;

    public WorkoutSet(double weight,int reps){
        validate(reps, "Reps cannot be negative you fatass. Skipping again?");
        validate(weight, "What are you lifting bro? Helium??");
        //internal documentation of personality
        //these message strings , when I watch them in 6 months, will be the only thing keepeing me sane
        this.reps=reps;
        this.weight=weight;
    }

    public WorkoutSet(){
        this.reps=0;
        this.weight=0;
    }

    public WorkoutSet(WorkoutSet other){
        this.reps=other.reps;
        this.weight=other.weight;
    }


    public void setReps(int reps){
        this.reps=reps;
        
    }

    public int getReps(){
        return this.reps;
    }

    public void setWeight(double weight){
        this.weight=weight;
    }

    public double getWeight(){
        return this.weight;
    }

    public String toString(){
        return String.format("%dx%.2f",this.reps,this.weight);
    }

    private void validate(double v, String msg){
        if (v<0) throw new IllegalArgumentException(msg);
    }
    

}
