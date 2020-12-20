package com.steven.topsail.demo.quickstart.cache;

import com.asiainfo.bits.core.redis.client.RedisClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 多级缓存
 * <p>
 * TODO: 1.缓存穿透的问题。2.搞懂未实现接口的用途。
 *
 * @author Steven
 * @date 2020-12-12
 */
@Slf4j
public class MultiSpeedCache extends AbstractValueAdaptingCache {

    public static final String VKEY = "##==>BITS_CACHE_VERSION<==##";

    /**
     * 模块名
     */
    private String moduleName;

    /**
     * 缓存名
     */
    private String name;

    /**
     * 缓存版本
     */
    @Getter
    @Setter
    private String version;

    private RedisClient pubRedisClient;

    private MultiSpeedCacheManager multiSpeedCacheManager;

    /**
     * 本地缓存，采用共享模式
     */
    private com.github.benmanes.caffeine.cache.Cache<String, Object> localCache;

    /**
     * Redis缓存的最大过期时间，28天
     */
    private static final long EXPIRATION_TIMEOUT = 86400 * 28;

    private MultiSpeedCache(MultiSpeedCache.Builder builder) {
        super(true);

        Assert.notNull(builder.name, "name must not be null");
        Assert.notNull(builder.version, "version must not be null");
        Assert.notNull(builder.pubRedisClient, "pubRedisClient must not be null");
        Assert.notNull(builder.localCache, "localCache must not be null");
        Assert.notNull(builder.multiSpeedCacheManager, "multiSpeedCacheManager must no be null");

        this.moduleName = builder.moduleName;
        this.name = builder.name;
        this.version = builder.version;
        this.pubRedisClient = builder.pubRedisClient;
        this.localCache = builder.localCache;
        this.multiSpeedCacheManager = builder.multiSpeedCacheManager;
    }

    /**
     * Return the cache name.
     */
    @Override
    public final String getName() {
        return this.name;
    }

    /**
     * Return the underlying native cache provider.
     */
    @Override
    public final com.github.benmanes.caffeine.cache.Cache<String, Object> getNativeCache() {
        return this.localCache;
    }

    /**
     * Perform an actual lookup in the underlying store.
     *
     * @param cacheKey the key whose associated value is to be returned
     * @return the raw store value for the key, or {@code null} if none
     */
    @Override
    protected Object lookup(Object cacheKey) {
        return localCache.getIfPresent(cacheKey);
    }

    /**
     * 1. 先在本地缓存中查找
     * 2. 再在分布式缓存中查找
     *
     * @param key
     * @return
     */
    @Override
    @Nullable
    public ValueWrapper get(Object key) {
        String cacheKey = createCacheKey(key);
        Object value = lookup(cacheKey);

        if (null != value) {
            log.trace("Local  hit: {}", cacheKey);
            return toValueWrapper(value);
        }

        value = pubRedisClient.getObject(cacheKey);
        if (null != value) {
            log.trace("Remote hit: {}", cacheKey);
            localCache.put(cacheKey, value);
        }

        return toValueWrapper(value);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        log.info("invoke MultiSpeedCache.get(Object key, Callable<T> valueLoader), key: {}", key);
        String cacheKey = createCacheKey(key);
        Object value = localCache.getIfPresent(cacheKey);
        if (null == value) {
            value = pubRedisClient.get(cacheKey);
            if (null == value) {
                try {
                    value = toStoreValue(valueLoader.call());
                } catch (Throwable e) {
                    throw new ValueRetrievalException(key, valueLoader, e);
                }
            }
        }

        return (T) fromStoreValue(value);

    }

    /**
     * Associate the specified value with the specified key in this cache.
     * <p>If the cache previously contained a mapping for this key, the old
     * value is replaced by the specified value.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     */
    @Override
    public void put(Object key, Object value) {
        String cacheKey = createCacheKey(key);
        log.trace("put {}", cacheKey);
        Object storeValue = toStoreValue(value);
        localCache.put(cacheKey, storeValue);
        pubRedisClient.set(cacheKey, storeValue, EXPIRATION_TIMEOUT);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        log.debug("putIfAbsent {} -> {}", key, value);
        String cacheKey = createCacheKey(key);
        Object storeValue = toStoreValue(value);
        boolean result = false;
        try {
            result = pubRedisClient.setnx(cacheKey, value);
        } catch (Exception e) {
            log.error("Redis 执行 setnx 失败", e);
            // Redis 挂了，暂时使用本地缓存，注意用作分布式锁时将在多应用中失效
            localCache.put(cacheKey, value);
        }
        if (result) {
            // redis更新成功，本地缓存直接覆盖
            localCache.put(cacheKey, value);
        }
        return toValueWrapper(value);
//        Object existing = this.store.putIfAbsent(key, toStoreValue(value));
//        return toValueWrapper(existing);
    }

    /**
     * Evict the mapping for this key from this cache if it is present.
     *
     * @param key the key whose mapping is to be removed from the cache
     */
    @Override
    public void evict(Object key) {
        this.clear();
    }

    /**
     * Clear the cache through removing all mappings.
     */
    @Override
    public void clear() {
        log.trace("Clear...");
        long now = System.currentTimeMillis();
        multiSpeedCacheManager.saveOrUpdateVersion(this.name, now);
        version = DateFormatUtils.format(now, "ddHHmm");
        this.pubRedisClient.hset(VKEY, this.name, this.version);
    }

    private String createCacheKey(Object key) {
        Objects.requireNonNull(key);

        StringBuilder buffer = new StringBuilder(512);
        buffer.append(moduleName);
        buffer.append(":");
        buffer.append(this.name);
        buffer.append("=");
        buffer.append(this.version);
        buffer.append(":");
        buffer.append(key);

        return buffer.toString();
    }

    public static class Builder {

        private String moduleName;
        private String name;
        private String version;
        private RedisClient pubRedisClient;
        private com.github.benmanes.caffeine.cache.Cache<String, Object> localCache;
        private MultiSpeedCacheManager multiSpeedCacheManager;

        public MultiSpeedCache.Builder moduleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }

        public MultiSpeedCache.Builder name(String name) {
            this.name = name;
            return this;
        }

        public MultiSpeedCache.Builder version(String version) {
            this.version = version;
            return this;
        }

        public MultiSpeedCache.Builder pubRedisClient(RedisClient pubRedisClient) {
            this.pubRedisClient = pubRedisClient;
            return this;
        }

        public MultiSpeedCache.Builder localCache(com.github.benmanes.caffeine.cache.Cache<String, Object> localCache) {
            this.localCache = localCache;
            return this;
        }

        public MultiSpeedCache.Builder multiSpeedCacheManager(MultiSpeedCacheManager multiSpeedCacheManager) {
            this.multiSpeedCacheManager = multiSpeedCacheManager;
            return this;
        }

        public MultiSpeedCache build() {
            return new MultiSpeedCache(this);
        }
    }
}
