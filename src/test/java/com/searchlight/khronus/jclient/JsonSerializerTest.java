package com.searchlight.khronus.jclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.searchlight.khronus.jclient.api.Measurement;
import com.searchlight.khronus.jclient.api.MetricBatch;
import com.searchlight.khronus.jclient.api.MetricMeasurement;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JsonSerializerTest {

    @Test
    public void toJson_oneMetric_returnsValidJson() throws IOException {
        JsonSerializer instance = new JsonSerializer(5000l, "demoApp");

        Collection<Measure> measures = new ArrayList<Measure>();
        measures.add(new Timer("responseTime", 1234l, 11111l));
        measures.add(new Timer("responseTime", 456l, 11111l));

        String json = instance.serialize(measures);

        ObjectMapper mapper = new ObjectMapper();
        MetricBatch fromJson = mapper.readValue(json, MetricBatch.class);

        for(MetricMeasurement measure: fromJson.metrics){
            if (measure.name.equals("demoApp:responseTime")){
                Assert.assertEquals(measure.mtype, "timer");
                Assert.assertEquals(measure.measurements.size(), 2);
            } else {
                Assert.fail("invalid name");
            }
        }
    }
    
    @Test
    public void toJson_oneMetricWithTwoRecords_returnsValidJson() throws IOException {
        JsonSerializer instance = new JsonSerializer(5000l, "demoApp");

        Collection<Measure> measures = new ArrayList<Measure>();
        measures.add(new Timer("responseTime", 1234l, 11111l));
        measures.add(new Timer("responseTime", 456l, 153453l));

        String json = instance.serialize(measures);

        ObjectMapper mapper = new ObjectMapper();
        MetricBatch fromJson = mapper.readValue(json, MetricBatch.class);

        for(MetricMeasurement measure: fromJson.metrics){
            if (measure.name.equals("demoApp:responseTime")){
                Assert.assertEquals(measure.mtype, "timer");
                Assert.assertEquals(measure.measurements.size(), 2);
            } else {
                Assert.fail("invalid name");
            }
        }
    }

    @Test
    public void toJson_twoMetrics_returnsValidJson() throws IOException {
        JsonSerializer instance = new JsonSerializer(5000l, null);

        Collection<Measure> measures = new ArrayList<Measure>();
        measures.add(new Timer("responseTime", 1234l, 11111l));
        measures.add(new Timer("responseTime", 456l, 11111l));
        measures.add(new Timer("totalTime", 1234l, 11111l));
        measures.add(new Timer("totalTime", 456l, 11111l));


        String json = instance.serialize(measures);

        ObjectMapper mapper = new ObjectMapper();
        MetricBatch fromJson = mapper.readValue(json, MetricBatch.class);

        for(MetricMeasurement measure: fromJson.metrics){
            if (measure.name.equals("responseTime")){
                Assert.assertEquals(measure.mtype, "timer");
                Assert.assertEquals(measure.measurements.size(), 2);
            } else if (measure.name.equals("totalTime")){
                Assert.assertEquals(measure.mtype, "timer");
                Assert.assertEquals(measure.measurements.size(), 2);
            } else {
                Assert.fail("invalid name");
            }
        }
    }

    @Test
    public void toJson_withCountersAndTimers_returnValidJson() throws Exception {
        JsonSerializer instance = new JsonSerializer(5000l, null);

        Collection<Measure> measures = new ArrayList<Measure>();
        measures.add(new Timer("responseTime", 1234l, 11111l));
        measures.add(new Timer("responseTime", 456l, 11111l));
        measures.add(new Counter("count200", 1234l, 11111l));
        measures.add(new Counter("count200", 456l, 11111l));

        String json = instance.serialize(measures);

        ObjectMapper mapper = new ObjectMapper();
        MetricBatch fromJson = mapper.readValue(json, MetricBatch.class);

        for(MetricMeasurement measure: fromJson.metrics){
            if (measure.name.equals("responseTime")){
                Assert.assertEquals(measure.mtype, "timer");
                Assert.assertEquals(measure.measurements.size(), 2);
            } else if (measure.name.equals("count200")){
                Assert.assertEquals(measure.mtype, "counter");
                Assert.assertEquals(measure.measurements.size(), 2);
            } else {
                Assert.fail("invalid name");
            }
        }
    }

    
    @Test
    public void toJson_withCountersAndTimersAndTags_returnValidJson() throws Exception {
        JsonSerializer instance = new JsonSerializer(5000l, null);

        Collection<Measure> measures = new ArrayList<Measure>();
        measures.add(new Timer("responseTime", 1234l, 11111l, Measure.tagsToMap(new String[]{"country=AR", "currency=ARS"})));
        measures.add(new Timer("responseTime", 456l, 11111l, Measure.tagsToMap(new String[]{"country=BR", "currency=ARS"})));
        measures.add(new Counter("count200", 1234l, 11111l, Measure.tagsToMap(new String[]{"country=BR", "currency=ARS"})));
        measures.add(new Counter("count200", 456l, 11111l, Measure.tagsToMap(new String[]{"country=BR", "currency=ARS"})));

        String json = instance.serialize(measures);

        ObjectMapper mapper = new ObjectMapper();
        MetricBatch fromJson = mapper.readValue(json, MetricBatch.class);

        for(MetricMeasurement measure: fromJson.metrics){
            if (measure.name.equals("responseTime")){
                Assert.assertEquals("timer", measure.mtype);
                Assert.assertEquals(1, measure.measurements.size());
                for(Measurement m: measure.measurements){
                    if (m.values.contains(1234l)){
                        Assert.assertTrue(measure.tags.containsKey("country"));
                        Assert.assertTrue(measure.tags.containsKey("currency"));
                        Assert.assertTrue(measure.tags.containsValue("AR"));
                        Assert.assertTrue(measure.tags.containsValue("ARS"));
                    }
                }
            } else if (measure.name.equals("count200")){
                Assert.assertEquals(measure.mtype, "counter");
                Assert.assertEquals(measure.measurements.size(), 2);
            } else {
                Assert.fail("invalid name");
            }
        }
    }
    


}
