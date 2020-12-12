package com.steven.topsail.demo.quickstart.cache;

import com.asiainfo.bits.core.redis.client.RedisClient;
import com.github.benmanes.caffeine.cache.Caffeine;
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
public class MultiLevelCacheManager implements CacheManager {

    /**
     * 本地缓存
     */
    private com.github.benmanes.caffeine.cache.Cache<Object, Object> localCache = Caffeine.newBuilder()
        .maximumSize(10000)
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build();

    private ConcurrentMap<String, MultiLevelCache> cacheMap = new ConcurrentHashMap<>(512);
    private Map<String, String> cacheVersion = new HashMap<>(512);

    private RedisClient pubRedisClient;

    public MultiLevelCacheManager(RedisClient pubRedisClient) {
        this.pubRedisClient = pubRedisClient;
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

        MultiLevelCache multiLevelCache = cacheMap.get(name);
        String version = cacheVersion.get(name);
        if (null == version) {
            version = "100000";
            cacheVersion.putIfAbsent(name, version);
        }

        if (null == multiLevelCache) {
            multiLevelCache = new MultiLevelCache(name, version, pubRedisClient, localCache);
            cacheMap.putIfAbsent(name, multiLevelCache);
        }

        return multiLevelCache;

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

    private class SyncVersionThread extends Thread {

        private static final String VKEY = "##==>BITS_CACHE_VERSION<==##";

        @Override
        public void run() {
            while (true) {

                Map<String, String> newCacheVersion = pubRedisClient.hgetAll(VKEY);
                cacheVersion = newCacheVersion;

                for (String name : cacheMap.keySet()) {
                    MultiLevelCache multiLevelCache = cacheMap.get(name);
                    String version = cacheVersion.get(name);
                    multiLevelCache.setVersion(version);
                }

                //System.out.println("cacheVersion: " + cacheVersion);

                try {
                    Thread.sleep(1000 * 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
