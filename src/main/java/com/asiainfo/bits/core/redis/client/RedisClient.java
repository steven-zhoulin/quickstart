package com.asiainfo.bits.core.redis.client;

import com.asiainfo.bits.core.redis.Command;
import com.asiainfo.bits.core.redis.RedisAddress;
import com.asiainfo.bits.core.redis.util.RedisIOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Copyright: Copyright (c) 2013 Asiainfo
 *
 * @className: RedisClient
 * @description: Redis客户端
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-8-5
 */
@Slf4j
public class RedisClient extends BinaryRedisClient {

    public RedisClient(RedisAddress[] address, int poolSize, int heartbeatSecond, boolean useNIO, String password, boolean isDistribute) {
        super(address, poolSize, heartbeatSecond, useNIO, password, isDistribute);
    }

    /******************************************************************
     *                          Keys相关指令
     *****************************************************************/

    /**
     * 字符串数组转字节数组
     */
    private static final byte[][] strArray2byteArray(String[] strArray) {
        byte[][] byteArray = new byte[strArray.length][];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = strArray[i].getBytes();
        }
        return byteArray;
    }

    /**
     * 删除一个或多个KEY
     *
     * @param keys
     * @return 被删除KEY的个数
     */
    public long del(String... keys) {

        if (null == keys || 0 == keys.length) {
            throw new IllegalArgumentException("keys不可为空!");
        }

        if (keys.length > 1) {
            assertNotSharding();
        }

        byte[][] args = strArray2byteArray(keys);
        Long n = (Long) cliSendCommand(Command.DEL, args);
        if (null == n) {
            return 0;
        }

        return n;
    }

    /**
     * 查找所有满足指定模式的key集合
     *
     * @param pattern KEY的模式串
     * @return
     */
    public Set<String> keys(String pattern) {

        assertNotSharding();

        if (null == pattern) {
            throw new IllegalArgumentException("pattern不可为空!");
        }

        byte[][] keys = (byte[][]) cliSendCommand(Command.KEYS, pattern.getBytes());
        if (null == keys) {
            return null;
        }

        Set<String> rtn = new HashSet<>(keys.length);
        for (byte[] key : keys) {
            rtn.add(new String(key));
        }
        return rtn;
    }

    /**
     * 判断一个key是否存在
     *
     * @param key
     * @return
     */
    public boolean exists(String key) {
        Assert.notNull(key, "key不可为空!");

        Long n = (Long) cliSendCommand(Command.EXISTS, key.getBytes());
        if (null == n) {
            return false;
        }

        return n == 1L ? true : false;
    }

    /**
     * 设置一个key，多少秒钟后超时
     *
     * @param key
     * @param secTTL
     * @return
     */
    public boolean expire(String key, int secTTL) {
        Assert.notNull(key, "key不可为空!");

        Long n = (Long) cliSendCommand(Command.EXPIRE, key.getBytes(), RedisIOUtil.encode(secTTL));
        if (null == n) {
            return false;
        }
        return n == 1L ? true : false;
    }

    /**
     * 返回过期时间
     *
     * @param key
     * @return
     */
    public long getExpire(String key) {
        Assert.notNull(key, "key不可为空!");
        Long n = (Long) cliSendCommand(Command.TTL, key.getBytes());
        if (null == n) {
            return 0;
        }
        return n;
    }

    /******************************************************************
     *                        Strings相关指令
     *****************************************************************/

    /**
     * 往指定key对应的value里追加数据
     *
     * @param key   KEY
     * @param value 追加数据
     * @return 追加后，key对应value的长度。
     */
    public long append(String key, String value) {

        Assert.notNull(key, "key不可为空!");
        Assert.notNull(value, "value不可为空!");

        Long rtn = (Long) cliSendCommand(Command.APPEND, key.getBytes(), value.getBytes());
        return null == rtn ? -1 : rtn;
    }

    /**
     * 设置一对KV值，V为String
     *
     * @param key
     * @param value
     * @return 成功true，失败false
     */
    public boolean set(String key, String value) {

        Assert.notNull(key, "key不可为空!");

        byte[] valueBytes;
        if (null == value) {
            valueBytes = MAGIC_STR_BYTES;
        } else {
            valueBytes = value.getBytes();
        }

        byte[] rtn = (byte[]) cliSendCommand(Command.SET, key.getBytes(), valueBytes);
        return Arrays.equals(rtn, REDIS_REPLAY_OK);
    }

    /**
     * 设置一对KV值, V为Object
     *
     * @param key
     * @param value
     * @return 成功true，失败false
     */
    public boolean set(String key, Object value) {

        Assert.notNull(key, "key不可为空!");

        byte[] valueBytes;
        if (null == value) {
            valueBytes = MAGIC_STR_BYTES;
        } else {
            valueBytes = SERIALIZER.encode(value);
        }

        byte[] rtn = (byte[]) cliSendCommand(Command.SET, key.getBytes(), valueBytes);
        return Arrays.equals(rtn, REDIS_REPLAY_OK);
    }

    /**
     * 当K不存在时，设置一对KV值
     *
     * @param key
     * @param value
     * @return
     */
    public boolean setnx(String key, String value) {

        Assert.notNull(key, "key不可为空!");
        byte[] valueBytes;
        if (null == value) {
            valueBytes = MAGIC_STR_BYTES;
        } else {
            valueBytes = value.getBytes();
        }

        Long n = (Long) cliSendCommand(Command.SETNX, key.getBytes(), valueBytes);
        if (null == n) {
            return false;
        }
        return n == 1L ? true : false;
    }

    /**
     * 当K不存在时，设置一对KV值
     *
     * @param key
     * @param value
     * @return
     */
    public boolean setnx(String key, Object value) {

        Assert.notNull(key, "key不可为空!");
        byte[] valueBytes;
        if (null == value) {
            valueBytes = MAGIC_STR_BYTES;
        } else {
            valueBytes = SERIALIZER.encode(value);
        }

        Long n = (Long) cliSendCommand(Command.SETNX, key.getBytes(), valueBytes);
        if (null == n) {
            return false;
        }
        return n == 1L ? true : false;
    }

    /**
     * 设置一对KV值,带超时秒数
     * (注: 需要2.6.13才支持)
     *
     * @param key
     * @param value
     * @param secTTL
     * @return 成功true，失败false
     */
    public boolean set(String key, String value, long secTTL) {

        Assert.notNull(key, "key不可为空!");
        byte[] valueBytes;
        if (null == value) {
            valueBytes = MAGIC_STR_BYTES;
        } else {
            valueBytes = value.getBytes();
        }

        byte[] rtn = (byte[]) cliSendCommand(Command.SET, key.getBytes(), valueBytes, REDIS_CMD_EX, RedisIOUtil.encode(secTTL));
        return Arrays.equals(rtn, REDIS_REPLAY_OK);
    }

    /**
     * 设置一对KV值,带超时秒数
     * (注: 需要2.6.13才支持)
     *
     * @param key
     * @param value
     * @param secTTL
     * @return 成功true，失败false
     */
    public boolean set(String key, Object value, long secTTL) {

        Assert.notNull(key, "key不可为空!");
        byte[] valueBytes;
        if (null == value) {
            valueBytes = MAGIC_STR_BYTES;
        } else {
            valueBytes = SERIALIZER.encode(value);
        }

        byte[] rtn = (byte[]) cliSendCommand(Command.SET, key.getBytes(), valueBytes, REDIS_CMD_EX, RedisIOUtil.encode(secTTL));
        return Arrays.equals(rtn, REDIS_REPLAY_OK);
    }

    /**
     * 根据指定的KEY，获取对应的VALUE，VALUE为String
     *
     * @param key
     * @return
     */
    public String get(String key) {

        Assert.notNull(key, "key不可为空!");

        byte[] rtn = (byte[]) cliSendCommand(Command.GET, key.getBytes());
        if (null == rtn) {
            return null;
        } else {
            if (Arrays.equals(MAGIC_STR_BYTES, rtn)) {
                return null;
            } else {
                return new String(rtn);
            }

        }

    }

    /**
     * 根据指定的KEY，获取对应的VALUE, VALUE为Object
     *
     * @param key
     * @return
     */
    public Object getObject(String key) {

        Assert.notNull(key, "key不可为空!");

        byte[] rtn = (byte[]) cliSendCommand(Command.GET, key.getBytes());

        if (null == rtn) {
            return null;
        } else {

            if (Arrays.equals(MAGIC_STR_BYTES, rtn)) {
                return null;
            } else {
                return SERIALIZER.decode(rtn);
            }

        }

    }

    /**
     * 获取指定KEY，对应VALUE的长度
     *
     * @param key
     * @return
     */
    public long strlen(String key) {

        Assert.notNull(key, "key不可为空!");

        Long n = (Long) cliSendCommand(Command.STRLEN, key.getBytes());
        if (null == n) {
            return 0;
        }

        return n;
    }

    /**
     * 按步长1，递增
     *
     * @param key
     * @return
     */
    public long incr(String key) {

        Assert.notNull(key, "key不可为空!");

        Long n = (Long) cliSendCommand(Command.INCR, key.getBytes());
        if (null == n) {
            return 0;
        }

        return n;
    }

    /**
     * 按指定步长，递增
     *
     * @param key
     * @param increment
     * @return
     */
    public long incrby(String key, int increment) {

        Assert.notNull(key, "key不可为空!");

        Long n = (Long) cliSendCommand(Command.INCRBY, key.getBytes(), RedisIOUtil.encode(increment));
        if (null == n) {
            return 0;
        }

        return n;
    }

    /**
     * 按步长-1,递减
     *
     * @param key
     * @return
     */
    public long decr(String key) {
        Assert.notNull(key, "key不可为空!");

        Long n = (Long) cliSendCommand(Command.DECR, key.getBytes());
        if (null == n) {
            return 0;
        }

        return n;
    }

    /**
     * 按指定步长，递减
     *
     * @param key
     * @param decrement
     * @return
     */
    public long decrby(String key, int decrement) {
        Assert.notNull(key, "key不可为空!");

        Long n = (Long) cliSendCommand(Command.DECRBY, key.getBytes(), RedisIOUtil.encode(decrement));
        if (null == n) {
            return 0;
        }

        return n;
    }


    /**
     * 设置或清除字符串指定位置上bit位
     *
     * @param key
     * @param offset 偏移
     * @param b      true: 设置, false: 清除
     * @return 指定偏移量原来存储的位
     */
    public boolean setbit(String key, int offset, boolean b) {
        Assert.notNull(key, "key不可为空!");

        if (offset < 0) {
            throw new IllegalArgumentException("offset值必须>=0");
        }

        long value = b ? 1 : 0;
        Long n = (Long) cliSendCommand(Command.SETBIT, key.getBytes(), RedisIOUtil.encode(offset), RedisIOUtil.encode(value));
        return n == 0 ? false : true;
    }

    /**
     * 对 key所储存的字符串值，获取指定偏移量上的位(bit)，当 offset比字符串值的长度大，或者 key不存在时，返回false
     *
     * @param key
     * @param offset
     * @return 字符串值指定偏移量上的位(bit)，为1返回true，为0返回false
     */
    public boolean getbit(String key, int offset) {

        Assert.notNull(key, "key不可为空!");

        if (offset < 0) {
            throw new IllegalArgumentException("offset值必须>=0");
        }

        Long n = (Long) cliSendCommand(Command.GETBIT, key.getBytes(), RedisIOUtil.encode(offset));
        return n == 0 ? false : true;
    }

    /**
     * 计算给定字符串中，被设置为true的比特位的数量，通过指定额外的start或end参数，可以只在特定的位上进行。
     *
     * @param key
     * @param start 按字节进行偏移
     * @param end   按字节进行偏移
     * @return
     */
    public long bitcount(String key, int start, int end) {
        Assert.notNull(key, "key不可为空!");

        if (start < 0) {
            throw new IllegalArgumentException("start值必须>=0");
        }

        Long n = (Long) cliSendCommand(Command.BITCOUNT, key.getBytes(), RedisIOUtil.encode(start), RedisIOUtil.encode(end));
        return n;
    }

    /******************************************************************
     *                        Hashs相关指令
     *****************************************************************/

    /**
     * 删除一个或多个hash field
     *
     * @param key
     * @param fields
     * @return
     */
    public long hdel(String key, String... fields) {
        Assert.notNull(key, "key不可为空!");

        if (null == fields) {
            throw new IllegalArgumentException("fields不可为空!");
        }

        byte[][] args = new byte[fields.length + 1][];
        args[0] = key.getBytes();

        byte[][] tmp = strArray2byteArray(fields);
        System.arraycopy(tmp, 0, args, 1, tmp.length);
        Long rtn = (Long) cliSendCommand(Command.HDEL, args);
        return null == rtn ? 0 : rtn;
    }

    /**
     * 按指定步长，递增某个field
     *
     * @param key
     * @param field
     * @return
     */
    public long hincrby(String key, String field, long value) {

        Assert.notNull(key, "key不可为空!");
        Assert.notNull(field, "field不可为空!");

        Long n = (Long) cliSendCommand(Command.HINCRBY, key.getBytes(), field.getBytes(), RedisIOUtil.encode(value));
        if (null == n) {
            return 0;
        }

        return n;
    }


    /**
     * 批量设置K-V
     *
     * @param key
     * @param map
     * @return
     */
    public boolean hmset(String key, Map<String, String> map) {

        Assert.notNull(key, "key不可为空!");
        Assert.notNull(map, "map不可为空!");

        byte[][] args = new byte[map.size() * 2 + 1][];
        args[0] = key.getBytes();
        int i = 1;
        for (String k : map.keySet()) {
            args[i++] = k.getBytes();
            args[i++] = map.get(k).getBytes();
        }

        byte[] rtn = (byte[]) cliSendCommand(Command.HMSET, args);
        return Arrays.equals(rtn, REDIS_REPLAY_OK);
    }

    /**
     * 批量获取K-V
     *
     * @param key
     * @param fields
     * @return
     */
    public Map<String, String> hmget(String key, String... fields) {

        Assert.notNull(key, "key不可为空!");
        Assert.notNull(fields, "fields不可为空!");

        Map<String, String> rtn = new HashMap<String, String>();

        byte[][] args = new byte[fields.length + 1][];
        args[0] = key.getBytes();
        byte[][] tmp = strArray2byteArray(fields);
        System.arraycopy(tmp, 0, args, 1, tmp.length);
        byte[][] values = (byte[][]) cliSendCommand(Command.HMGET, args);

        if (null == values) {
            return rtn;
        }

        int i = 0;
        for (String field : fields) {
            byte[] curr = values[i++];
            if (null == curr) {
                continue;
            }

            String value = new String(curr);
            rtn.put(field, value);

        }
        return rtn;
    }

    /**
     * 获取hash中所有的value
     *
     * @param key
     * @return
     */
    public Set<String> hvals(String key) {

        Assert.notNull(key, "key不可为空!");

        Set<String> rtn = new HashSet<String>();
        byte[][] values = (byte[][]) cliSendCommand(Command.HVALS, key.getBytes());
        for (byte[] value : values) {
            String v = new String(value);
            rtn.add(v);
        }
        return rtn;
    }

    /**
     * 判断hash中是否存在指定的field
     *
     * @param key
     * @param field
     * @return
     */
    public boolean hexists(String key, String field) {

        Assert.notNull(key, "key不可为空!");
        Assert.notNull(field, "fields不可为空!");

        Long n = (Long) cliSendCommand(Command.HEXISTS, key.getBytes(), field.getBytes());
        if (null == n) {
            return false;
        }
        return n == 1L ? true : false;
    }

    /**
     * 获取hash中field对应的value
     *
     * @param key
     * @param field
     * @return
     */
    public String hget(String key, String field) {

        Assert.notNull(key, "key不可为空!");
        Assert.notNull(field, "field不可为空!");

        byte[] rtn = (byte[]) cliSendCommand(Command.HGET, key.getBytes(), field.getBytes());
        if (null == rtn) {
            return null;
        } else {
            if (Arrays.equals(MAGIC_STR_BYTES, rtn)) {
                return null;
            } else {
                return new String(rtn);
            }
        }
    }

    /**
     * 获取hash中所有的key
     *
     * @param key
     * @return
     */
    public Set<String> hkeys(String key) {

        Assert.notNull(key, "key不可为空!");

        Set<String> rtn = new HashSet<String>();
        byte[][] values = (byte[][]) cliSendCommand(Command.HKEYS, key.getBytes());
        for (byte[] value : values) {
            String v = new String(value);
            rtn.add(v);
        }
        return rtn;
    }

    /**
     * 往hash里设置K-V
     *
     * @param key
     * @param field
     * @param value
     * @return 如果hash中不存在filed则返回1，否则返回0
     */
    public long hset(String key, String field, String value) {

        Assert.notNull(key, "key不可为空!");
        Assert.notNull(field, "field不可为空!");

        byte[] valueBytes;
        if (null == value) {
            valueBytes = MAGIC_STR_BYTES;
        } else {
            valueBytes = value.getBytes();
        }

        Long n = (Long) cliSendCommand(Command.HSET, key.getBytes(), field.getBytes(), valueBytes);
        if (null == n) {
            return 0;
        }

        return n;
    }

    /**
     * 获取hash中所有的K-V，屏蔽此函数
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unused")
    public Map<String, String> hgetAll(String key) {

        Assert.notNull(key, "key不可为空!");

        Map<String, String> rtn = new HashMap<String, String>();
        byte[][] entry = (byte[][]) cliSendCommand(Command.HGETALL, key.getBytes());

        if (null == entry) {
            return rtn;
        }

        for (int i = 0, size = (entry.length - 1); i < size; i += 2) {
            String k = new String(entry[i]);
            String v = new String(entry[i + 1]);
            rtn.put(k, v);
        }

        return rtn;
    }

    /**
     * 返回hash中K-V对数量
     *
     * @param key
     * @return
     */
    public long hlen(String key) {

        Assert.notNull(key, "key不可为空!");

        Long n = (Long) cliSendCommand(Command.HLEN, key.getBytes());
        if (null == n) {
            return 0;
        }

        return n;
    }

    /**
     * 当field不存在时，设置field-value对
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public long hsetnx(String key, String field, String value) {

        Assert.notNull(key, "key不可为空!");
        Assert.notNull(field, "field不可为空!");
        byte[] valueBytes;
        if (null == value) {
            valueBytes = MAGIC_STR_BYTES;
        } else {
            valueBytes = value.getBytes();
        }

        Long n = (Long) cliSendCommand(Command.HSETNX, new byte[][]{key.getBytes(), field.getBytes(), valueBytes});
        if (null == n) {
            return 0;
        }

        return n;
    }

    /******************************************************************
     *                        Lists相关指令
     *****************************************************************/

    /**
     * 返回指定队列的长度
     *
     * @param key 队列名
     * @return
     */
    public long llen(String key) {

        Assert.notNull(key, "key不可为空!");

        Long n = (Long) cliSendCommand(Command.LLEN, key.getBytes());
        if (null == n) {
            return 0;
        }

        return n;
    }

    /**
     * 从队列左边弹出一个数据，如果队列为空，则返回null
     *
     * @return
     */
    public String lpop(String key) {
        Assert.notNull(key, "key不可为空!");
        byte[] rtn = (byte[]) cliSendCommand(Command.LPOP, key.getBytes());
        return null == rtn ? null : new String(rtn);
    }

    /**
     * 从队列右边弹出一个数据，如果队列为空，则返回null
     *
     * @return
     */
    public String rpop(String key) {
        Assert.notNull(key, "key不可为空!");
        byte[] rtn = (byte[]) cliSendCommand(Command.RPOP, key.getBytes());
        return null == rtn ? null : new String(rtn);
    }

    /**
     * 从队列左边弹出一个数据，如果队列为空，则阻塞secTTL秒
     *
     * @param secTTL 多少秒后超时，传0表示不超时
     * @param keys   队列名
     * @return
     */
    public String[] blpop(long secTTL, String... keys) {

        Assert.notNull(keys, "keys不可为空!");

        if (keys.length > 1) {
            assertNotSharding();
        }

        byte[][] args = new byte[keys.length + 1][];
        System.arraycopy(keys, 0, args, 0, keys.length);
        args[keys.length] = RedisIOUtil.encode(secTTL);
        byte[][] rtn = (byte[][]) cliSendCommand(Command.BLPOP, args);
        return new String[]{new String(rtn[0]), new String(rtn[1])};
    }

    /**
     * 从队列右边弹出一个数据，如果队列为空，则阻塞secTTL秒
     *
     * @param secTTL 多少秒后超时，传0表示不超时
     * @param keys   队列名
     * @return
     */
    public String[] brpop(long secTTL, String... keys) {

        Assert.notNull(keys, "keys不可为空!");

        if (keys.length > 1) {
            assertNotSharding();
        }

        byte[][] args = new byte[keys.length + 1][];
        System.arraycopy(keys, 0, args, 0, keys.length);
        args[keys.length] = RedisIOUtil.encode(secTTL);
        byte[][] rtn = (byte[][]) cliSendCommand(Command.BRPOP, args);
        return new String[]{new String(rtn[0]), new String(rtn[1])};
    }

    /**
     * 往队列左端追加数据
     *
     * @param key    队列名
     * @param values 数据
     * @return 返回追加数据后的队列长度
     */
    public long lpush(String key, String... values) {

        Assert.notNull(key, "key不可为空!");
        Assert.notNull(values, "values不可为空!");

        byte[][] args = new byte[values.length + 1][];
        args[0] = key.getBytes();

        byte[][] tmp = strArray2byteArray(values);
        System.arraycopy(tmp, 0, args, 1, tmp.length);

        Long n = (Long) cliSendCommand(Command.LPUSH, args);
        if (null == n) {
            return 0;
        }

        return n;
    }

    /**
     * 往队列右端追加数据
     *
     * @param key    队列名
     * @param values 数据
     * @return 返回追加数据后的队列长度
     */
    public long rpush(String key, String... values) {

        Assert.notNull(key, "key不可为空!");
        Assert.notNull(values, "values不可为空!");

        byte[][] args = new byte[values.length + 1][];
        args[0] = key.getBytes();
        byte[][] tmp = strArray2byteArray(values);
        System.arraycopy(tmp, 0, args, 1, tmp.length);

        Long n = (Long) cliSendCommand(Command.RPUSH, args);
        if (null == n) {
            return 0;
        }

        return n;
    }

    /**
     * 当队列已经存在时，往队列左端追加数据
     *
     * @param key
     * @param value
     * @return
     */
    public long lpushx(String key, String value) {

        Assert.notNull(key, "key不可为空!");
        Assert.notNull(value, "value不可为空!");

        Long n = (Long) cliSendCommand(Command.LPUSHX, key.getBytes(), value.getBytes());
        if (null == n) {
            return 0;
        }

        return n;
    }

    /**
     * 当队列已经存在时，往队列右端追加数据
     *
     * @param key
     * @param value
     * @return
     */
    public long rpushx(String key, String value) {

        Assert.notNull(key, "key不可为空!");
        Assert.notNull(value, "value不可为空!");

        Long rtn = (Long) cliSendCommand(Command.RPUSHX, key.getBytes(), value.getBytes());
        return null == rtn ? 0 : rtn;
    }

    /**
     * 从队列中，获取指定开始与结束位置之间的所有数据。
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public String[] lrange(String key, int start, int end) {

        Assert.notNull(key, "key不可为空!");

        byte[] startByte = RedisIOUtil.encode(start);
        byte[] endByte = RedisIOUtil.encode(end);
        byte[][] bytes = (byte[][]) cliSendCommand(Command.LRANGE, key.getBytes(), startByte, endByte);
        String[] rtn = new String[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            rtn[i] = new String(bytes[i]);
        }
        return rtn;
    }

    /**
     * 获取队列中指定位置的元素
     *
     * @param key
     * @param index
     * @return
     */
    public String lindex(String key, int index) {
        Assert.notNull(key, "key不可为空!");
        byte[] rtn = (byte[]) cliSendCommand(Command.LINDEX, key.getBytes(), RedisIOUtil.encode(index));
        return null == rtn ? null : new String(rtn);
    }

    /**
     * 往队列指定位置设置数据
     *
     * @param key
     * @param index
     * @param value
     * @return
     */
    public boolean lset(String key, int index, String value) {
        Assert.notNull(key, "key不可为空!");
        Assert.notNull(value, "value不可为空!");

        byte[] rtn = (byte[]) cliSendCommand(Command.LSET, key.getBytes(), RedisIOUtil.encode(index), value.getBytes());
        return Arrays.equals(rtn, REDIS_REPLAY_OK);
    }

    /******************************************************************
     *                          Sets相关指令
     *****************************************************************/

    /**
     * 向集合中添加一个元素
     *
     * @param key
     * @param member
     * @return
     */
    public long sadd(String key, String... member) {

        Assert.notNull(key, "key不可为空!");
        Assert.notNull(member, "member不可为空!");

        byte[][] args = new byte[member.length + 1][];
        args[0] = key.getBytes();

        byte[][] tmp = strArray2byteArray(member);
        System.arraycopy(tmp, 0, args, 1, tmp.length);

        Long rtn = (Long) cliSendCommand(Command.SADD, args);
        return null == rtn ? 0 : rtn;
    }

    /**
     * 获取集合中所有元素
     *
     * @param key
     * @return
     */
    public Set<String> smembers(String key) {
        Assert.notNull(key, "key不可为空!");

        byte[][] members = (byte[][]) cliSendCommand(Command.SMEMBERS, key.getBytes());
        if (null == members) {
            return null;
        }

        Set<String> rtn = new HashSet<String>(members.length);
        for (byte[] member : members) {
            rtn.add(new String(member));
        }

        return rtn;
    }

    /**
     * 从集合中删除一个元素
     *
     * @param key
     * @param member
     * @return
     */
    public long srem(String key, String member) {
        Assert.notNull(key, "key不可为空!");
        Assert.notNull(member, "member不可为空!");

        Long rtn = (Long) cliSendCommand(Command.SREM, key.getBytes(), member.getBytes());
        return null == rtn ? 0 : rtn;
    }

    /**
     * 随机从集合中弹出一个元素
     *
     * @param key
     * @return
     */
    public String spop(String key) {
        Assert.notNull(key, "key不可为空!");
        byte[] rtn = (byte[]) cliSendCommand(Command.SPOP, key.getBytes());
        return null == rtn ? null : new String(rtn);
    }

    /**
     * 将元素从一个集合移动到另一个集合
     *
     * @param srckey
     * @param dstkey
     * @param member
     * @return
     */
    public long smove(String srckey, String dstkey, String member) {

        assertNotSharding();

        Assert.notNull(srckey, "srckey不可为空!");
        Assert.notNull(dstkey, "dstkey不可为空!");
        Assert.notNull(member, "member不可为空!");

        Long rtn = (Long) cliSendCommand(Command.SMOVE, srckey.getBytes(), dstkey.getBytes(), member.getBytes());
        return null == rtn ? 0 : rtn;
    }

    /**
     * 获取集合中元素个数
     *
     * @param key
     * @return
     */
    public long scard(String key) {

        Assert.notNull(key, "key不可为空!");

        Long rtn = (Long) cliSendCommand(Command.SCARD, key.getBytes());
        return null == rtn ? 0 : rtn;
    }

    /**
     * 判断元素是否存在于集合中
     *
     * @param key
     * @param member
     * @return
     */
    public boolean sismember(String key, String member) {
        Assert.notNull(key, "key不可为空!");
        Assert.notNull(member, "member不可为空!");

        Long rtn = (Long) cliSendCommand(Command.SISMEMBER, key.getBytes(), member.getBytes());
        if (null == rtn) {
            return false;
        }

        return 1L == rtn ? true : false;
    }

    /**
     * 计算两个集合的交集
     *
     * @param keys
     * @return
     */
    public Set<String> sinter(String[] keys) {

        assertNotSharding();
        if (null == keys || keys.length < 2) {
            throw new IllegalArgumentException("keys不可为空!");
        }

        byte[][] args = strArray2byteArray(keys);

        byte[][] members = (byte[][]) cliSendCommand(Command.SINTER, args);
        Set<String> rtn = new HashSet<String>(members.length);
        for (byte[] member : members) {
            rtn.add(new String(member));
        }

        return rtn;
    }

    /**
     * 计算两个集合的交集，并存进另一个集合中
     *
     * @param dstkey
     * @param keys
     * @return
     */
    public long sinterstore(String dstkey, String[] keys) {

        assertNotSharding();

        Assert.notNull(dstkey, "dstkey不可为空!");

        if (null == keys || keys.length < 2) {
            throw new IllegalArgumentException("keys不可为空!");
        }

        byte[][] tmp = strArray2byteArray(keys);

        byte[][] args = new byte[tmp.length + 1][];
        args[0] = dstkey.getBytes();
        System.arraycopy(tmp, 0, args, 1, tmp.length);

        Long rtn = (Long) cliSendCommand(Command.SINTERSTORE, args);
        return null == rtn ? 0 : rtn;
    }

    /**
     * 计算两个集合的并集
     *
     * @param keys
     * @return
     */
    public Set<String> sunion(String[] keys) {

        assertNotSharding();
        if (null == keys || keys.length < 2) {
            throw new IllegalArgumentException("keys不可为空!");
        }

        byte[][] args = strArray2byteArray(keys);
        byte[][] members = (byte[][]) cliSendCommand(Command.SUNION, args);
        Set<String> rtn = new HashSet<String>(members.length);
        for (byte[] member : members) {
            rtn.add(new String(member));
        }

        return rtn;
    }

    /**
     * 计算两个集合的并集，并存进另一个集合中
     *
     * @param dstkey
     * @param keys
     * @return
     */
    public long sunionstore(String dstkey, String[] keys) {

        assertNotSharding();

        Assert.notNull(dstkey, "dstkey不可为空!");

        if (null == keys || keys.length < 2) {
            throw new IllegalArgumentException("keys不可为空!");
        }

        byte[][] tmp = strArray2byteArray(keys);

        byte[][] args = new byte[tmp.length + 1][];
        args[0] = dstkey.getBytes();
        System.arraycopy(tmp, 0, args, 1, tmp.length);

        Long rtn = (Long) cliSendCommand(Command.SUNIONSTORE, args);
        return null == rtn ? 0 : rtn;
    }

    /**
     * 计算两个集合的差集
     *
     * @param keys
     * @return
     */
    public Set<String> sdiff(String[] keys) {

        assertNotSharding();
        if (null == keys || keys.length < 2) {
            throw new IllegalArgumentException("keys不可为空!");
        }

        byte[][] args = strArray2byteArray(keys);

        byte[][] members = (byte[][]) cliSendCommand(Command.SDIFF, args);
        Set<String> rtn = new HashSet<String>(members.length);
        for (byte[] member : members) {
            rtn.add(new String(member));
        }

        return rtn;
    }

    /**
     * 计算两个集合的差集，并存储进另一个集合
     *
     * @param dstkey
     * @param keys
     * @return
     */
    public long sdiffstore(String dstkey, String[] keys) {

        assertNotSharding();
        Assert.notNull(dstkey, "dstkey不可为空!");

        if (null == keys || keys.length < 2) {
            throw new IllegalArgumentException("keys不可为空!");
        }

        byte[][] tmp = strArray2byteArray(keys);

        byte[][] args = new byte[keys.length + 1][];
        args[0] = dstkey.getBytes();
        System.arraycopy(tmp, 0, args, 1, tmp.length);

        Long rtn = (Long) cliSendCommand(Command.SDIFFSTORE, args);
        return null == rtn ? 0 : rtn;
    }

    /**
     * 随机获取集合中的一个元素
     *
     * @param key
     * @return
     */
    public String srandmember(String key) {
        Assert.notNull(key, "key不可为空!");
        byte[] rtn = (byte[]) cliSendCommand(Command.SRANDMEMBER, key.getBytes());
        return null == rtn ? null : new String(rtn);
    }

}
