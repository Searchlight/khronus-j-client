package com.searchlight.khronus.jclient;

public class IntegrationDemo {

    public static void main(String args[]) throws InterruptedException {
        KhronusClient client = new KhronusClient.Builder()
                .withApplicationName("testApp")
                .withHosts("localhost:8400")
                .withSendIntervalMillis(500l)
                .build();

        for (int j = 0; j < 10; j++) {
            client.incrementCounter("sales", "country=arg", "host=localhost");
            client.recordTime("sale", 2200l, "country=arg", "host=localhost");
            Thread.sleep(300l);
        }

        client.shutdown();
    }
}
