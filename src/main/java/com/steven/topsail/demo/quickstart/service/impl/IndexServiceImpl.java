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
import org.elasticsearch.common.xcontent.XContentType;
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
     * @throws IOException
     */
    @Override
    public void createIndex(String indexName) throws IOException {

        if (!existsIndex(indexName)) {
            log.info("创建索引: {}", indexName);
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            createIndexRequest.source("{\n" +
                    "  \"settings\": {\n" +
                    "    \"number_of_shards\": \"5\",\n" +
                    "    \"number_of_replicas\": \"0\"\n" +
                    "  },\n" +
                    "  \"mappings\": {\n" +
                    "    \"properties\": {\n" +
                    "      \"DONE_DATE\": {\n" +
                    "        \"type\": \"date\"\n" +
                    "      },\n" +
                    "      \"FUNC_ID\": {\n" +
                    "        \"type\": \"text\"\n" +
                    "      },\n" +
                    "      \"FUNC_IMG\": {\n" +
                    "        \"type\": \"text\"\n" +
                    "      },\n" +
                    "      \"FUNC_LEVEL\": {\n" +
                    "        \"type\": \"long\"\n" +
                    "      },\n" +
                    "      \"FUNC_NAME\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"analyzer\": \"ik_max_word\",\n" +
                    "        \"search_analyzer\": \"ik_max_word\"\n" +
                    "      },\n" +
                    "      \"FUN_SEQ\": {\n" +
                    "        \"type\": \"long\"\n" +
                    "      },\n" +
                    "      \"MODULE_TYPE\": {\n" +
                    "        \"type\": \"text\"\n" +
                    "      },\n" +
                    "      \"PARENT_FUNC_ID\": {\n" +
                    "        \"type\": \"text\"\n" +
                    "      },\n" +
                    "      \"STATE\": {\n" +
                    "        \"type\": \"text\"\n" +
                    "      },\n" +
                    "      \"VIEWNAME\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"analyzer\": \"ik_max_word\",\n" +
                    "        \"search_analyzer\": \"ik_max_word\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}",
                XContentType.JSON);

            CreateIndexResponse response = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            System.out.println(response.toString());
            log.info("索引创建结果: {}", response.isAcknowledged());
        } else {
            log.warn("索引：{}，已经存在，不能再创建。", indexName);
        }
    }

    /**
     * 删除索引
     *
     * @param indexName
     * @throws IOException
     */
    @Override
    public void deleteIndex(String indexName) throws IOException {
        log.info("删除索引: {}", indexName);
        if (existsIndex(indexName)) {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            log.info("删除响应: {}", delete.isAcknowledged());
        }
    }

    /**
     * 判断索引是否存在
     *
     * @param indexName
     * @return
     * @throws IOException
     */
    @Override
    public boolean existsIndex(String indexName) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        log.debug("existsIndex: {}", exists);
        return exists;
    }

}
