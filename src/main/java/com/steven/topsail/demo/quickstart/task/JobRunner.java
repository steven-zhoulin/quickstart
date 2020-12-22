package com.steven.topsail.demo.quickstart.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author Steven
 * @date 2020-12-22
 */
@Slf4j
@Service
public class JobRunner implements CommandLineRunner {

//    @Autowired
//    private SysJobMapper sysJobMapper;

    @Autowired
    private CronTaskRegistrar cronTaskRegistrar;

    @Override
    public void run(String... args) {
        // 初始加载数据库里状态为正常的定时任务
        List<CacheVersion> jobList = Arrays.asList(
                CacheVersion.builder().name("c1").syncCron("*/1 * * * * *").build()
        );
        //sysJobMapper.getSysJobListByStatus(SysJobStatus.NORMAL.index());
        if (!CollectionUtils.isEmpty(jobList)) {
            for (CacheVersion job : jobList) {
                SchedulingRunnable task = new SchedulingRunnable(job.getName());
                cronTaskRegistrar.addCronTask(task, job.getSyncCron());
            }
            log.info("定时任务已加载完毕...");
        }
    }
}