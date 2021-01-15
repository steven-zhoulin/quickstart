package com.steven.topsail.demo.quickstart.controller;

import com.steven.topsail.demo.quickstart.service.impl.SearchServiceImpl;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @date 2021-01-15
 */
@RestController
public class SearchController {

    @Autowired
    private SearchServiceImpl searchServiceImpl;


    public void index() throws IOException {
        searchServiceImpl.index();
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

