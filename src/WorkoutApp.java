import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.UUID;




public class WorkoutApp {
    private Scanner in;
    Map<String,WorkoutRoutine> routines=new LinkedHashMap<>();
    List<Exercise> exMenu;
    List<WorkoutSession> history; 
    Map<Exercise,Double> weightPr;
    Map<Exercise,Double> volumePr;
    Map<UUID,Exercise> exercisebyId = new HashMap<>();
    private final List<String> menuLabels = List.of(
        "Exit",
        "Start Routine",
        "Build Routine",
        "Edit Routine",
        "List Routines",
        "Create Custom Exercise",
        "View/Edit History"
        
    );

    private HistorySaver saver;
    private HistoryLoader loader;

    //runnable is something you can run. has method run(). literally that is it
    private final List<Runnable> options = List.of(
        ()->{}, //this means do nothing
        // "::" is called a method reference: point to this method, don't call it
        this::startRoutine,
        this::buildRoutine,
        this::editRoutine,
        this::listRoutines,
        this::createCustom,
        this::viewHistory 
    );

    public WorkoutApp(Scanner in){
        this.in=in;
        exMenu=new ArrayList<>();
        history = new ArrayList<>();
        weightPr=new LinkedHashMap<>();
        volumePr=new LinkedHashMap<>();
        Path p = Path.of("saved_data/session_history.txt");
        saver = new HistorySaver(p);
        loader = new HistoryLoader(p);
        exMenu.clear();
        exercisebyId.clear();
        for (ExerciseType exT:ExerciseType.values()){
            Exercise ex=Exercise.fromType(exT);
            exercisebyId.put(ex.getId(),ex);
            exMenu.add(ex);

        }
    }

    private  void run() {
        
        System.out.println("INITIALIZING GAIN ENGINE...");
        List<String> warnings;
        try{
            HistoryLoader.LoadResult lr = loader.loadAllSessions(exercisebyId);
            history = lr.sessions();
            warnings = lr.warnings();
            
        }
        catch (IOException e){
            System.out.println("Couldn't load history!! FATASS " + e.getMessage());
            warnings = new ArrayList<>();
        }

        if (!warnings.isEmpty()){
            for (String w : warnings){
                System.err.println(w); //printing warnings to err (not normal output)
            }
        }

        if (!history.isEmpty()){
            System.out.println("Gains loaded successfully");
            System.out.println("Loading past PR's ...");
            prRecomputer();
            System.out.println("Done!");
        } else {
            System.out.println("Shit, first time working out?! Get to work fatty");
        }
        
        boolean running=true;
        while (running){
            printMenu();
            int choice=InputUtils.readIntAndRetry(this.in,"What would you like to do?\nEnter here: ","Enter a valid choice from the menu: ",0,menuLabels.size()-1);
            if (choice==0){
                running=false;
                
            } else {
                options.get(choice).run();
            }
        }

    }

    private void printMenu(){
        
        for (int i=0;i<menuLabels.size();i++){
            System.out.printf("%d. %s%n",i,menuLabels.get(i));
        }

        System.out.printf("0. %s%n",menuLabels.get(0));

    }

    

    public static void main(String[] args) {
        WorkoutApp wApp=new WorkoutApp(new Scanner(System.in));
        wApp.run();
    }


    //builds a routine for the user. print the exercise menu, and adds exercices to the routine. if the exercise
    // is a pre-chosen type, then user is not allowed to add twice:
    private void buildRoutine(){
        
        
        String name="";
        String key;
        while (true){
            System.out.print("Pick a name for your routine: \n->");
            name = in.nextLine();
            if (name.isBlank()) {
                System.out.println("Name cannot be empty.");
                continue;
            }
            key = name.toLowerCase();
            if (routines.containsKey(key)){
                System.out.println("You already have a routine with that name. Pick another one.");
                continue;
            }

            break;
        }

        WorkoutRoutine routine = new WorkoutRoutine(name);
        while (true){
        
            printExerciseList(routine);

            int choice=
            InputUtils.readIntAndRetry
            (this.in,"Pick an exercise to toggle, or exit (0) : "
            ,"Enter a valid exercise from the menu: "
            ,0,exMenu.size());

            if (choice==0) break;
            Exercise template=exMenu.get(choice-1);

            boolean dup = routine.contains(template);
            if (!dup){
                Exercise ex = 
                Exercise.freshCopyOf(template);
                
                System.out.println(ex.details());
                System.out.print("Add exercise? (type n/no to cancel): ");
                String answer=in.nextLine().trim().toLowerCase();
                //if (!answer.equals("n")&&!answer.equals("no")){
                    //add=true;
                //} could do this, but the following is better. 

                boolean add = (!answer.equals("n"))&&(!answer.equals("no"));


                
                if (add){
                    routine.addExercise(ex);
                    System.out.printf("Added %s%n",ex.getName());
                } else {
                    System.out.println("Cancelled");
                }
            }   else {
                routine.removeExercise(template);
            }
            

        }

        routines.put(name.toLowerCase(),routine);
        System.out.printf("Built and added [%s] to your routines%n",name);
    }

    private void startRoutine(){
        WorkoutRoutine r=pickRoutine("Which routine you hittin' today?");
        if (r==null) return;
        WorkoutSession sesh=new WorkoutSession(r,LocalDateTime.now());
        
        sessionModifier(sesh);

        history.add(sesh);
        saveHistorySafely();
        
        
        

    }

    private void sessionModifier(WorkoutSession sesh){

         while (true){
            sesh.printSessionExercises();
            int choice = InputUtils.readIntAndRetry(this.in,"Choose an exercise to start/edit, or 0 to exit:\n->","Choose a valid exercise\n->",0,sesh.getSessionSize());
            if (choice==0) break;
            logSetsForSessionExercise(sesh,choice);
        }

        System.out.println(sesh);
        System.out.println(sesh.getStats());
    }

    private void printExerciseList(WorkoutRoutine r){
        int count=1;    
        for (Exercise ex:exMenu){
            System.out.printf("%d. %s %s%n",count,ex.getName(),(r.contains(ex)) ? "[SELECTED]" : "");
            count++;
        }
    }

    

    private void editRoutine(){
        boolean editing = true;
        WorkoutRoutine picked = pickRoutine("Pick a routine to edit from the menu");
        if (picked==null) return; 
        
        while (editing){
            if (routines.isEmpty()) {
                System.out.println("No routines to edit");
                editing=false;
            }
            printEditMenu();
            int choice = InputUtils.readIntAndRetry(this.in,"Pick a choice from the menu : ", "Enter a valid choice: ", 0, 4);
            switch (choice){
                case 0 -> editing=false;
                case 1 -> addExerciseToRoutine(picked);
                case 2 -> removeExerciseFromRoutine(picked);
                case 3 -> renameRoutine(picked);
                case 4 -> deleteRoutine(picked);
            }
        }
    }

    private WorkoutRoutine pickRoutine(String title){
        if (routines.isEmpty()){
            System.out.println("Routines: ZERO = 0 !!!. Body fat : 100% !! GOD DAMN!");
            return null;
        }
        List<WorkoutRoutine> routineList=new ArrayList<>(routines.values());
        int count = 1;
        System.out.println(title);
        for (WorkoutRoutine routine: routineList){
            System.out.printf("%d. %s%n",count,routine.getName());
            count++;
        }
        int choice=InputUtils.readIntAndRetry(this.in,"Enter here: ","Pick a valid routine number: ",0,routines.size());
        if (choice==0) return null;
        return routineList.get(choice-1);
    }

    private void printEditMenu(){
        System.out.println("===EDIT ROUTINE===");
        System.out.println("1. Add exercise");
        System.out.println("2. Remove exercise");
        System.out.println("3. Rename Routine");
        System.out.println("4. Delete routine");
        System.out.println("0. Exit");
        System.out.println("==================");
    }

    private void addExerciseToRoutine(WorkoutRoutine picked){
        System.out.println("Which exercises would you like to add?");
        printExerciseList(picked);
        int choice=InputUtils.readIntAndRetry(this.in,"Choose one of the following exercises to add, or 0 to finish adding: ", "Pick a valid exercise number: ",0,exMenu.size());
        while (choice!=0){
            Exercise template=exMenu.get(choice-1);
            Exercise ex = Exercise.freshCopyOf(template);
            if (!picked.addExercise(ex)){
                System.out.printf("Cannot add %s twice.%n",ex.getName());
            }else {
                System.out.printf("Added %s%n",ex.getName());
            }
            choice=InputUtils.readIntAndRetry(this.in,"Pick another exercise, or exit: ","Enter a valid exercise from the menu: ",0,exMenu.size());
        }
        System.out.println("New routine: ");
        System.out.println(picked.toString());
    }  

    private void removeExerciseFromRoutine(WorkoutRoutine picked){ //removes an exercise from picked routine.
        //if the routine becomes empty, the method stops running.
        if (picked.getRoutineSize()==0){
            System.out.println("No exercises for this routine.");
            return;
        }
        System.out.println("Which exercises would you like to remove?");
        System.out.println(picked.getExercises());
        int choice=InputUtils.readIntAndRetry(this.in,"Choose one of the following exercises to remove, or 0 to finish adding: ", "Pick a valid exercise number: ",0,picked.getRoutineSize());
        while (true){
            
            if (choice==0) break;
            picked.removeExercise(choice-1);
            if (picked.getRoutineSize()==0){
                System.out.println("No more exercises to remove");
                break;
            }
            System.out.println(picked.getExercises());
            choice=InputUtils.readIntAndRetry(this.in,"Remove another exercise, or 0 to finish removing: ", "Pick a valid exercise number: ",0,picked.getRoutineSize());
        }
        System.out.println("New routine: ");
        System.out.println(picked.toString());
    }

    private void renameRoutine(WorkoutRoutine picked){ //chesk this out next time, about renaming
        
        System.out.print("Pick another name for your routine\n-> ");
        String oldName=picked.getName();
        String oldKey=oldName.toLowerCase();
        String newName=in.nextLine().trim();
        String newKey=newName.toLowerCase();
        if (routines.containsKey(newKey)&&!(oldKey.equals(newKey))){
            System.out.println("A routine with that name already exists.");
            return;
        }

        routines.remove(oldName.toLowerCase());
        picked.setName(newName);
        routines.put(newKey,picked);

        System.out.printf("Renamed routine %s to %s%n",oldName,newName);
    }

    private void deleteRoutine(WorkoutRoutine picked){
        System.out.printf("Are you sure you want to delete [%s] from your routines (yes/no)?\n->",picked.getName());
        String ans=in.nextLine().trim().toLowerCase();
        while (!ans.equals("yes")&&!ans.equals("no")){
            System.out.print("Give a valid answer.\n->");
            ans=in.nextLine();
        }
        if (ans.equalsIgnoreCase("no")){
            System.out.println("Cancelled.");
            return;
        }
        String key=picked.getName().toLowerCase();
        routines.remove(key);
        System.out.printf("Deleted %s from your routines%n",picked.getName());

    }

    //lists the routines built by the user
    //if none are built yet, an appropriate message is printed
    private void listRoutines(){
        
        if (routines.isEmpty()){
            System.out.println("No routines built yet.");
            return;
        }
        System.out.println("=====YOUR ROUTINES=====");
        List<WorkoutRoutine> routineList=new ArrayList<>(routines.values());
        int count=1;
        for (WorkoutRoutine r: routineList){
            System.out.printf("%d. %s%n",count,r.getName());
            count++;
        }
        System.out.println("=======================");
        int choice= InputUtils.readIntAndRetry(this.in,"Pick a routine to expand or 0 to exit\n->","Enter a valid routine number\n->",0,routines.size());

        while (choice!=0){

            WorkoutRoutine expandR=routineList.get(choice-1);
            System.out.print(expandR.getExercises());
            
            choice= InputUtils.readIntAndRetry(this.in,"Pick a routine to expand or 0 to exit\n->","Enter a valid routine number\n->",0,routines.size());
        }

    }


    //logs sets for an exercise till user chooses to exit. 
    private void logSetsForSessionExercise(WorkoutSession session, int exIdx){ //objects are passed by reference, no need to return the session.
        int userChoice=exIdx-1; //-1 to the index to map it to the correct array index
        PerformedExercise pe = session.getSessionExercise(userChoice);
        
        
        while (true){
            System.out.println("1. Add a set.");
            System.out.println("2. Edit existing set.");
            System.out.println("0. Back");
            int action = InputUtils.readIntAndRetry(in,"->","Pick a valid choice:\n->",0,2);
            if (action==0) break;
            if (action==1){
                while (true){
                    int nextSetNum=pe.getTotalSets()+1;
                    System.out.printf("Set %d: %s%n",nextSetNum,pe.getName());
                    double weight = InputUtils.readDoubleAndRetry(this.in, "Enter weight here \n->", "Enter a valid weight\n->", 0, 1000);
                    
                    int reps = InputUtils.readIntAndRetry(this.in, "Enter the number of reps: \n->", "Enter a valid number:\n->", 0, 100);

                    session.addSet(userChoice, weight, reps); 
                    
                    if (checkPr(pe.getExercise(),weight,weightPr)){
                        System.out.printf("🔥 NEW WEIGHT PR on %s: %.2f. GOOD SHIT!!%n",pe.getName(),weight);
                        session.recordPr(PrType.WEIGHT,pe.getExercise(),weight);
                    }

                    double volume = (weight==0) ? reps  : weight*reps;
                    if (checkPr(pe.getExercise(),volume,volumePr)){
                        System.out.printf("NEW VOLUME PR on %s: %.2f. NICE KID!!%n",pe.getName(),volume);
                        session.recordPr(PrType.VOLUME,pe.getExercise(),weight*reps);
                    }
                    System.out.print("Press enter to add another set, or any button to exit\n->");
                    String exit = in.nextLine().trim();
                    if (!exit.isEmpty()) break;
                }
            } else if (action==2){
                editSet(session,pe);
            }

        }

        if (pe.getTotalSets()!=0) System.out.println(pe);
        
    }

    private void createCustom(){
        System.out.println("===== CREATE CUSTOM EXERCISE =====");
        System.out.print("1. Name : ");
        String name = in.nextLine().trim();
        while (name.isEmpty()){
            System.out.print("Please give a name to the exercise: ");
            name = in.nextLine().trim();
        }
        System.out.println("2. Choose Muscles Trained: "); 
        EnumSet<Muscles> selected= pickMuscles();
        System.out.print("3. Give a desciption (optional): ");
        String desc=in.nextLine().trim();
        if (desc.isEmpty()) desc= "No description";
        Exercise e = Exercise.createCustom(name, desc, selected);
        exMenu.add(e);
        exercisebyId.put(e.getId(),e);
    }

    private EnumSet<Muscles> pickMuscles(){
        EnumSet<Muscles> selected = EnumSet.noneOf(Muscles.class);
        Muscles[] all = Muscles.values();
        
        while (true){
            for (int i=0;i<all.length;i++){
                Muscles m= all[i];
                System.out.printf("\t%c. %s %s%n",(char) ('a'+i),m.getName(), //a is a number. we just cast it. 
                (selected.contains(m)) ? "(selected)" : "");
            }

            System.out.printf("Pick a muscle group (%c-%c): %n->",'a',(char) ('a'+all.length-1));
            String input=in.nextLine().trim();
            

            if (input.equals("0")){
                if (selected.isEmpty()){
                    System.out.println("Pick at least one muscle");
                    continue;
                } else {
                    break;
                }
            }

            
            try {
                char c = InputUtils.checkChar(input, 'a', (char) ('a'+all.length-1));
                int index = c -'a'; //c is a number. if c=a, then index = 'a'-'a'=0. If it is b then 1, and so on
                Muscles picked = all[index];
                if (selected.contains(picked)){
                    selected.remove(picked);
                } else {
                    selected.add(picked);
            }
            } catch (IllegalArgumentException e){
                System.out.println(e.getMessage());
                
            }   
        }

        return selected;
    }


    private void viewHistory(){  //prints a history of completed sessions
        if (history.isEmpty()) {
            System.out.println("No workouts completed. Fatass");
            return;
        }
        
        int i=1;

        for (WorkoutSession s : history){
            System.out.printf("%d. Workout on %s : %s%n",i++,s.getFormattedDate(),s.getName());
        }
        
        while (true){
            int choice = InputUtils.readIntAndRetry(in, "Press (0) to go back to the main menu, or pick a session to edit: ",
            "Please pick a valid choice: ", 
            0, history.size());

            if (choice==0) break;
            
            WorkoutSession chosen = history.get(choice-1);
            
            System.out.println("Choose an option: ");
            System.out.println("1. View Details");
            System.out.println("2. Edit Session");
            System.out.println("3. Delete Session");
            choice = InputUtils.readIntAndRetry(in,"-> ","Pick a valid choice from the menu:\n->",0,3);
            switch (choice) {
                case 1 -> System.out.println(chosen);
                case 2 -> {
                    sessionModifier(chosen);
                    System.out.println("Saved changes: Check!");
                    saveHistorySafely();
                }
                case 3 -> {
                    history.remove(chosen);
                    System.out.println("Deleted");
                    saveHistorySafely();
                }
            }
            
            
        }     
    }

    private void editSet(WorkoutSession session, PerformedExercise pe){
        
        
        while (true){
            if (pe.getTotalSets()==0) {
                System.out.println("No sets to edit.");
                return;
            }
            System.out.println(pe);
            int picked = InputUtils.readIntAndRetry(in,"Pick a set to edit or 0 to go back: ","Pick a valid set: ",0,pe.getTotalSets());
            if (picked==0) return;
            int setIdx=picked-1;
            System.out.println("1. Edit set");
            System.out.println("2. Delete set");
            System.out.println("0. Back");
            int choice = InputUtils.readIntAndRetry(in,"->","Pick a valid edit choice bro\n->",0,2);
            switch (choice){

                case 0 -> {return;}
                case 1 -> {
                    double newWeight = InputUtils.readDoubleAndRetry(in,"Enter weight ->","Pick valid weight fatass ->",0,1000);
                    if (checkPr(pe.getExercise(),newWeight,weightPr)){
                        System.out.printf("🔥 NEW WEIGHT PR on %s: %.2f. GOOD SHIT!!%n",pe.getName(),newWeight);
                        session.recordPr(PrType.WEIGHT,pe.getExercise(),newWeight);
                    }


                    int newReps = InputUtils.readIntAndRetry(in,"Enter reps ->","Pick valid reps FATTY ->",0,100);
                    double volume = (newWeight==0) ? newReps  : newWeight*newReps;
                    if (checkPr(pe.getExercise(),volume,volumePr)){
                        System.out.printf("NEW VOLUME PR on %s: %.2f. NICE KID!!%n",pe.getName(),volume);
                        session.recordPr(PrType.VOLUME,pe.getExercise(),newWeight*newReps);
                    }
                    pe.updateSet(setIdx,newWeight,newReps);
                    System.out.println("Updated");
                }

                case 2 -> {
                    pe.removeSet(setIdx);
                    System.out.printf("Removed Set %d%n",picked);
                }
            }
        }
    }

    private boolean checkPr(Exercise e,double val, Map<Exercise,Double> prMap){
        
        double oldPr = prMap.getOrDefault(e,0.0);
        if (val>oldPr){
            prMap.put(e,val);
            return true;
        }

        return false;

    }

    private void saveHistorySafely(){
        try {
            saver.saveAllSessions(history);
            System.out.println("Changes saved succesfully!!");
        } catch (IOException e){
            System.out.println("Could not save history. Try again later");
        }
    }

    private void prRecomputer(){

        weightPr.clear();
        volumePr.clear(); //clearing maps before recomputing

        for (WorkoutSession sesh : history){
            for (PerformedExercise pe : sesh.getExerciseList()){

                double maxWeight = pe.getSets().stream()
                .mapToDouble(s->s.getWeight())
                .max().orElse(0);

                double maxVolume = pe.getSets().stream()
                .mapToDouble(s->s.getWeight()*s.getReps())
                .max().orElse(0);

                
                //mapToDouble returns a DoubleStream
                //map returns a Stream<Double>
                //DoubleStream works with primitive double values (more methods , average, sum, max)
                checkPr(pe.getExercise(), maxWeight, weightPr);
                checkPr(pe.getExercise(), maxVolume, volumePr);
            }
        }
    }
}
