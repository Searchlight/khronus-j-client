package com.searchlight.khronus.jclient;

import com.searchlight.khronus.jclient.buffer.BoundedBuffer;
import com.searchlight.khronus.jclient.buffer.Buffer;
import com.searchlight.khronus.jclient.buffer.DummyBuffer;
import org.apache.commons.lang3.StringUtils;

/**
 * KhronusClient is an asynchronous client of Khronus.
 * It buffers each measurement taken by the application and then periodically sends them
 * in background to Khronus through its Rest API.
 * Given to its asynchronous functioning, a buffer is maintained internally in memory.
 * Both the buffer size and the interval to send metrics are configurable.
 * It is thread-safe, so one instance per application should be enough.
 */
public class KhronusClient {
    private static final long DEFAULT_INCREMENT = 1L;

    private Buffer buffer;

    /**
     * Helper to build the client.
     * <p/>
     * The properties "hosts" and "applicationName" are required.
     */
    public static class Builder {
        /**
         * Comma separated host:port
         */
        String[] hosts;
        /**
         * Time interval in milliseconds to flush the buffer and send the
         * accumulated metrics (Default to 5 seconds).
         * It must be less than the smallest time window configured in Khronus
         */
        long sendIntervalMillis = 5000L;
        /**
         * Maximum number of measures to be stored in memory within intervals before start discarding them.
         */
        int maximumMeasures = 500000;
        /**
         * Maximum number of connections per host
         */
        int maxConnections = 50;
        /**
         * Application name
         */
        String applicationName;
        /**
         * Act like a dummy and do nothing
         */
        boolean dummy = false;

        /**
         * @param hosts comma separated host:port
         */
        public Builder withHosts(String hosts) {
            this.hosts = hosts.split(",");
            return this;
        }

        /**
         * Time interval in milliseconds to flush the buffer and send the
         * accumulated metrics (Default to 5 seconds).
         * It must be less than the smallest time window configured in Khronus
         * * * 
         * @param interval milliseconds
         */
        public Builder withSendIntervalMillis(long interval) {
            this.sendIntervalMillis = interval;
            return this;
        }

        /**
         * @param max Maximum number of measures to be stored in memory within
         *            intervals before start discarding them
         */
        public Builder withMaximumMeasures(int max) {
            this.maximumMeasures = max;
            return this;
        }

        /**
         * @param max Maximun number of connections per host
         */
        public Builder withMaxConnections(int max) {
            this.maxConnections = max;
            return this;
        }

        /**
         * Metrics will be reported as "applicationName:metricName"
         *
         * @param appName Application name
         */
        public Builder withApplicationName(String appName) {
            this.applicationName = StringUtils.join(appName);
            return this;
        }
        
        /**
         * Act like a dummy and do nothing. Default is false.
         * @param dummy
         */
        public Builder withDummyMode(boolean dummy) {
            this.dummy = dummy;
            return this;
        }

        /**
         * Create an instance of a Client. One per application is recommended
         *
         * @return an new instance of Client
         */
        public KhronusClient build() {
            validate();
            return new KhronusClient(this);
        }

        private void validate() {
            if (hosts == null | hosts.length == 0) {
                throw new RuntimeException(
                        "Fail to build KhronusClient. Please provide a host to send metrics to Khronus");
            }
        }
    }

    private KhronusClient(Builder builder) {
	if (builder.dummy) {
	    this.buffer = new DummyBuffer();
	} else {
	    this.buffer = new BoundedBuffer(new KhronusConfig(
		    builder.applicationName, builder.maximumMeasures,
		    builder.sendIntervalMillis, builder.hosts,
		    builder.maxConnections));
	}
    }

    /**
     * Record the duration of an event. The timestamp at which happened is
     * assumed to be the current time.
     *
     * @param metricName unique name within application
     * @param time       duration in milliseconds
     * @param tags optional array of dimensions, format like "key=value".
     */
    public void recordTime(String metricName, long time, String... tags) {
        this.recordTime(metricName, time, System.currentTimeMillis(), tags);
    }

    /**
     * Record the duration of an event specifying the timestamp at which
     * happened.
     *
     * @param metricName unique name within application
     * @param time       duration in milliseconds
     * @param timestamp  event start time in milliseconds since epoch
     * @param tags optional array of dimensions, format like "key=value". 
     */
    public void recordTime(String metricName, long time, long timestamp, String... tags) {
        buffer.add(new Timer(metricName, time, timestamp, Measure.tagsToMap(tags)));
    }

    /**
     * Increment by one the value of the specified metric
     *
     * @param metricName unique name within application
     * @param tags optional array of dimensions, format like "key=value".
     */
    public void incrementCounter(String metricName, String... tags) {
        this.incrementCounter(metricName, DEFAULT_INCREMENT, tags);
    }

    /**
     * Increment by N the value of the specified metric
     *
     * @param metricName unique name within application
     * @param counts     number of times to be incremented
     * @param tags optional array of dimensions, format like "key=value".
     */
    public void incrementCounter(String metricName, long counts, String... tags) {
        this.incrementCounter(metricName, counts, System.currentTimeMillis(), tags);
    }

    /**
     * Increment by n the value of the specified metric specifying the timestamp
     * at which happened
     *
     * @param metricName unique name within application
     * @param counts     number of times to be incremented
     * @param timestamp  when the event happened in milliseconds since epoch
     * @param tags optional array of dimensions, format like "key=value".
     */
    public void incrementCounter(String metricName, long counts, long timestamp, String... tags) {
        buffer.add(new Counter(metricName, counts, timestamp, Measure.tagsToMap(tags)));
    }

    /**
     * Record a gauge value. The timestamp at which happened is
     * assumed to be the current time.
     *
     * @param metricName unique name within application
     * @param value      gauge value
     * @param tags optional array of dimensions, format like "key=value".
     */
    public void recordGauge(String metricName, long value, String... tags) {
        this.recordGauge(metricName, value, System.currentTimeMillis(), tags);
    }

    /**
     * Record a gauge value specifying the timestamp at which
     * happened.
     *
     * @param metricName unique name within application
     * @param value       gauge value
     * @param timestamp  event start time in milliseconds since epoch
     * @param tags optional array of dimensions, format like "key=value".
     */
    public void recordGauge(String metricName, long value, long timestamp, String... tags) {
        buffer.add(new Gauge(metricName, value, timestamp, Measure.tagsToMap(tags)));
    }
    /**
     * Shutdown Gracefully Khronus executors
     * 
     **/ 
    public void shutdown() {
        buffer.shutdown();
    }

}
