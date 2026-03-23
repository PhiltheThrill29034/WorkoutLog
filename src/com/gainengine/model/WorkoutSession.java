package com.gainengine.model;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.text.NumberFormat;
import java.util.EnumMap;


public class WorkoutSession {
    private String routineName;
    LocalDateTime stamp;
    List <PerformedExercise> sessionExercises=new ArrayList<>();
    EnumMap<PrType,Map<Exercise,Double>> sessionPrs = new EnumMap<>(PrType.class); //enum map, thought : WEIGHT - > weight pr map 
    //VOLUME -> volume pr map
    

    public WorkoutSession(WorkoutRoutine routine,LocalDateTime stamp){ //we add new Exercise objects to the routine. fresh so instances are not shared
        this.routineName=routine.getName();
        this.stamp=stamp; //routine start
        
        sessionPrs = new EnumMap<>(PrType.class);

        
        for (Exercise e: routine.getExerciseList()){

            sessionExercises.add(new PerformedExercise(e));
            
        }
    }

    public WorkoutSession(WorkoutRoutine routine){ //we add new Exercise objects to the routine. fresh so instances are not shared
        this(routine, LocalDateTime.now()); //this is cool
    }

    public WorkoutSession(String routineName, LocalDateTime stamp, List<PerformedExercise> exercises){
        this.routineName=routineName;
        this.stamp=stamp;
        this.sessionExercises=(exercises!=null) ? exercises : new ArrayList<>();
    }



    public String getName(){
        return this.routineName;
    }

    public String getFormattedDate(){
         DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, MMM d yyyy 'at' HH:mm");
         return stamp.format(fmt);
    }

    public LocalDateTime getDate(){
        return stamp;
    }

    public PerformedExercise getSessionExercise(int idx){
        if (idx<0||idx>=sessionExercises.size()){
            throw new IllegalArgumentException("Invalid index for session exercise");
        }
        return sessionExercises.get(idx);
    }

    public void addSet(int exIdx, double weight, int reps){
        if (exIdx<0||exIdx>=sessionExercises.size()){
            throw new IllegalArgumentException("Exercise index "+exIdx+" is invalid.");
        }

        sessionExercises.get(exIdx).addSet(weight,reps);
    }

    public void printSessionExercises(){
        int count=1;
        for (PerformedExercise e: sessionExercises){
            System.out.printf("%d. %s%n",count,e.getName());
            count++;
        }
    }



    public int getSessionSize(){
        return sessionExercises.size();
    }

    public String toString(){
        StringBuilder sb=new StringBuilder();
       
        sb.append("Workout on ").append(getFormattedDate()).append("\n");
        sb.append("=====").append(routineName.toUpperCase()).append("=====\n");
        for (PerformedExercise e : sessionExercises){
            sb.append(e).append("\n");
        }
        sb.append("=====");
        for (int i=0;i<routineName.length();i++){
            sb.append("=");
        }
        sb.append("=====");
        return sb.toString();

    }

    private int calculateTotalVolume(){
        int total=0;
        for (PerformedExercise e: sessionExercises){
            total+=e.calculateTotalVolume();
        }
        return total;
    }

    private int totalSets(){
        int total=0;
        for (PerformedExercise e: sessionExercises){
            total+=e.getTotalSets();
        }
        return total;
    }

    public String getStats(){
        StringBuilder sb = new StringBuilder();
        NumberFormat nf = NumberFormat.getInstance();
        for (Map.Entry<PrType,Map<Exercise,Double>> entry: sessionPrs.entrySet()){

            PrType t = entry.getKey();
            Map<Exercise,Double> prs = entry.getValue();
            String title = t+" PR'S BROKEN THIS SESSION";
            sb.append(prsBrokenString(title,prs));

        }
        sb.append("Total Sets: ").append(totalSets()).append("\n");
        sb.append("Total Volume: ").append(nf.format(calculateTotalVolume())).append("\n");
        return sb.toString();
    }

    public void recordPr(PrType type, Exercise e, double pr){
        sessionPrs.computeIfAbsent(type, ignored->new HashMap<>()).put(e,pr); 
        //here computeIfAbsent needs 2 paramaters: a key (PrType) and a function that
        //satisfies the FunctionInterface. The SAM (Single Abstract Method) interface function
        //has a single asbtract method: R apply (T t). That is why we need ignored. because
        //we need to satisfy the contract
    }

    
    
    private  String prsBrokenString(String title, Map<Exercise,Double> prMap){
        if (prMap.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");
        prMap.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().getName()))
            .forEach(e ->
            sb.append(e.getKey().getName())
              .append(": ")
              .append(String.format("%.2f", e.getValue()))
              .append("\n")
        );

        return sb.toString();

    }

    public List<PerformedExercise> getExerciseList(){
        return this.sessionExercises;
    }
 
}
