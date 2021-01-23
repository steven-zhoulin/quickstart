package com.steven.topsail.demo.quickstart.service;

import org.elasticsearch.client.indices.CreateIndexRequest;

import java.io.IOException;

/**
 * @author Steven
 * @date 2021-01-15
 */
public interface IIndexService {
    /**
     * 创建索引
     *
     * @param index
     * @throws IOException
     */
    void createIndex(String index) throws IOException;

    /**
     * 判断索引是否存在
     *
     * @param index
     * @return
     * @throws IOException
     */
    boolean existsIndex(String index) throws IOException;

    /**
     * 删除索引
     *
     * @param index
     * @throws IOException
     */
    void deleteIndex(String index) throws IOException;
}
