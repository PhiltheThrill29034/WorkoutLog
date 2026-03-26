package com.gainengine.logic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.gainengine.model.Exercise;

public class ExerciseLibrary {
    
    private final Map<UUID,Exercise> exerciseById = new LinkedHashMap<>();
    private List<Exercise> cacheAll = null;
    private List<Exercise> cacheCustoms = null;
    

    private void invalidate() {
        this.cacheAll = null;
        this.cacheCustoms = null;
    }

    public void register(Exercise e){

        exerciseById.put(e.getId(),e);
        invalidate();
        
    }

    public void registerAll(List<Exercise> exercises){
        for (Exercise e: exercises){
            exerciseById.put(e.getId(),e);
        }
        invalidate();
    }

    public void remove (UUID id){
        exerciseById.remove(id);
        invalidate();
    }

    public Exercise getById(UUID id){
        return exerciseById.get(id);
    }

    

    public List<Exercise> getAllCustoms(){
        if (cacheCustoms==null){
            List<Exercise> freshCustoms = getAll().stream().filter(e->e.getType()==null).toList();
            cacheCustoms = Collections.unmodifiableList(freshCustoms);
        }
        return this.cacheCustoms;
    }

    public List<Exercise> getAll(){
        //CACHING
        // Is the snapshot missing or burned?
        if (this.cacheAll == null) {
            // "Cache Miss" - Do the work
            List<Exercise> freshList = new ArrayList<>(exerciseById.values());
            freshList.sort(Comparator.comparing(Exercise::getName));
            
            // Store it so we don't have to sort again next time
            this.cacheAll = Collections.unmodifiableList(freshList);
        }
        
        return this.cacheAll; // Return the snapshot (New or Existing)
    }

    public int getSize(){
        return exerciseById.size();
    }

    public Exercise getByIndex(int index){
        List<Exercise> asList = new ArrayList<>(exerciseById.values());
        if (index>asList.size() || index <0 ){
            throw new IllegalArgumentException("Invalid exercise index given.");
        }
        return asList.get(index);
    }

}
