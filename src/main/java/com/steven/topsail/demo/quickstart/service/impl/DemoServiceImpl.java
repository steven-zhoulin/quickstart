package com.steven.topsail.demo.quickstart.service.impl;

import com.steven.topsail.demo.quickstart.service.IDemoService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Steven
 * @date 2020-12-12
 */
@Service
public class DemoServiceImpl implements IDemoService {

    @Cacheable("randomString")
    @Override
    public String randomString(String name, Long age, boolean male) {
        return name + ":" + age + ":" + male + ":" + UUID.randomUUID().toString();
    }

}
