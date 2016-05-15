package com.searchlight.khronus.jclient.buffer;

import com.searchlight.khronus.jclient.Measure;

/**
 * Dummy to avoid buffering and do nothing. 
 *
 */
public class DummyBuffer implements Buffer {

    @Override
    public void add(Measure measure) {
    }

    @Override
    public void shutdown() {
    }

}
