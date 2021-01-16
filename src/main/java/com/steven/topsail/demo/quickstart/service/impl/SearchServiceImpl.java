package com.steven.topsail.demo.quickstart.service.impl;

import com.steven.topsail.demo.quickstart.service.ISearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Steven
 */
@Slf4j
@Service
public class SearchServiceImpl implements ISearchService {

    private static final int DEMO_NUMBER = 100;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 生成模拟数据
     *
     * @throws Exception
     */
    @Override
    public void createDemoData() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < DEMO_NUMBER; i++) {
            IndexRequest indexRequest = new IndexRequest("posts");
            Map<String, Object> jsonMap = new HashMap<>(6);
            jsonMap.put("user", RandomStringUtils.randomAlphabetic(8));
            jsonMap.put("date", new Date());
            jsonMap.put("message", RandomStringUtils.randomAlphabetic(16));
            indexRequest.source(jsonMap);
            indexRequest.id(UUID.randomUUID().toString());
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            Thread.sleep(10);
        }
        long cost = System.currentTimeMillis() - start;
        log.info("创建 {} 条模拟数据，耗时：{} ms", DEMO_NUMBER, cost);
    }

    /**
     * 采用 Bulk API 生成模拟数据
     *
     * @throws Exception
     */
    @Override
    public void createDemoDataBulk() throws Exception {

        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            BulkRequest bulkRequest = new BulkRequest();
            for (int j = 0; j < 10000; j++) {
                IndexRequest indexRequest = new IndexRequest("posts");
                Map<String, Object> jsonMap = new HashMap<>(6);
                jsonMap.put("user", RandomStringUtils.randomAlphabetic(8));
                jsonMap.put("date", new Date());
                jsonMap.put("message", RandomStringUtils.randomAlphabetic(16));
                indexRequest.source(jsonMap);
                indexRequest.id(UUID.randomUUID().toString());
                bulkRequest.add(indexRequest);
            }
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            long cost = System.currentTimeMillis() - start;
            log.info("批量创建 {} 条模拟数据，耗时：{} ms", 10000, cost);
        }

    }

    /**
     * 批量删除数据
     *
     * @param indexName
     * @throws IOException
     */
    @Override
    public void deleteRecord(String indexName) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(indexName);
        deleteByQueryRequest.setConflicts("proceed");
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("date").lt(new Date());
        deleteByQueryRequest.setQuery(rangeQueryBuilder);
        restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
    }

    /**
     * 获取指定index和id的 数据
     *
     * @param index
     * @param id
     * @return
     */
    public Map<String, Object> getNameById(String index, String id) throws IOException {
        GetRequest getRequest = new GetRequest(index, id);
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        Map<String, Object> source = getResponse.getSource();
        System.out.println("查询结果: " + source);
        return source;
    }

    public SearchHits search(String index, String key, String value) {
        QueryBuilder matchQueryBuilder = QueryBuilders.matchPhraseQuery(key, value);
//        matchQueryBuilder.
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        sourceBuilder.query(QueryBuilders.termQuery("content", content));
        sourceBuilder.query(matchQueryBuilder);
        sourceBuilder.from(0);
        sourceBuilder.size(100);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse;
        List<Map<String, Object>> list = new ArrayList<>();
        SearchHits searchHits = null;
        try {
            searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            searchHits = searchResponse.getHits();
            for (SearchHit hit : searchHits.getHits()) {
                System.out.println(hit.getSourceAsString());
                list.add(hit.getSourceAsMap());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchHits;
    }

    public long deleteArticle(String titleName) {
        long deleteNum = 0l;

        SearchHits searchHits = search("article-2019.08.08.03", "title", titleName);
        System.out.println("Exxcute Start");
        deleteCommon(searchHits);
        //deleteAsync(searchHits);
        System.out.println("Exxcute Done");
        return deleteNum;

    }

    /**
     * 正常删除
     *
     * @param searchHits
     */
    private void deleteCommon(SearchHits searchHits) {
        DeleteRequest deleteRequest = new DeleteRequest();
        for (SearchHit hit : searchHits.getHits()) {
            deleteRequest = new DeleteRequest("article-2019.08.08.03", hit.getId());
            try {
                DeleteResponse deleteResponse = this.restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
                System.out.println("Delete Done【" + deleteResponse.getId() + "】,Status:【" + deleteResponse.status() + "】");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 异步删除
     *
     * @param searchHits
     */
    private void deleteAsync(SearchHits searchHits) {
        DeleteRequest deleteRequest = new DeleteRequest();
        for (SearchHit hit : searchHits.getHits()) {
            deleteRequest = new DeleteRequest("article-2019.08.08.03", hit.getId());


            //异步删除
            this.restHighLevelClient.deleteAsync(deleteRequest, RequestOptions.DEFAULT, new ActionListener<DeleteResponse>() {
                @Override
                public void onResponse(DeleteResponse deleteResponse) {
                    RestStatus restStatus = deleteResponse.status();
                    int status = restStatus.getStatus();
                    deleteResponse.getId();
                    System.out.println("Delete Done【" + deleteResponse.getId() + "】,Status:【" + status + "】");
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    System.out.println("ERROR  " + hit.getId());
                }
            });
        }

    }
}
