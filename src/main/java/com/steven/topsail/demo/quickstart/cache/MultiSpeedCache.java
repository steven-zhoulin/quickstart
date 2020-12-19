package com.steven.topsail.demo.quickstart.cache;

import com.asiainfo.bits.core.redis.client.RedisClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 多级缓存
 *
 * @author Steven
 * @date 2020-12-12
 */
@Slf4j
public class MultiSpeedCache extends AbstractValueAdaptingCache {

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

    /**
     * 本地缓存，采用共享模式
     */
    private com.github.benmanes.caffeine.cache.Cache<String, Object> localCache;

    /**
     * Redis缓存的最大过期时间，28天
     */
    private static final long EXPIRATION_TIMEOUT = 86400 * 28;

    public MultiSpeedCache(String moduleName,
                           String name,
                           String version,
                           RedisClient pubRedisClient,
                           com.github.benmanes.caffeine.cache.Cache<String, Object> localCache) {
        super(true);
        this.moduleName = moduleName;
        this.name = name;
        this.version = version;
        this.pubRedisClient = pubRedisClient;
        this.localCache = localCache;
    }

    /**
     * Return the cache name.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Return the underlying native cache provider.
     */
    @Override
    public Object getNativeCache() {
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
        Object value = localCache.getIfPresent(cacheKey);
        log.info("lookup {} -> {}", cacheKey, value);
        return value;
    }

    @Override
    @Nullable
    public ValueWrapper get(Object key) {
        String cacheKey = createCacheKey(key);
        Object value = lookup(cacheKey);

        log.info("get {}", cacheKey);

        if (value != null) {
            log.info("local hit: {}", cacheKey);
            return toValueWrapper(value);
        }

        // 从Redis中查找
        try {
            value = pubRedisClient.getObject(cacheKey);
        } catch (RuntimeException e) {
            log.error("Redis获取缓存异常", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Redis 产生异常也需要将值写入 Caffeine 中并返回
            if (null != value) {
                log.info("remote hit: {} -> {}", cacheKey, value);
                localCache.put(cacheKey, value);
            }
        }

        return toValueWrapper(value);
    }

    /**
     * Return the value to which this cache maps the specified key, obtaining
     * that value from {@code valueLoader} if necessary. This method provides
     * a simple substitute for the conventional "if cached, return; otherwise
     * create, cache and return" pattern.
     * <p>If possible, implementations should ensure that the loading operation
     * is synchronized so that the specified {@code valueLoader} is only called
     * once in case of concurrent access on the same key.
     * <p>If the {@code valueLoader} throws an exception, it is wrapped in
     * a {@link ValueRetrievalException}
     *
     * @param key         the key whose associated value is to be returned
     * @param valueLoader
     * @return the value to which this cache maps the specified key
     * @throws ValueRetrievalException if the {@code valueLoader} throws an exception
     * @see #get(Object)
     * @since 4.3
     */
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        throw new RuntimeException("get(Object key, Callable<T> valueLoader)");
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
        log.info("put {} -> {}", cacheKey, value);
        localCache.put(cacheKey, value);
        pubRedisClient.set(cacheKey, value, EXPIRATION_TIMEOUT);
    }

    /**
     * Atomically associate the specified value with the specified key in this cache
     * if it is not set already.
     * <p>This is equivalent to:
     * <pre><code>
     * ValueWrapper existingValue = cache.get(key);
     * if (existingValue == null) {
     *     cache.put(key, value);
     * }
     * return existingValue;
     * </code></pre>
     * except that the action is performed atomically. While all out-of-the-box
     * {@link CacheManager} implementations are able to perform the put atomically,
     * the operation may also be implemented in two steps, e.g. with a check for
     * presence and a subsequent put, in a non-atomic way. Check the documentation
     * of the native cache implementation that you are using for more details.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the value to which this cache maps the specified key (which may be
     * {@code null} itself), or also {@code null} if the cache did not contain any
     * mapping for that key prior to this call. Returning {@code null} is therefore
     * an indicator that the given {@code value} has been associated with the key.
     * @see #put(Object, Object)
     * @since 4.1
     */
    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        String cacheKey = createCacheKey(key);
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
    }

    /**
     * Evict the mapping for this key from this cache if it is present.
     *
     * @param key the key whose mapping is to be removed from the cache
     */
    @Override
    public void evict(Object key) {
        this.version = DateFormatUtils.format(System.currentTimeMillis(), "ddHHmm");
    }

    /**
     * Clear the cache through removing all mappings.
     */
    @Override
    public void clear() {
        this.version = DateFormatUtils.format(System.currentTimeMillis(), "ddHHmm");
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

}
