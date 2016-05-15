package com.searchlight.khronus.jclient.buffer;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.searchlight.khronus.jclient.JsonSerializer;
import com.searchlight.khronus.jclient.KhronusConfig;
import com.searchlight.khronus.jclient.Measure;
import com.searchlight.khronus.jclient.Sender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Stores a list of metrics in memory and periodically sends them to Khronus.
 * 
 * It uses a LinkedBlockingQueue who has a penalty in concurrency (minor on most usages) for adding a element.
 */
public class BoundedBuffer implements Buffer {
    private static final Logger LOG = LoggerFactory.getLogger(BoundedBuffer.class);
    /**
     * Metrics stored in memory
     */
    private final LinkedBlockingQueue<Measure> measures;
    /**
     * Flush periodically the queue and send the metrics to Khronus cluster
     */
    private final ScheduledExecutorService executor;
    /**
     * Http client wrapper
     */
    private final Sender sender;
    /**
     * json serializer
     */
    private JsonSerializer jsonSerializer;
    /**
     * too many measures in the buffer
     */
    private AtomicBoolean overflow = new AtomicBoolean(false);


    public BoundedBuffer(KhronusConfig config) {
        this.measures = new LinkedBlockingQueue<Measure>(config.getMaximumMeasures());
        this.sender = new Sender(config);
        this.jsonSerializer = new JsonSerializer(config.getSendIntervalMillis(), config.getApplicationName());

        BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("KhronusClientSender").build();
        this.executor = Executors.newScheduledThreadPool(1, threadFactory);
        this.executor.scheduleWithFixedDelay(send(), config.getSendIntervalMillis(), config.getSendIntervalMillis(), TimeUnit.MILLISECONDS);

        LOG.debug("Buffer to store metrics created [MaximumMeasures: {}; SendIntervalMillis: {}]",
                config.getMaximumMeasures(), config.getSendIntervalMillis());
    }

    public void add(Measure measure) {
	if (!measures.offer(measure) && overflow.compareAndSet(false, true)) {
	    LOG.warn("Could not add measure because the buffer is full. Start to discard measures until send");
	}
    }

    /**
     * Flush periodically the queue and send the metrics to Khronus cluster
     */
    private Runnable send() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    if (!measures.isEmpty()){
                        LOG.debug("Sending metrics to Khronus...");
                        Collection<Measure> copiedMeasures = new ArrayList<Measure>();
                        measures.drainTo(copiedMeasures);
                        overflow.set(false);
    
                        String json = jsonSerializer.serialize(copiedMeasures);
    
                        LOG.trace("Json to be posted to Khronus: {}", json);
    
                        sender.send(json);
                        LOG.debug("Metrics sent successfully to Khronus");
                    }
                } catch (Throwable reason) {
                    LOG.warn("Error sending metrics to Khronus", reason);
                }
            }
        };
    }

    /**
     * Shutdown gracefully thread pool executor and sender
     */
    public void shutdown() {
        if (! executor.isShutdown() )
            executor.shutdown();
        sender.shutdown();
    }
}
