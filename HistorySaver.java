import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HistorySaver {

    private final Path path;
    private static final  String SESSION_START = "WORKOUT SESSION";
    private static final  String SESSION_END = "END_SESSION";
    private static final  String EX_START = "EXERCISE";
    private static final String EX_END = "END_EX";
      
    HistorySaver(Path path){
        this.path=path;
    }

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
            lines.add("MUSCLES:"+musclesToSaveFormat(e.getMuscles()));
        }
            
    
        for (WorkoutSet s: pe.getSets()){
            lines.add("SET:"+s.getWeight()+","+s.getReps());
        }

        lines.add(EX_END);
        lines.add("");

        return lines;


    }

    private static String musclesToSaveFormat(Set<Muscles> m){
        //stream means go through each of the elements of the set
        //map just returns a function value for each element
        //reduce "reduces" it all to a string
        return m.stream().map(Enum::name).reduce((a,b)->a+";"+b).orElse("");
    }

}

    
