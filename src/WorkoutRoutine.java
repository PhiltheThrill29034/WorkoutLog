import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class WorkoutRoutine {
    private String routineName;
    private List<Exercise> routine;
    


    public WorkoutRoutine(String name){
        this.routineName=name;
        routine=new ArrayList<>();
            
    }

    public List<Exercise> getExerciseList(){
        return this.routine;
    }

    public WorkoutRoutine(WorkoutRoutine other){
        this.routineName=other.routineName;
        for (Exercise e: other.routine){
            routine.add(Exercise.freshCopyOf(e));
        }
    }

    public Exercise getExercise(int index){
        return routine.get(index);
    }

    public boolean contains(Exercise e){
        if (e==null) return false;
        for (Exercise ex: routine){
            if (e.equals(ex)){
                return true;
            }
        }
        return false;
    }

    public boolean addExercise(Exercise ex){
        if (contains(ex)) return false;
        routine.add(ex);
        return true;
    }

    

    public void removeExercise(int index){
        if (index<0 || index>=routine.size()) {
            System.out.println("Invalid exercise number.");
            return;
        }
        routine.remove(index);
    }
    public void removeExercise(Exercise e){

        for (int i=0;i<routine.size();i++){
            if (routine.get(i).equals(e)){
                routine.remove(i);
                return;
            }
        }

        System.out.println("Exercise doesn't exist in current routine.");
    }

    public String getExercises(){
        StringBuilder sb=new StringBuilder();
        if (getRoutineSize()==0){
            sb.append("No exercises for this routine\n");
            return sb.toString();
        }
        int count=1;
        for (Exercise ex: routine){
            sb.append(count).append(". ").append(ex.getName()).append("\n");
            count++;
        }
        return sb.toString();
    }

    public String toString(){

        StringBuilder sb=new StringBuilder();
        sb.append("Routine: ").append(getName()).append("\n");
        if (getRoutineSize()==0) return sb.toString()+"No exercises for this routine";
        for (Exercise ex: routine){
            sb.append("\t").append(ex).append("\n");
        }
        return sb.toString();
    }

    public String getName(){
        return this.routineName;
    }
    
    public void setName(String name){
        this.routineName=name;
    }

    public int getRoutineSize(){
        return routine.size();
    }



    

}
