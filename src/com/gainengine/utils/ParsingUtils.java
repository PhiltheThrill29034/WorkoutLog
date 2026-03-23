package com.gainengine.utils;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.gainengine.model.Muscles;

public final class ParsingUtils {
    
    private ParsingUtils(){};

    public static Set<Muscles> parseMuscles(String line){
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

    public static List<List<String>> extractBlocks(List<String> lines,String start, String end){

        List<List<String>> blocks = new ArrayList<>();

        List<String> current = null;

        for (String line : lines){

            line = line.trim();

            if (line.equals(start)){
                current = new ArrayList<>();
            }

            
            else if (line.equals(end)&&current!=null){
                blocks.add(current);
                current = null;
            }

            else if (current!=null){
                current.add(line);
            }

        }

        return blocks;
    }

    public static void validateStringInput(String input, String field, int maxLen){

        if (input==null || input.isBlank()){
            throw new IllegalStateException ("ERROR: ["+field+"] cannot be empty");
        } else if (input.length()>maxLen){
            throw new IllegalStateException ("ERROR: ["+field+"] exceeds max length of "+maxLen);
        }
    }


}
