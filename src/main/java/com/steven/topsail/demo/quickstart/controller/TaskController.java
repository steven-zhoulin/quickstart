package com.steven.topsail.demo.quickstart.controller;

import com.steven.topsail.demo.quickstart.task.autorefresh.ScheduledTaskRegistry;
import com.steven.topsail.demo.quickstart.task.autorefresh.FlushRunnable;
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
    private ScheduledTaskRegistry scheduledTaskRegistry;

    @GetMapping("reload/{n}")
    public String reload(@PathVariable("n") int n) {

        for (Runnable runnable : scheduledTaskRegistry.getScheduledTasks().keySet()) {
            scheduledTaskRegistry.getScheduledTasks().get(runnable).cancel();
            scheduledTaskRegistry.getScheduledTasks().remove(runnable);
        }

        for (int i = 1; i <= n; i++) {
            FlushRunnable task = new FlushRunnable("任务:" + i);
            scheduledTaskRegistry.addCronTask(task, "*/" + i + " * * * * *");

        }

        return "ok";
    }

}
