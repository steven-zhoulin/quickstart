package com.steven.topsail.demo.quickstart.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Steven
 * @date 2021-01-15
 */
@Slf4j
@Configuration
public class RestHighLevelClientConfig {

    @Value("${elastic.hosts:10.230.55.48}")
    private String hosts;

    @Value("${elastic.username:elastic}")
    private String username;

    @Value("${elastic.password:123456}")
    private String password;

    @Bean
    public RestHighLevelClient restHighLevelClient() {

        String[] hosts = this.hosts.split(",");
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            httpHosts[i] = new HttpHost(hosts[i], 9200, "http");
        }

        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts)
            .setRequestConfigCallback(requestConfigBuilder -> {
                requestConfigBuilder.setConnectTimeout(-1);
                requestConfigBuilder.setSocketTimeout(-1);
                requestConfigBuilder.setConnectionRequestTimeout(-1);
                return requestConfigBuilder;
            }).setHttpClientConfigCallback(httpClientBuilder -> {
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
                httpClientBuilder.disableAuthCaching();
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            });

        return new RestHighLevelClient(restClientBuilder);

    }

}
