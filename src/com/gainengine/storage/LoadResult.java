package com.gainengine.storage;

import java.util.ArrayList;
import java.util.List;

public record LoadResult<T>(List<T> data, List<String> warnings) {
    public boolean hasWarnings(){
        return !warnings.isEmpty();
    }
    public String getWarnings(){
        if (!hasWarnings()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("WARNINGS:");
        int count=1;
        for (String warning: warnings){
            sb.append(count++).append(": ").append(warning).append("\n");
        }
        return sb.toString();
    }

    public static <T> LoadResult<T> empty(){
        return new LoadResult<T>(new ArrayList<>(), new ArrayList<>());
    }
}
