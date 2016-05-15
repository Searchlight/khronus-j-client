package com.searchlight.khronus.jclient;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http client wrapper.
 */
public class Sender {
    private static final Logger LOG = LoggerFactory.getLogger(Sender.class);

    private static final int socketTimeout = 3000;
    private static final int connectionRequestTimeout = 1000;
    private static final int connectTimeout = 1000;

    private final String[] hosts;
    private final CloseableHttpClient httpClient;

    public Sender(KhronusConfig config) {
        this.hosts = config.getHosts();

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(config.getMaxConnections());

        HttpClientBuilder builder = HttpClients.custom().setConnectionManager(connManager);

        this.httpClient = builder.build();

        LOG.debug("Sender to send metrics created [Hosts: {}; MaxConnections: {}; socketTimeout: {}; connectionRequestTimeout: {}; connectTimeout: {}]",
                config.getHosts(), config.getMaxConnections(), socketTimeout, connectionRequestTimeout, connectTimeout);
    }

    public void send(String json) throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://%s/khronus/metrics", getHost()));
        httpPost.setEntity(getEntity(json));
        httpPost.setConfig(getDefaultConfig());

        httpClient.execute(httpPost, new BasicResponseHandler());
    }

    private HttpEntity getEntity(String json) {
        return EntityBuilder.create()
                .setText(json)
                .setContentType(ContentType.APPLICATION_JSON)
                .gzipCompress()
                .build();
    }

    private RequestConfig getDefaultConfig() {
        return RequestConfig.custom()
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();
    }

    private String getHost() {
        return hosts[0]; //FIXME Must implement some sort of round-robin algorithm
    }

    /**
     * Gracefully httpClient shutdown
     */
    public void shutdown() {
        try {
            this.httpClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
