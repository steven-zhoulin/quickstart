package com.steven.topsail.demo.quickstart.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @date 2020-11-27
 */
@RestController
public class ProbeController {

    private static final String serverTime = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss").format(LocalDateTime.now());

    private long count = 1;
    private long interval = 0;

    @Value("#{${bits.service.timeout.map:{k1:1}}}")
    private Map<String, Long> timeoutMapping = new HashMap<>();

    @GetMapping("/probe.jsp")
    public String probe() {

        System.out.println(timeoutMapping);
        for (String key : timeoutMapping.keySet()) {
            System.out.println("key: " + key + " -> value: " + timeoutMapping.get(key));
        }

        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String msg = String.format("%-30s | %s", "OK", serverTime);
        System.out.printf("%s, count: %d\n", msg, count++);
        return msg;
    }

    @GetMapping("/sleep/{interval}")
    public void sleep(@PathVariable("interval") long interval) {
        this.interval = interval * 1000;
        System.out.println("设置心跳访问时长: " + interval);
    }

}
