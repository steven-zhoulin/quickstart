package com.steven.topsail.demo.quickstart.service.impl;

import com.steven.topsail.demo.quickstart.service.IIndexService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author Steven
 * @date 2021-01-15
 */
@Slf4j
@Service
public class IndexServiceImpl implements IIndexService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 创建索引
     *
     * @param indexName 索引名
     * @param request
     * @throws IOException
     */
    @Override
    public void createIndex(String indexName, CreateIndexRequest request) throws IOException {
        if (!existsIndex(indexName)) {
            log.info("创建索引: {}", indexName);
            CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            System.out.println(response.toString());
            log.info("索引创建结果: {}", response.isAcknowledged());
        } else {
            log.warn("索引：{}，已经存在，不能再创建。", indexName);
        }
    }

    /**
     * 删除索引
     *
     * @param index
     * @throws IOException
     */
    @Override
    public void deleteIndex(String index) throws IOException {
        log.info("删除索引: {}", index);
        if (existsIndex(index)) {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index);
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            log.info("删除响应: {}", delete.isAcknowledged());
        }
    }

    /**
     * 判断索引是否存在
     *
     * @param index
     * @return
     * @throws IOException
     */
    @Override
    public boolean existsIndex(String index) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);
        boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        log.debug("existsIndex: {}", exists);
        return exists;
    }

}
