package com.steven.topsail.demo.quickstart.controller;

import com.steven.topsail.demo.quickstart.service.IIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Steven
 * @date 2021-01-15
 */
@Slf4j
@RestController
public class IndexController {

    @Autowired
    private IIndexService indexService;

    @GetMapping("createIndex/{indexName}")
    public void create(@PathVariable("indexName") String indexName) throws IOException {
        indexService.createIndex(indexName);

    }

    @GetMapping("deleteIndex/{indexName}")
    public void delete(@PathVariable("indexName") String indexName) throws IOException {
        indexService.deleteIndex(indexName);
    }

}
