package com.steven.topsail.demo.quickstart.task.autorefresh;

import com.steven.topsail.demo.quickstart.task.CacheVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 当表 CACHE_VERSION 表里的同步表达式配置发生变化后可自动生效
 *
 * @author Steven
 * @date 2020-12-22
 */
@Slf4j
@Service
public class AutoReloadTask {

    @Autowired
    private ScheduledTaskRegistry scheduledTaskRegistry;

    @Scheduled(initialDelay = 10000, fixedDelay = 1000 * 6)
    public void reload() {
        log.info("从数据库加载最新的 syncCron 数据");

        /**
         * TODO:
         * 1.从数据库加载最新的 syncCron 数据。
         * 2.遍历数据库数据           -> UPDATE/ADD
         * 3.遍历 CronTaskRegistry  -> DELETE
         */

        // 初始加载数据库里状态为正常的定时任务
        List<CacheVersion> cacheVersions = Arrays.asList(
                CacheVersion.builder()
                        .name("c1")
                        .syncCron("*/1 * * * * *")
                        .build()
        );

        if (CollectionUtils.isEmpty(cacheVersions)) {
            return;
        }

        for (CacheVersion cacheVersion : cacheVersions) {
            FlushRunnable task = new FlushRunnable(cacheVersion.getName());
            scheduledTaskRegistry.addCronTask(task, cacheVersion.getSyncCron());
            log.info("加载定时刷新任务: {} -> {}", cacheVersion.getName(), cacheVersion.getSyncCron());
        }
        log.info("共加载定时刷新任务: {} 条", cacheVersions.size());

    }

}