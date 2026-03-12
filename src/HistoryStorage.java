import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map;


import java.time.LocalDateTime;


public class HistoryStorage {

    private static final  String SESSION_START = "WORKOUT SESSION";
    private static final  String SESSION_END = "END_SESSION";
    private static final  String EX_START = "EXERCISE";
    private static final String EX_END = "END_EX";

    private final Path path;

    HistoryStorage(Path path){
        this.path=path;
    }

    public LoadResult loadAllSessions(Map<UUID,Exercise> exerciseById) throws IOException{
        
            if (!Files.exists(path)){
                return new LoadResult(new ArrayList<>(), new ArrayList<>());
            }
            List<String> lines = Files.readAllLines(path);
            List<String> warnings = new ArrayList<>();
            List<WorkoutSession> sList = parseAllSessions(lines, warnings,exerciseById);
            return new LoadResult(sList, warnings); 
    }

    

    private static List<WorkoutSession> parseAllSessions(List<String> lines,List<String> warnings,Map<UUID,Exercise> exerciseById){

        List<List<String>> blocks = ParsingUtils.extractBlocks(lines, SESSION_START, SESSION_END);
        List<WorkoutSession> sessionList = new ArrayList<>();
        int count=0;
        for (List<String> block: blocks){
                count++;
                try {
                    WorkoutSession result = parseSession(block, exerciseById);
                    sessionList.add(result);
                } catch (IllegalStateException e){
                    warnings.add("Skipping invalid session at block "+count);
                    warnings.add("👉 REASON: " + e.getMessage());
                    
                    if (e.getCause() != null) {
                        warnings.add("🔍 ROOT CAUSE: " + e.getCause().getMessage());
                    }
                    
                }
        }

        return sessionList;
    }


    private static WorkoutSession parseSession (List<String> sessionLines,Map<UUID,Exercise> exerciseById){
        
        LocalDateTime stamp = null;
        String routineName="";
        int idx=0;
        for (String line : sessionLines){
            idx++;
            line = line.trim();
            if (line.startsWith("DATE")){
                String raw = FormatUtils.valueAfterColon(line);
                try {
                    stamp = LocalDateTime.parse(raw);
                }
                catch (Exception e){
                    throw new IllegalStateException("Bad date at line "+idx);
                }
            }

            else if (line.startsWith("ROUTINE")){
                routineName = FormatUtils.valueAfterColon(line);
            } 

        }

        if (stamp==null){
            throw new IllegalStateException("Workout Session  missing DATE value");
        }

        if (routineName==null || routineName.isBlank()){
            throw new IllegalStateException("Workout Session missing ROUTINE value");
        }
        
        List<PerformedExercise> eList = new ArrayList<>();
        List<List<String>> sessionExercises = ParsingUtils.extractBlocks(sessionLines, EX_START, EX_END);
        for (List<String> exercise: sessionExercises){
            parseOneExercise(exercise,exerciseById);
        }

        return new WorkoutSession(routineName,stamp,eList);
        
    }

    private static PerformedExercise parseOneExercise(List<String> lines ,Map<UUID,Exercise> exerciseById){
        String kind = null;
        String name = null;
        String desc = null;
        UUID id = null;
        boolean deleted = false;
        PerformedExercise pe = null;
        Set<Muscles> muscles = null;
        int idx = 0;
        List<WorkoutSet> sets = new ArrayList<>();
        for (String line: lines){
            idx++;
            line=line.trim();
            
            if (line.startsWith("ID")) {id = UUID.fromString(FormatUtils.valueAfterColon(line));}
            else if (line.startsWith("KIND"))  {kind = FormatUtils.valueAfterColon(line);}
            else if (line.startsWith("NAME")) {name = FormatUtils.valueAfterColon(line);}
            else if (line.startsWith("DESC")){desc = FormatUtils.valueAfterColon(line);}
            else if (line.startsWith("MUSCLES")) {
                try{
                    muscles = parseMuscles(FormatUtils.valueAfterColon(line));
                }
                catch (IllegalStateException e){
                    throw new IllegalStateException
                    ("Illegal muscle values in custom exercise at line "+idx,e);
                    //this is excpetion chaining
                    //the exception is thrown and we pass as a parameter the cause (e), the
                    //exception that parseMuscles threw
                }
            }
            else if (line.startsWith("SET")){
                String values = FormatUtils.valueAfterColon(line);
                String[] parts = values.split(",");
                double weight = Double.parseDouble(parts[0]);
                int reps = Integer.parseInt(parts[1]);
                sets.add(new WorkoutSet(weight,reps));
            }
            else if (line.startsWith(EX_END)){
                break;
            }

            idx++;
        }

        if (kind == null || kind.isBlank()) {
            throw new IllegalStateException("Exercise missing KIND value.");
        }

        if (name==null||name.isBlank()){
            throw new IllegalStateException("Exercise missing NAME value.");
        }

        Exercise base = null;

        if ("CUSTOM".equals(kind)&&id==null){ //throw an error if ID is empty for a custom
            throw new IllegalArgumentException("ID for custom exercise cannot be empty.");
        }

        if (id!=null){
            base = exerciseById.get(id);
        }
        

        if (base == null){
            if ("TYPE".equals(kind)){ //here we don't mind if ID is null, because the id is
            //uniquely identified by the name anyways
                try{
                    ExerciseType type = ExerciseType.valueOf(name);
                    base = ExerciseFactory.fromType(type);
                    //try catch block in case the enum doesn't exist
                }
                catch (Exception e){
                    throw new IllegalStateException("Exercise has an unknown type value: ["+name+"]");
                }
            } else if ("CUSTOM".equals(kind)) {
                deleted = true;
                if (muscles==null||muscles.isEmpty())
                    throw new IllegalStateException("Muscles values for custom exercise cannot be empty");
                

                base = ExerciseFactory.loadCustom(id,name,desc,muscles);

            }

        }

        pe = new PerformedExercise(base);
        if (deleted) pe.markDeleted();

        for (WorkoutSet s : sets){
            pe.addSet(s);
        }
        
        return pe;
    }

    private static Set<Muscles> parseMuscles(String line){
        EnumSet<Muscles> m = EnumSet.noneOf(Muscles.class);
        if (line == null || line.isBlank()) return m;
        String[] mParts = line.split(";");

        for (String part : mParts){
            String token = part.trim();
            if (token.isEmpty()) continue;
            try {
                m.add(Muscles.valueOf(token));
            } catch (Exception e){
                throw new IllegalStateException("Invalid muscle value : ["+token+"]");
            }
        }
        return m;
    }

    


    



    public record LoadResult(
        List<WorkoutSession> sessions,
        List<String> warnings
    ) {} //a record can be thought of as a mini class that holds objects
    //java creates getters : sessions() and warnings()
    //constructor too

    void appendSession(WorkoutSession s) throws IOException{
        List<String> lines = sessionToLines(s);
        
        write(lines,StandardOpenOption.CREATE,StandardOpenOption.APPEND);
        
    }


    void write(List<String> lines,StandardOpenOption... opts) throws IOException{ //varargs!!
        if (path.getParent()!=null) Files.createDirectories(path.getParent());
        Files.write(path,lines,opts);
    }

    public void saveAllSessions(List<WorkoutSession> sList) throws IOException{
        List<String> allLines = new ArrayList<>();
        
        for (WorkoutSession s : sList){
            allLines.addAll(sessionToLines(s));
        }
        write(allLines,StandardOpenOption.CREATE);
    }

    private static List<String> sessionToLines(WorkoutSession s){
        List<String> lines = new ArrayList<>();
        lines.add(SESSION_START);
        lines.add("DATE:"+s.getDate());
        lines.add("ROUTINE:"+s.getName());
        for (PerformedExercise e: s.getExerciseList()){
            lines.addAll(exerciseToLines(e));
        }

        lines.add(SESSION_END);
        lines.add("");
        return lines;
    }

    

    private static List<String> exerciseToLines(PerformedExercise pe){
        Exercise e = pe.getExercise();
        List<String> lines = new ArrayList<>();
        lines.add(EX_START);
        lines.add("ID:"+pe.getId().toString());
        if (e.getType()!=null){
            lines.add("KIND:TYPE");
            lines.add("NAME:"+e.getType().name());
        } else {
            lines.add("KIND:CUSTOM");
            lines.add("NAME:"+e.getName());
            if (e.getDesc() != null && !e.getDesc().isBlank()) 
                lines.add("DESC:" + e.getDesc());
            lines.add("MUSCLES:"+FormatUtils.musclesToSaveFormat(e.getMuscles()));
        }
            
    
        for (WorkoutSet s: pe.getSets()){
            lines.add("SET:"+s.getWeight()+","+s.getReps());
        }

        lines.add(EX_END);
        lines.add("");

        return lines;


    }


}




