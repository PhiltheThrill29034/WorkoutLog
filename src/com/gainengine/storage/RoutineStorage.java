package com.gainengine.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.gainengine.logic.ExerciseLibrary;
import com.gainengine.model.Exercise;
import com.gainengine.model.WorkoutRoutine;
import com.gainengine.utils.FormatUtils;
import com.gainengine.utils.ParsingUtils;

public class RoutineStorage implements StorageProvider<WorkoutRoutine> {
    
    Path path;
    ExerciseLibrary library;

    public RoutineStorage(Path p,ExerciseLibrary library){
        this.path=p;
        this.library=library;
    }

    void write(List<String> lines, StandardOpenOption... opts) throws IOException{
        if (path.getParent()!=null) Files.createDirectories(path.getParent());
        Files.write(path,lines,opts);
    }

    public void save (List<WorkoutRoutine> routines) throws IOException{
        List<String> allLines = new ArrayList<>();
        for (WorkoutRoutine r: routines){
            allLines.addAll(routineToLines(r));
        }
        write(allLines);
    }   

    private List<String> routineToLines(WorkoutRoutine r){
        List<String> lines = new ArrayList<>();
        lines.add("R_START");
        lines.add("NAME:"+r.getName());
        for (Exercise e : r.getExerciseList()){
            lines.add("EX_ID:"+e.getId());
        }
        lines.add("ROUTINE_END");

        return lines;
    }

    public LoadResult<WorkoutRoutine> loadAll() throws IOException{
        List<WorkoutRoutine> routines = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (!Files.exists(path)){
            return LoadResult.empty();
        }
        List<String> lines = Files.readAllLines(path);
        List<List<String>> routineBlocks = ParsingUtils.extractBlocks(lines,"R_START","ROUTINE_END");
        int blockCount=0;
        for (List<String> routine : routineBlocks ){
            blockCount++;
            try {
                WorkoutRoutine r = parseRoutine(routine,warnings);
                routines.add(r);
            } catch (IllegalStateException e){
                warnings.add("Error parsing routine in block "+blockCount+". Reason : " + e.getMessage());
            }
        }

        return new LoadResult<>(routines,warnings);
    }

    private WorkoutRoutine parseRoutine(List<String> lines,List<String> warnings){
        String name="";
        UUID id = null;
        List<Exercise> routineExercises = new ArrayList<>();
        int count=0;
        for (String line: lines){
            count++;
            line=line.trim();
            if (line.startsWith("NAME")){
                name = FormatUtils.valueAfterColon(line);
            } else if (line.startsWith("EX_ID")){
                
                try {
                    id = UUID.fromString(FormatUtils.valueAfterColon(line));
                    ParsingUtils.validate(id, "ID");
                    Exercise fromId = library.getById(id);

                    if (fromId!=null){
                        routineExercises.add(fromId);
                    } else {
                        warnings.add("Exercise with ID:"+id+"does not exist or had been deleted.");
                    }

                } catch (IllegalStateException e){
                    warnings.add("Exercise at line "+count+": "+e.getMessage());
                } catch (IllegalArgumentException e){
                    warnings.add("Exercise at line "+count+": "+e.getMessage());
                }
            }

            


            
        }

        ParsingUtils.validate(name, "NAME", 100);

        if (routineExercises.isEmpty()){
            throw new IllegalStateException("Routine does not contain any exercises or they were deleted");
        }

        return new WorkoutRoutine(name,routineExercises);

    }
}
