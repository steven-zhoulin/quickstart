package com.steven.topsail.demo.quickstart.controller;

import com.steven.topsail.demo.quickstart.service.IDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Steven
 * @date 2020-11-27
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private IDemoService demoServiceImpl;

    @GetMapping("cache/{name}/{age}/{male}")
    public String cache(@PathVariable("name") String name,
                        @PathVariable("age") Long age,
                        @PathVariable("male") boolean male) {
        return demoServiceImpl.randomString(name, age, male);
    }

    @GetMapping("clear")
    public String clear() {
        demoServiceImpl.clear();
        return "ok";
    }

}
