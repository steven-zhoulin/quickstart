package com.steven.topsail.demo.quickstart.service;

import java.io.IOException;

/**
 * @author Steven
 * @date 2021-01-15
 */
public interface ISearchService {

    /**
     * 创建模拟数据
     *
     * @throws Exception
     */
    void createDemoData() throws Exception;

    void createDemoDataBulk() throws Exception;

    void deleteRecord(String indexName) throws IOException;

    void search() throws IOException;
}
