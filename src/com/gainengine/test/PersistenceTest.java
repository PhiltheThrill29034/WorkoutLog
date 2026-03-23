package com.gainengine.test;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

import com.gainengine.logic.ExerciseFactory;
import com.gainengine.logic.ExerciseLibrary;
import com.gainengine.model.Exercise;
import com.gainengine.model.Muscles;
import com.gainengine.storage.CustomStorage;

public class PersistenceTest {
    
    public static void main(String[] args) {
        
        Exercise ex = ExerciseFactory.createCustom("Custom", "Custom Description", EnumSet.of(Muscles.BACK,Muscles.BICEPS));
        CustomStorage storage = new CustomStorage(Path.of("saved_data/customstest.txt"));
        ExerciseLibrary lib = new ExerciseLibrary();
        try {
            storage.saveAllCustoms(List.of(ex));
        }
        catch (IOException e){
            System.err.println("Error loading customs");
        }

        try{
            lib.registerAll(storage.loadCustoms());
            System.out.println("Success!");
        } catch (IOException e){
            System.out.println("Couldn't load");
        }

        for (Exercise e : lib.getAllCustoms()){
            System.out.println(e);
        }
    }
}
