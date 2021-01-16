package com.steven.topsail.demo.quickstart.controller;

import com.steven.topsail.demo.quickstart.service.impl.SearchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * @author Steven
 * @date 2021-01-15
 */
@RestController
public class SearchController {

    @Autowired
    private SearchServiceImpl searchServiceImpl;

    @GetMapping("createDemoData")
    public void createDemoData() throws Exception {
        searchServiceImpl.createDemoData();
    }

    @GetMapping("createDemoDataBulk")
    public void createDemoDataBulk() throws Exception {
        searchServiceImpl.createDemoDataBulk();
    }

    @GetMapping("truncate/{indexName}")
    public void truncateIndex(@PathVariable("indexName") String indexName) throws IOException {
        searchServiceImpl.deleteRecord(indexName);
    }

    /**
     * 根据索引名和Id查找数据
     *
     * @param index
     * @param id
     * @return
     */
    @GetMapping("/getById/{index}/{id}")
    public Map<String, Object> getById(@PathVariable String index, @PathVariable String id) throws IOException {
        return searchServiceImpl.getNameById(index, id);
    }

    @GetMapping("/search")
    public String search(String content) {
        String article = "";
        //article = esRestService.searchArticle(content);
        return article;
    }

    @GetMapping("/delete")
    public long delete(String title) {
        long deleteNum = searchServiceImpl.deleteArticle(title);
        return deleteNum;
    }

}

