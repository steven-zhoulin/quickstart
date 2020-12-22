package com.steven.topsail.demo.quickstart.task;

import java.util.concurrent.ScheduledFuture;


/**
 * @author Steven
 * @date 2020-12-22
 */
public final class ScheduledTask {

    volatile ScheduledFuture future;

    /**
     * 取消定时任务
     */
    public void cancel() {
        ScheduledFuture future = this.future;
        if (future != null) {
            future.cancel(true);
        }
    }
}
