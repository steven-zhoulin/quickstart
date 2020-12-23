package com.steven.topsail.demo.quickstart.task.autorefresh;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Steven
 * @date 2020-12-22
 */
@Slf4j
public class SchedulingRunnable implements Runnable {

    private String cacheName;

    public SchedulingRunnable(String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public void run() {
        log.info("刷新缓存: {}", cacheName);
    }


}
