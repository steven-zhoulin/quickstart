package com.steven.topsail.demo.quickstart.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Steven
 * @date 2020-12-22
 */
@Slf4j
//@Component
public class DynamicScheduleTask implements SchedulingConfigurer {

    private ScheduledTaskRegistrar scheduledTaskRegistrar;
    public void destroy() {
        log.info("销毁 scheduledTaskRegistrar");
        scheduledTaskRegistrar.destroy();
    }

    public void reload() {
        init(scheduledTaskRegistrar);
    }

    /**
     * 测试数据，实际可从数据库获取
     */
    @Setter
    private List<Task> tasks = Arrays.asList(
            new Task(1, "任务1", "*/1 * * * * *")
    );

    /**
     * TODO: 修改表的版本号，并同步写入 Redis
     *
     * @param name
     */
    private void execute(String name) {
        log.info("执行动态定时任务: {}", name);
    }

    public void init(ScheduledTaskRegistrar taskRegistrar) {
        scheduledTaskRegistrar = taskRegistrar;
        tasks.forEach(task -> {
            taskRegistrar.addTriggerTask(
                    () -> execute(task.getName()),
                    triggerContext -> {
                        // 定时任务触发，可修改定时任务的执行周期
                        CronTrigger trigger = new CronTrigger(task.getCron());
                        Date nextExecDate = trigger.nextExecutionTime(triggerContext);
                        return nextExecDate;
                    });
        });
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        init(taskRegistrar);
    }

    @Data
    @AllArgsConstructor
    public static class Task {

        /**
         * 主键ID
         */
        private int id;

        /**
         * 任务名称
         */
        private String name;

        /**
         * cron表达式
         */
        private String cron;
    }

}
