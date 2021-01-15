package com.steven.topsail.demo.quickstart.service;

import org.elasticsearch.client.indices.CreateIndexRequest;

import java.io.IOException;

/**
 * @author Steven
 * @date 2021-01-15
 */
public interface IIndexService {
    void createIndex(String index, CreateIndexRequest request) throws IOException;

    boolean existsIndex(String index) throws IOException;

    void deleteIndex(String index) throws IOException;
}
