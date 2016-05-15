Java client for Khronus
=======================

A simple Java client for Khronus.

It works by buffering metrics to send them later in a batch way.

## Maven

Not available yet

## How to use it

#### 1) Create the client
```java
new KhronusClient.Builder()
          .withApplicationName("exampleApp")
          .withSendIntervalMillis(3000L)
          .withMaximumMeasures(500000)
          .withHosts("KhronusHost:KhronusPort")
          .build()
```
The application name is prepend to all metrics names, to guarantee uniqueness in Khronus.

The send interval is how often the metrics are sent to Khronus.

Maximum measures is to avoid excessive memory pressure and potentially OOM.

#### 2) Use it to measure a timer
```java
client.recordTime("pageLoad", 300L)
```
This will push to Khronus a metric named "exampleApp:pageLoad" with a measured value of 300 milliseconds. If the event occurs previously use an overloaded method to pass the specific timestamp.

#### 3) Use it to measure a counter
```java
client.incrementCounter("pageVisits")
```

#### $) On app shutdown, release resources
```java
client.shutdown()
```

## Caveats 

1) The send interval should be less or equal than the minor time window configured in Khronus.

2) Take in care that the client uses an LinkedBlockingQueue in each measure, who has a penalty in concurrency (minor on most usages) for adding a element.
