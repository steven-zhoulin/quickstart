package com.steven.topsail.demo.quickstart.task.autorefresh;

import java.util.concurrent.ScheduledFuture;


/**
 * @author Steven
 * @date 2020-12-22
 */
public final class ScheduledTask {

    volatile ScheduledFuture scheduledFuture;

    /**
     * 取消定时任务
     */
    public void cancel() {
        if (null != scheduledFuture) {
            scheduledFuture.cancel(true);
        }
    }
}
