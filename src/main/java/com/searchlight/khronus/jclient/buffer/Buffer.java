package com.searchlight.khronus.jclient.buffer;

import com.searchlight.khronus.jclient.Measure;

public interface Buffer {

    /**
     * Add measure to the buffer. Discard measure if buffer is full.
     * @param measure
     */
    void add(Measure measure);
    
    /**
     * Free resources.
     */
    void shutdown();
}
