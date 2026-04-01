package com.gainengine.storage;

import java.io.IOException;
import java.util.List;

public interface StorageProvider<T> {

    public LoadResult<T> loadAll() throws IOException;
    public void save(List<T> data) throws IOException;
}  
    
