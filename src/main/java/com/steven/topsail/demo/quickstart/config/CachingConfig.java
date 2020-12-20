package com.steven.topsail.demo.quickstart.config;

import com.asiainfo.bits.core.redis.client.RedisClient;
import com.steven.topsail.demo.quickstart.cache.MultiSpeedCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

        @Value("${spring.cache.version.report.endpoints:127.0.0.1:1982}")
        private String endpoints;

        @Value("${spring.cache.durationMinutes:1}")
        private long durationMinutes;

        @Value("${spring.cache.maximumSize:10000}")
        private long maximumSize;

        @Bean
        public CacheManager cacheManager(RedisClient pubRedisClient) {
            log.info("加载缓存管理器：MultiSpeedCacheManager, durationMinutes: {}, maximumSize: {}, endpoints: {}",
                    durationMinutes, maximumSize, endpoints);

            MultiSpeedCacheManager multiSpeedCacheManager = new MultiSpeedCacheManager.Builder()
                    .durationMinutes(durationMinutes)
                    .maximumSize(maximumSize)
                    .pubRedisClient(pubRedisClient)
                    .stringEndpoints(endpoints)
                    .build();
            return multiSpeedCacheManager;
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

                Class<?> targetClass = target.getClass();
                String simpleName = targetClass.getSimpleName();
                String packageName = targetClass.getPackage().getName();

                StringBuilder buff = new StringBuilder(packageName.length() * 2);
                char[] chars = packageName.toCharArray();

                buff.append("@").append(chars[0]);
                for (int i = 1; i < chars.length; i++) {
                    if ('.' == chars[i]) {
                        char c = chars[i + 1];
                        buff.append('.').append(c);
                        i++;
                    }
                }

                buff.append('.').append(simpleName).append('.').append(method.getName());
                String paramsStr = Arrays.stream(params).map(String::valueOf).collect(joining(",", "(", ")"));
                return buff.append(paramsStr).toString();
            };
        }
    }
}
