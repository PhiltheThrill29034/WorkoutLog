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

        List<WorkoutSession> sList = new ArrayList<>();
        int idx=0;

        while (idx<lines.size()){
            String line = lines.get(idx).trim();
            if (line.isEmpty()) {idx++; continue;}
            if (line.equals("WORKOUT SESSION")){
                try {
                    ParseResult<WorkoutSession> result = parseSession(lines, idx,exerciseById);
                    sList.add(result.value);
                    idx = result.nextIdx;
                    continue;
                } catch (IllegalStateException e){
                    warnings.add("Skipping invalid session at line "+idx);
                    warnings.add("👉 REASON: " + e.getMessage());
                    
                    if (e.getCause() != null) {
                        warnings.add("🔍 ROOT CAUSE: " + e.getCause().getMessage());
                    }
                    idx++;
                    while (idx < lines.size() && !lines.get(idx).trim().equals("WORKOUT SESSION")){
                        idx++;
                    }
                    //while loop restores the state of the parser. loops through garbage state and
                    //stops when it meets a new WorkoutSession
                    continue; // so we don't fall in outer loop
                }
            }

            idx++;
        }

        return sList;

    }


    private static ParseResult<WorkoutSession> parseSession (List<String> lines,int start,Map<UUID,Exercise> exerciseById){
        List<PerformedExercise> eList = new ArrayList<>();
        LocalDateTime stamp = null;
        String routineName="";
        int idx = start;
        while (idx < lines.size()){
            String line = lines.get(idx).trim();
            if (line.isEmpty()) {idx++; continue;}
            if (line.startsWith("DATE")){
                String raw = valueAfterColon(line);
                try {
                    stamp = LocalDateTime.parse(raw);
                }
                catch (Exception e){
                    throw new IllegalStateException("Bad date at line "+idx);
                }
            }

            else if (line.startsWith("ROUTINE")){
                routineName = valueAfterColon(line);
            } 

            else if (line.startsWith("EXERCISE")){
                ParseResult<PerformedExercise> result = parseOneExercise(lines, idx+1,exerciseById);
                eList.add(result.value);
                idx = result.nextIdx;
                continue; //if we didnt use continue, start ++ would happen at the bottom and we would skip a line

            }

            else if (line.startsWith("END_SESSION")){
                break;
            }
            idx ++;
        }

        if (stamp==null){
            throw new IllegalStateException("Workout Session at line "+ start + "missing DATE value");
        }

        if (routineName==null || routineName.isBlank()){
            throw new IllegalStateException("Workout Session at line "+ start + "missing ROUTINE value");
        }


        
        WorkoutSession s = new WorkoutSession(routineName,stamp,eList);
        return new ParseResult<>(s, idx+1);
    }

    private static ParseResult<PerformedExercise> parseOneExercise(List<String> lines,int start,Map<UUID,Exercise> exerciseById){
        String kind = null;
        String name = null;
        String desc = null;
        UUID id = null;
        boolean deleted = false;
        PerformedExercise pe = null;
        Set<Muscles> muscles = null;
        int idx = start;
        List<WorkoutSet> sets = new ArrayList<>();
        while (idx<lines.size()){
            String line = lines.get(idx).trim();
            if (line.isEmpty()) {idx++; continue;}
            else if (line.startsWith("ID")) {id = UUID.fromString(valueAfterColon(line));}
            else if (line.startsWith("KIND"))  {kind = valueAfterColon(line);}
            else if (line.startsWith("NAME")) {name = valueAfterColon(line);}
            else if (line.startsWith("DESC")){desc = valueAfterColon(line);}
            else if (line.startsWith("MUSCLES")) {
                try{
                    muscles = parseMuscles(valueAfterColon(line));
                }
                catch (IllegalStateException e){
                    throw new IllegalStateException
                    ("Illegal muscle values in custom exercise at line "+start,e);
                    //this is excpetion chaining
                    //the exception is thrown and we pass as a parameter the cause (e), the
                    //exception that parseMuscles threw
                }
            }
            else if (line.startsWith("SET")){
                String values = valueAfterColon(line);
                String[] parts = values.split(",");
                double weight = Double.parseDouble(parts[0]);
                int reps = Integer.parseInt(parts[1]);
                sets.add(new WorkoutSet(weight,reps));
            }
            else if (line.startsWith("END_EX")){
                break;
            }

            idx++;
        }

        if (kind == null || kind.isBlank()) {
            throw new IllegalStateException("Exercise at line " + start + " missing KIND value.");
        }

        if (name==null||name.isBlank()){
            throw new IllegalStateException("Exercise at line " + start + " missing NAME value.");
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
                    throw new IllegalStateException("Exercise at line " + start + " has an unknown type value: ["+name+"]");
                }
            } else if ("CUSTOM".equals(kind)) {
                deleted = true;
                if (muscles==null||muscles.isEmpty())
                    throw new IllegalStateException("Muscles values for custom exercise at line "+ start + "cannot be empty");
                

                base = ExerciseFactory.loadCustom(id,name,desc,muscles);

            }

        }

        pe = new PerformedExercise(base);
        if (deleted) pe.markDeleted();

        for (WorkoutSet s : sets){
            pe.addSet(s);
        }
        
        return new ParseResult<>(pe, idx+1);
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

    private static String valueAfterColon(String line){
        int idx = line.indexOf(":");
        return (idx>=0) ? line.substring(idx+1) : "";
    }


    private static class ParseResult<T>{
        T value;
        int nextIdx;
        ParseResult(T value, int nextIdx){
            this.value=value;
            this.nextIdx=nextIdx;
        }
    
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




