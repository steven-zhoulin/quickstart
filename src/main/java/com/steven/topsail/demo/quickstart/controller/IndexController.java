package com.steven.topsail.demo.quickstart.controller;

import com.steven.topsail.demo.quickstart.service.IIndexService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Steven
 * @date 2021-01-15
 */
@Slf4j
@RestController
public class IndexController {

    @Autowired
    private IIndexService indexService;

    @GetMapping("createIndex/{indexName}")
    public void create(@PathVariable("indexName") String indexName) throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        createIndexRequest.settings(
            Settings.builder()
                .put("index.number_of_shards", "5")
                .put("index.number_of_replicas", "0")
                .build()
        );
        indexService.createIndex(indexName, createIndexRequest);

    }

    @GetMapping("deleteIndex/{indexName}")
    public void delete(@PathVariable("indexName") String indexName) throws IOException {
        log.info("删除索引: {}", indexName);
        indexService.deleteIndex(indexName);
    }

}
