package com.searchlight.khronus.jclient;

import java.util.Map;

public class Gauge extends Measure {
    public Gauge(String metricName, long value, long timestamp, Map<String, String> tags) {
        super(metricName, value, timestamp, tags);
    }
    @Override
    public MetricType getType() {
        return MetricType.gauge;
    }
}
