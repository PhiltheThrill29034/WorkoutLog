package com.gainengine.model;
public enum Muscles {
    
    CHEST("Chest"),
    SHOULDERS("Shoulders"),
    TRICEPS("Triceps"),
    BACK("Back"),
    BICEPS("Biceps"),
    QUADS("Quads"),
    HAMSTRINGS("Hamstrings"),
    CALVES("Calves");


    private final String name;

    Muscles (String name){
        this.name=name;
    }

    public String getName(){
        return this.name;
    }

    

    public Muscles fromName(String name){

        for (Muscles m: Muscles.values()){
            if (m.getName().equalsIgnoreCase(name)){
                return m;
            }
        }
        return null;
    }

}
