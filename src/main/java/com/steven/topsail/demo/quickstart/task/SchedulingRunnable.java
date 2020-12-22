package com.steven.topsail.demo.quickstart.task;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Steven
 * @date 2020-12-22
 */
@Slf4j
public class SchedulingRunnable implements Runnable {
    private String beanName;

    public SchedulingRunnable(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void run() {
        log.info("定时任务开始执行 - bean：{}", beanName);
    }


}
