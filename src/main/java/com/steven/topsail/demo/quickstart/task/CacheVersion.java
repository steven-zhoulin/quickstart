package com.steven.topsail.demo.quickstart.task;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Steven
 * @date 2020-12-22
 */
@Builder
@Getter
@Setter
public class CacheVersion {
    private String name;
    private LocalDateTime version;
    private String syncCron;
}
