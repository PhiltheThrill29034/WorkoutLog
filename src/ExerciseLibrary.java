import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ExerciseLibrary {
    
    private final Map<UUID,Exercise> exerciseById = new HashMap<>();
    private List<Exercise> exercises = new ArrayList<>();

    
    public void register(Exercise e){

        exerciseById.put(e.getId(),e);
        exercises.add(e);
    }

    public Exercise getById(UUID id){

        return exerciseById.get(id);
    }


    public List<Exercise> getAll() {
        return exercises;
    }

}
