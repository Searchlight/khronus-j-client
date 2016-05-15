package com.searchlight.khronus.jclient;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Measure {
    private static final Logger LOG = LoggerFactory.getLogger(Measure.class);
    
    private String metricName;
    private long timestamp;
    private long value;
    private Map<String, String> tags;

    public Measure(String metricName, long value, long timestamp) {
        this.metricName = metricName;
        this.timestamp = timestamp;
        this.value = value;
    }
    
    public Measure(String metricName, long value, long timestamp, Map<String, String> tags) {
        this.metricName = metricName;
        this.timestamp = timestamp;
        this.value = value;
        this.tags = tags;
    }
    
    public static Map<String, String> tagsToMap(String[] tags) {
	Map<String, String> dimensionsMap = null;
	try {
	    if ((tags != null) && (tags.length >= 0)) {
		dimensionsMap = new HashMap<String, String>();
		for (String tupple : tags) {
		    String[] split = tupple.split("=");
		    dimensionsMap.put(split[0], split[1]);
		}
	    }
	} catch (Exception e) {
	    LOG.error("Could not add tags. Wrong format?", e);
	}

	return dimensionsMap;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getValue() {
        return value;
    }

    public abstract MetricType getType();


    public String getMetricName() {
        return metricName;
    }

    public Map<String, String> getTags() {
        return tags;
    }

}
