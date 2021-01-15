package com.steven.topsail.demo.quickstart.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Steven
 * @date 2021-01-15
 */
@Configuration
public class RestHighLevelClientConfig {

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
            RestClient.builder(
                new HttpHost("10.230.55.48", 9200, "http")
            )
        );
        return restHighLevelClient;
    }

}
