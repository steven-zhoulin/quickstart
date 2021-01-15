package com.steven.topsail.demo.quickstart.controller;

import com.steven.topsail.demo.quickstart.service.impl.SearchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Steven
 * @date 2021-01-15
 */
@RestController
public class SearchController {

    @Autowired
    private SearchServiceImpl searchServiceImpl;

    /**
     * 根据索引名和Id查找数据
     *
     * @param index
     * @param id
     * @return
     */
    @GetMapping("/getById/{index}/{id}")
    public String getById(@PathVariable String index, @PathVariable String id) {
        return searchServiceImpl.getNameById(index, id);
    }

    @GetMapping("/")
    public String all() {
        String article = "";
        article = searchServiceImpl.getNameById("", "");
        return article;
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

