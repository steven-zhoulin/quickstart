package com.asiainfo.bits.core.redis;

import com.asiainfo.bits.core.redis.client.RedisClient;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 *
 * @className: RedisFactory
 * @description: Redis工厂类
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-8-5
 */
@Slf4j
public final class RedisFactory {

    static final Map<String, RedisClient> redises = new HashMap<>();

    /**
     * 根据集群名获取RedisClient实例
     *
     * @param clusterName
     * @return
     */
    public static final RedisClient getRedisClient(String clusterName) {
        RedisClient client = redises.get(clusterName);
        if (null == client) {
            throw new NullPointerException("工厂中未找到对应的 RedisClient 客户端实例, cluster: " + clusterName);
        }
        return client;
    }



}