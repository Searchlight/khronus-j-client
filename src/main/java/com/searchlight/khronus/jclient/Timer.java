package com.searchlight.khronus.jclient;

import java.util.Map;

public class Timer extends Measure {

    public Timer(String metricName, long value, long timestamp) {
	super(metricName, value, timestamp);
    }
    
    public Timer(String metricName, long value, long timestamp, Map<String, String> dimensions) {
	super(metricName, value, timestamp, dimensions);
    }

    @Override
    public MetricType getType() {
	return MetricType.timer;
    }

}
