package com.steven.topsail.demo.quickstart.task.autorefresh;

import lombok.extern.slf4j.Slf4j;

/**
 * 缓存刷新
 *
 * @author Steven
 * @date 2020-12-22
 */
@Slf4j
public class FlushRunnable implements Runnable {

    /**
     * 缓存名
     */
    private String cacheName;

    public FlushRunnable(String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public void run() {
        log.info("刷新缓存: {}", cacheName);
    }


}
