package com.steven.topsail.demo.quickstart.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 *
 * @author Steven
 * @date 2020-11-30
 */
@Slf4j
@Component
public class CacheVersionReloadTask {

    @PostConstruct
    public void init() {

    }

    @Scheduled(initialDelay = 2000, fixedDelay = 2000)
    public void execute() {

    }

}
