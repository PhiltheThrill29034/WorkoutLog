import java.util.Set;
import java.util.UUID;

public class ExerciseFactory {
    
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

    public static Exercise freshCopyOf(Exercise other) {
        return (other.getType() == null) ? 
            loadCustom(other.getId(), other.getName(), other.getDesc(), other.getMuscles()) :
            fromType(other.getType());
    }
}
