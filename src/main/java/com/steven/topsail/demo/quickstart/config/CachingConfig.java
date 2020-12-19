package com.steven.topsail.demo.quickstart.config;

import com.asiainfo.bits.core.redis.client.RedisClient;
import com.steven.topsail.demo.quickstart.cache.MultiSpeedCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Arrays;

import static java.util.stream.Collectors.joining;

/**
 * @author Steven
 * @date 2020-12-12
 */
@Slf4j
@Configuration
@EnableCaching
public class CachingConfig {

    /**
     * 用于开发模式
     */
    @ConditionalOnProperty(prefix = "spring.cache", name = "type", havingValue = "simple")
    static class SimpleCachingConfig {
        @Bean
        public CacheManager cacheManager() {
            log.info("缓存管理器：ConcurrentMapCacheManager");
            return new ConcurrentMapCacheManager();
        }
    }

    /**
     * 用于生产模式
     */
    @ConditionalOnProperty(prefix = "spring.cache", name = "type", havingValue = "redis")
    static class RedisCachingConfig {

        @Resource(name = "pubRedisClient")
        private RedisClient pubRedisClient;

        @Bean
        public CacheManager cacheManager(RedisClient pubRedisClient) {
            log.info("加载缓存管理器：MultiSpeedCacheManager...");
            return new MultiSpeedCacheManager(pubRedisClient);
        }

    }

    static class MyKeyGenerator extends CachingConfigurerSupport {
        /**
         * 自定义缓存key：@类名.方法名(参数值)
         */
        @Override
        @Bean("cacheKeyGenerator")
        public KeyGenerator keyGenerator() {
            log.info("自定义缓存 KeyGenerator...");
            return (target, method, params) -> {
                String className = target.getClass().getName();
                String paramsStr = Arrays.stream(params).map(String::valueOf).collect(joining(",", "(", ")"));
                return "@" + className + "." + method.getName() + paramsStr;
            };
        }
    }
}
