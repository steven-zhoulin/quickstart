package com.steven.topsail.demo.quickstart.cache;

import com.asiainfo.bits.core.redis.client.RedisClient;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.steven.topsail.demo.quickstart.util.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存
 *
 * @author Steven
 * @date 2020-12-12
 */
@Slf4j
public class MultiSpeedCacheManager implements CacheManager {

    /**
     * 本地缓存（一级缓存）
     */
    private com.github.benmanes.caffeine.cache.Cache<String, Object> localCache =
            Caffeine.newBuilder()
                    .maximumSize(50000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();

    /**
     * 分布式缓存（二级缓存）
     */
    private RedisClient pubRedisClient;

    private ConcurrentMap<String, MultiSpeedCache> cacheMap = new ConcurrentHashMap<>(512);

    /**
     * 版本号
     */
    private Map<String, String> cacheVersion = new HashMap<>(512);

    /**
     * 模块名
     */
    private String moduleName;

    public MultiSpeedCacheManager(RedisClient pubRedisClient) {
        this.pubRedisClient = pubRedisClient;

        this.moduleName = "no_module_name";
        try {
            moduleName = SpringContextUtils.getPropertyValue("module-name");
        } catch (Exception e) {
            log.error("应用中未配置 'module-name' 属性，请在 application.yml 中加入配置");
        }

        /**
         * 版本同步线程
         */
        SyncVersionThread syncVersionThread = new SyncVersionThread();
        syncVersionThread.setDaemon(true);
        syncVersionThread.start();
    }

    /**
     * Get the cache associated with the given name.
     * <p>Note that the cache may be lazily created at runtime if the
     * native provider supports it.
     *
     * @param name the cache identifier (must not be {@code null})
     * @return the associated cache, or {@code null} if such a cache
     * does not exist or could be not created
     */
    @Override
    public Cache getCache(String name) {

        MultiSpeedCache multiSpeedCache = cacheMap.get(name);

        if (null != multiSpeedCache) {
            return multiSpeedCache;
        }

        String version = cacheVersion.get(name);
        if (null == version) {
            version = "def";
            cacheVersion.put(name, version);
        }

        multiSpeedCache = new MultiSpeedCache(moduleName, name, version, pubRedisClient, localCache);
        cacheMap.put(name, multiSpeedCache);

        return multiSpeedCache;

    }

    /**
     * Get a collection of the cache names known by this manager.
     *
     * @return the names of all caches known by the cache manager
     */
    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(cacheMap.keySet());
    }

    /**
     * 版本同步线程
     */
    private class SyncVersionThread extends Thread {

        private static final String VKEY = "##==>BITS_CACHE_VERSION<==##";

        @Override
        public void run() {
            while (true) {

                try {
                    Map<String, String> newCacheVersion = pubRedisClient.hgetAll(VKEY);
                    if (newCacheVersion.size() > 0) {
                        cacheVersion = newCacheVersion;

                        for (String name : cacheMap.keySet()) {
                            MultiSpeedCache multiLevelCache = cacheMap.get(name);
                            String version = cacheVersion.get(name);
                            if (null != version) {
                                multiLevelCache.setVersion(version);
                            }
                        }

                    }

                    Thread.sleep(60000);
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }
        }

    }

}
