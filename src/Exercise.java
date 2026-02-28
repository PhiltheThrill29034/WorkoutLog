//package java_practice.WorkoutLog;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Set;
import java.util.EnumSet;
import java.util.UUID;



class Exercise {

    
    
    private final ExerciseType type;
    private final String customName; //null if exercise is a ready type. 
    private final String customDesc;
    private final EnumSet<Muscles> customMusclesTrained;

    private final UUID id;

    
    
    

    

    private Exercise(UUID id,ExerciseType type, String name,String desc, Set<Muscles> muscles){
        
        this.id=id;
        this.type=type;
        this.customName=name;
        this.customDesc=desc;
        this.customMusclesTrained=(muscles!=null) ? EnumSet.copyOf(muscles) : null;
        
    }

    public static Exercise createCustom(String name,String desc, Set<Muscles> muscles){

        if (name==null||name.isBlank()) 
            throw new IllegalArgumentException("Name cannot be empty");
        if (muscles==null||muscles.isEmpty()) 
            throw new IllegalArgumentException("Muscles cannot be empty.");
        return new Exercise(UUID.randomUUID(),null,name,desc,muscles);
        
    }

    public static Exercise loadCustom(UUID id,String name, String desc, Set<Muscles> muscles){
        if (id==null)
            throw new IllegalArgumentException("Id for loaded custom exercise cannot be null");
        return new Exercise(id, null,name,desc,muscles);
    }

    public static Exercise fromType (ExerciseType type){

        if (type==null) throw new IllegalArgumentException("Exercise Type cannot be empty");
        return new Exercise(UUID.nameUUIDFromBytes(("TYPE: "+type.name()).getBytes()), type, null, null, null);
        
    }

    public static Exercise freshCopyOf(Exercise other){ //copies an exercise, passing the name and muscles. doesn't copy the sets
        return (other.type==null) ? 
        Exercise.loadCustom(other.id,other.customName,other.customDesc,other.customMusclesTrained) :
        Exercise.fromType(other.type);
    }
    
    


    public String getName(){
        return (getType()!=null) ? type.getName() : this.customName+" [CUSTOM]";
    }

    public String getDesc(){
        return (getType()!=null) ? type.getDesc() : this.customDesc;
    }

    public Set<Muscles> getMuscles(){
        return (type==null) ? Set.copyOf(this.customMusclesTrained) : type.getMuscles();
        //defensive copying: return a copy of the set so changes are not reflected in the original. super important
    }

    public ExerciseType getType(){
        return this.type;
    }

    


    

    public String formatMuscles(){ //simple method to print out the muscles trained
        // if (getMuscles()==null) throw new IllegalArgumentException()
        //NO! that would be wrong. Muscles being null is not up to the caller
        //throw the exceptions at the root of the problem
        //see constructior!
        StringBuilder sb=new StringBuilder();
        int count=0;
        for (Muscles m : getMuscles()){
            if (count>0) {
                sb.append(", ").append(m.getName());
            } else {
                sb.append(m.getName());
            }
            
            count++;
        }
        sb.append("\n");
        return sb.toString();
    }

    public String details(){ //returns the exercise details in a nice way
        StringBuilder sb= new StringBuilder();
        sb.append("=====EXERCISE DETAILS=====\n");
        sb.append("Name : ").append(getName()).append("\n");
        sb.append("Muscles Trained : \n");
        sb.append(formatMuscles()).append("\n");
        sb.append("Description:\n").append(getDesc());
        sb.append("==========================\n");
        return sb.toString();

    }

    @Override 
    public boolean equals(Object other){ //to override, other must be of type object
        if (this==other) return true;
        if (!(other instanceof Exercise o)) return false; //If o is an Exercise, then cast it to Exercise and name it other.

        return this.getIdentity().equals(o.getIdentity());


    }

    @Override
    public int hashCode() {
        
        return Objects.hash(
            getIdentity()
        );
    }

    public UUID getId() { return id; }


    public String getIdentity(){ return id.toString(); }

    

   

}   