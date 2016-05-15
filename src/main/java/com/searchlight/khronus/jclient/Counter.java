package com.searchlight.khronus.jclient;

import java.util.Map;

public class Counter extends Measure {
    
    public Counter(String metricName, long value, long timestamp) {
        super(metricName, value, timestamp);
    }

    public Counter(String metricName, long value, long timestamp, Map<String, String> tags) {
        super(metricName, value, timestamp, tags);
    }

    @Override
    public MetricType getType() {
        return MetricType.counter;
    }

}
