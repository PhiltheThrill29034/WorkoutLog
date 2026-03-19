import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.io.IOException;
import java.nio.file.Files;

public class CustomStorage {
    
    
    private static final  String EX_START = "EXERCISE";
    private static final String EX_END = "END_EX";

    private final Path path;

    CustomStorage (Path p){
        this.path=p;
    }

    void write(List<String> lines, StandardOpenOption... opts) throws IOException{
        if (path.getParent()!=null) Files.createDirectories(path.getParent());
        Files.write(path,lines,opts);
    }

    public void saveAllCustoms(List<Exercise> customs) throws IOException{
        List<String> allLines = new ArrayList<>();
        for (Exercise e: customs){
            allLines.addAll(customExerciseToLines(e));
        }
        write(allLines,StandardOpenOption.CREATE);
    }

    

    private static List<String> customExerciseToLines(Exercise e){
    
        List<String> lines = new ArrayList<>();
        lines.add(EX_START);
        lines.add("ID:"+e.getId().toString());
        
        
        lines.add("KIND:CUSTOM");
        lines.add("NAME:"+e.getName());
        if (e.getDesc() != null && !e.getDesc().isBlank()) 
            lines.add("DESC:" + e.getDesc());
        lines.add("MUSCLES:"+ FormatUtils.musclesToSaveFormat(e.getMuscles()));
        
        lines.add(EX_END);
        lines.add("");

        return lines;
    }

    public List<Exercise> loadCustoms() throws IOException{
        
        List<Exercise> customs = new ArrayList<>();
        if (!Files.exists(path)){
            return customs;
        }
        List<String> lines = Files.readAllLines(path);
        List<List<String>> blocks = ParsingUtils.extractBlocks(lines,EX_START,EX_END);
        int count=0;
        for (List<String> block: blocks){
            count++;
            try{
                Exercise e = parseOneExercise(block);
                customs.add(e);
            }
            catch (IllegalStateException e){
                System.err.println("Error parsing block "+count);
                System.err.println("Details: "+e.getMessage());
            }
        }

        return customs;
    }

    private static Exercise parseOneExercise(List<String> lines){
        String kind = null;
        String name = null;
        String desc = null;
        UUID id = null;
        Set<Muscles> muscles = null;
        
        for (String line: lines){
            
            
            if (line.startsWith("ID")) {id = UUID.fromString(FormatUtils.valueAfterColon(line));}
            else if (line.startsWith("KIND"))  {kind = FormatUtils.valueAfterColon(line);}
            else if (line.startsWith("NAME")) {name = FormatUtils.valueAfterColon(line);}
            else if (line.startsWith("DESC")){desc = FormatUtils.valueAfterColon(line);}
            else if (line.startsWith("MUSCLES")) {
                try{
                    muscles = ParsingUtils.parseMuscles(FormatUtils.valueAfterColon(line));
                }
                catch (IllegalStateException e){
                    throw new IllegalStateException
                    ("Illegal muscle values in custom exercise.");
                    //this is excpetion chaining
                    //the exception is thrown and we pass as a parameter the cause (e), the
                    //exception that parseMuscles threw
                }
            }
            
            else if (line.startsWith(EX_END)){
                break;
            }

            
        }

        ParsingUtils.validateStringInput(kind, "KIND", 20);

        ParsingUtils.validateStringInput(name, "NAME", 100);


        if (id==null){
            throw new IllegalStateException("Exercise missing ID value.");
        }

        

        if ("CUSTOM".equals(kind)){
            return ExerciseFactory.loadCustom(id,name,desc,muscles);
        } else {
            throw new IllegalStateException("Custom exercise expected, invalid [KIND] value");
        }
    }

    

    

}
