package com.steven.topsail.demo.quickstart.service.impl;

import com.steven.topsail.demo.quickstart.service.ISearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
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

    @Override
    public void indexRequest() throws IOException {
        /** 从 json 字符串索引数据 */
        IndexRequest indexRequest1 = new IndexRequest("posts");
        String jsonString = "{" +
            "\"user\" : \"张三\"," +
            "\"date\" : \"2013-01-30\"," +
            "\"message\" : \"打酱油\"" +
            "}";
        indexRequest1.source(jsonString, XContentType.JSON);
        indexRequest1.id("10000");
        IndexResponse indexResponse1 = restHighLevelClient.index(indexRequest1, RequestOptions.DEFAULT);
        displayIndexResponse(indexResponse1);

        /** 从 map 对象索引数据 */
        IndexRequest indexRequest2 = new IndexRequest("posts");
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("user", "李四");
        jsonMap.put("date", "2014-01-01");
        jsonMap.put("message", "王婆卖瓜越卖越夸");
        indexRequest2.source(jsonMap);
        indexRequest2.id("10001");
        IndexResponse indexResponse2 = restHighLevelClient.index(indexRequest2, RequestOptions.DEFAULT);
        displayIndexResponse(indexResponse2);

        /** 使用 XContentBuilder 创建对象索引数据 */
        IndexRequest indexRequest3 = new IndexRequest("posts");
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder();
        xContentBuilder.startObject();
        xContentBuilder.field("user", "王五");
        xContentBuilder.timeField("date", new Date());
        xContentBuilder.field("message", "卖烧饼");
        xContentBuilder.endObject();
        indexRequest3.source(xContentBuilder);
        indexRequest3.id("10002");
        IndexResponse indexResponse3 = restHighLevelClient.index(indexRequest3, RequestOptions.DEFAULT);
        displayIndexResponse(indexResponse3);

    }

    private void displayIndexResponse(IndexResponse indexResponse) {
        String index = indexResponse.getIndex();
        String id = indexResponse.getId();
        long version = indexResponse.getVersion();
        log.info("index: {}, id: {}, version: {}", index, id, version);
        if (DocWriteResponse.Result.CREATED == indexResponse.getResult()) {
            log.info("文档第一次创建");
        } else if (DocWriteResponse.Result.UPDATED == indexResponse.getResult()) {
            log.info("文档之前已存在,这次是更新");
        }
    }

    @Override
    public void getApi() throws IOException {
        GetRequest getRequest = new GetRequest("posts", "10000");
        String[] includes = new String[] {"user", "date"};
        String[] excludes = new String[] {"message"};
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
        getRequest.fetchSourceContext(fetchSourceContext);
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        String index = getResponse.getIndex();
        String type = getResponse.getType();
        String id = getResponse.getId();
        if (getResponse.isExists()) {
            long version = getResponse.getVersion();
            String sourceAsString = getResponse.getSourceAsString();
            Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
            byte[] sourceAsBytes = getResponse.getSourceAsBytes();
            log.info("version: {}, sourceAsString: {}", version, sourceAsString);
        } else {
            // 没有发现请求的文档
        }
    }

    @Override
    public void isExist() throws IOException {
        GetRequest getRequest = new GetRequest("posts", "10001");
        getRequest.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);

        boolean exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        log.info("isExist: {}", exists);
    }

    @Override
    public void delete() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("posts","10002");
        deleteRequest.timeout(TimeValue.timeValueSeconds(2));
        deleteRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        log.info("deleteResponse: {}", deleteResponse.getResult());
    }

    @Override
    public void update() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("posts", "10001");
        Map<String, Object> map = new HashMap<>(6);
        map.put("date", new Date());
        map.put("sec", "female");
        updateRequest.doc(map);
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        log.info("updateResponse: {}", updateResponse.getResult());
    }

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

    @Override
    public void multiGet() throws IOException {
        MultiGetRequest multiGetRequest = new MultiGetRequest();
        multiGetRequest.add(new MultiGetRequest.Item("posts", "10000"));
        multiGetRequest.add(new MultiGetRequest.Item("posts", "10001"));
        MultiGetResponse mget = restHighLevelClient.mget(multiGetRequest, RequestOptions.DEFAULT);
        for (MultiGetItemResponse response : mget.getResponses()) {
            Map<String, Object> source = response.getResponse().getSource();
            System.out.println(source);
        }
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

            /** 在批量操作前要求全部分片都处于激活状态 */
            bulkRequest.waitForActiveShards(ActiveShardCount.ALL);
            BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
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
     * @param indexName
     * @param id
     * @return
     */
    public Map<String, Object> getNameById(String indexName, String id) throws IOException {
        GetRequest getRequest = new GetRequest(indexName, id);
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        Map<String, Object> source = getResponse.getSource();
        System.out.println("查询结果: " + source);
        return source;
    }

    /**
     * matchAllQuery 查询所有数据：searchSourceBuilder.query(QueryBuilders.matchAllQuery());
     * termQuery     作为一个整体和目标字段进行匹配：searchSourceBuilder.query(QueryBuilders.termQuery("PARENT_FUNC_ID", "mrs0000"));
     * matchQuery    将搜索词分词，再与目标字段进行匹配，若分词中的任意一个词，与目标字段匹配上，则可查询到: searchSourceBuilder.query(QueryBuilders.matchQuery("FUNC_NAME", "提醒类"));
     *
     * @throws IOException
     */
    @Override
    public void search() throws IOException {

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field highlightField = new HighlightBuilder.Field("FUNC_NAME");
        highlightBuilder.field(highlightField);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");

        SearchRequest searchRequest = new SearchRequest("sec_function");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder = QueryBuilders
            .matchQuery("FUNC_NAME", "备卡激活")
            .fuzziness(Fuzziness.AUTO)
            .prefixLength(3)
            .maxExpansions(10);
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(5);
        searchSourceBuilder.fetchSource(
            new String[]{"MODULE_TYPE", "FUNC_NAME", "FUN_SEQ", "FUNC_ID", "FUNC_LEVEL", "PARENT_FUNC_ID"},
            new String[]{"@timestamp", "@version"}
        );
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.timeout(new TimeValue(5, TimeUnit.SECONDS));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits searchHits = searchResponse.getHits();

        log.info("HTTP状态码: {}，查询时间: {} ms，是否超时: {}，查询命中数量: {}，最大分值: {}",
            searchResponse.status().getStatus(),
            searchResponse.getTook().getMillis(),
            searchResponse.isTimedOut(),
            searchHits.getTotalHits().value,
            searchHits.getMaxScore()
        );

        for (SearchHit searchHit : searchHits.getHits()) {
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField funcName = highlightFields.get("FUNC_NAME");
            Map<String, Object> source = searchHit.getSourceAsMap();
            //log.info("{}", funcName.getFragments());
            log.info("{}", source);
        }
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
