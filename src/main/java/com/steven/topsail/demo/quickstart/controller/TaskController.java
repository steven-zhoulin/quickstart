package com.steven.topsail.demo.quickstart.controller;

import com.steven.topsail.demo.quickstart.task.CronTaskRegistrar;
import com.steven.topsail.demo.quickstart.task.SchedulingRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Steven
 * @date 2020-12-22
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private CronTaskRegistrar cronTaskRegistrar;

    @GetMapping("reload/{n}")
    public String reload(@PathVariable("n") int n) {

        for (Runnable runnable : cronTaskRegistrar.getScheduledTasks().keySet()) {
            cronTaskRegistrar.getScheduledTasks().get(runnable).cancel();
            cronTaskRegistrar.getScheduledTasks().remove(runnable);
        }

        for (int i = 1; i <= n; i++) {
            SchedulingRunnable task = new SchedulingRunnable("任务:" + i);
            cronTaskRegistrar.addCronTask(task, "*/" + i + " * * * * *");

        }

        return "ok";
    }

}
