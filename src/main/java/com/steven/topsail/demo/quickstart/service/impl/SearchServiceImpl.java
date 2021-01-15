package com.steven.topsail.demo.quickstart.service.impl;

import com.steven.topsail.demo.quickstart.service.ISearchService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Steven
 */
@Service
public class SearchServiceImpl implements ISearchService {

    private RestHighLevelClient client;

    public SearchServiceImpl() {
        RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(
                new HttpHost("10.230.55.48", 9200, "http")
            )
        );
        this.client = client;
    }

    public void shutdown() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取指定index和id的 数据
     *
     * @param index
     * @param id
     * @return
     */
    public String getNameById(String index, String id) {
        GetRequest getRequest = new GetRequest(index, id);
        GetResponse getResponse = null;
        try {
            getResponse = this.client.get(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Object> source = getResponse.getSource();
        System.out.println("查询结果: " + source);
        return (String) source.get("FUNC_NAME");
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
            searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
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
                DeleteResponse deleteResponse = this.client.delete(deleteRequest, RequestOptions.DEFAULT);
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
            this.client.deleteAsync(deleteRequest, RequestOptions.DEFAULT, new ActionListener<DeleteResponse>() {
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
