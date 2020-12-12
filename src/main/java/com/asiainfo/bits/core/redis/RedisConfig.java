package com.asiainfo.bits.core.redis;

import com.asiainfo.bits.core.redis.client.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.TreeSet;

/**
 * bits.redis.enable=true
 * bits.redis.cluster.names=sna,sec,common
 * <p>
 * bits.redis.cluster.sna.mode=client
 * bits.redis.cluster.sna.useNIO=false
 * bits.redis.cluster.sna.password=1q1w1e1r
 * bits.redis.cluster.sna.heartbeatInterval=5
 * bits.redis.cluster.sna.poolSize=5
 * bits.redis.cluster.sna.m.address=主1,主2,主3
 * bits.redis.cluster.sna.s.address=备1,备2,备3
 *
 * @author Steven
 * @date 2020-11-23
 */
@ConditionalOnProperty(prefix = "bits.redis", name = "enable", havingValue = "true")
@Configuration
@Slf4j
public class RedisConfig {

    private static final String PREFIX = "bits.redis.cluster.";

    @Value("#{'${bits.redis.cluster.names:}'.split(',')}")
    private List<String> clusterNames;

    @Autowired
    private Environment env;

    @Bean("snaRedisClient")
    public RedisClient snaRedisClient(RedisFactory redisFactory) {
        return RedisFactory.getRedisClient("sna");
    }

    @Bean("secRedisClient")
    public RedisClient secRedisClient(RedisFactory redisFactory) {
        return RedisFactory.getRedisClient("sec");
    }

    @Bean("pubRedisClient")
    public RedisClient pubRedisClient(RedisFactory redisFactory) {
        return RedisFactory.getRedisClient("pub");
    }

    @Bean
    public RedisFactory redisFactory() {

        RedisFactory redisFactory = new RedisFactory();

        log.info("bits.redis.cluster.names: {}", clusterNames);

        for (String clusterName : clusterNames) {

            String mode = env.getProperty(PREFIX + clusterName + ".mode", "client");
            String useNIO = env.getProperty(PREFIX + clusterName + ".useNIO", "false");
            String password = env.getProperty(PREFIX + clusterName + ".password", "");
            String heartbeatInterval = env.getProperty(PREFIX + clusterName + ".heartbeatInterval", "5");
            String poolSize = env.getProperty(PREFIX + clusterName + ".poolSize", "3");

            String mAddressString = env.getProperty(PREFIX + clusterName + ".m.address", "");
            String sAddressString = env.getProperty(PREFIX + clusterName + ".s.address", "");

            /** 连接池大小 */
            int iPoolSize = Integer.parseInt(poolSize);

            /** 心跳间隔（单位：秒） */
            int iHeartbeatInterval = Integer.parseInt(heartbeatInterval);

            /** 是否启用 NIO */
            boolean isUseNIO = Boolean.parseBoolean(useNIO);

            /** 是否采用服务端集群模式 */
            boolean isDistribute = "server".equals(mode) ? true : false;

            TreeSet<RedisAddress> redisAddressSet = parse(mAddressString, sAddressString);
            check(clusterName, redisAddressSet);

            RedisAddress[] address = redisAddressSet.toArray(new RedisAddress[0]);
            RedisClient client = new RedisClient(address, iPoolSize, iHeartbeatInterval, isUseNIO, password, isDistribute);

            log.info("------ redis 连接池初始化 ------");
            log.info("分组组名: " + clusterName);
            log.info("集群模式: " + (isDistribute ? "server" : "client"));
            log.info("需要认证: " + (StringUtils.isNotBlank(password) ? "true" : "false"));
            log.info("地址集合:");
            for (RedisAddress addr : address) {
                if (null != addr.getSlave()) {
                    log.info("  master " + addr.getMaster() + " -> slave " + addr.getSlave());
                } else {
                    log.info("  " + addr.getMaster());
                }
            }
            log.info("连接数量: " + poolSize);
            log.info("心跳周期: " + iHeartbeatInterval);
            log.info("IO模式: " + (isUseNIO ? "NIO" : "BIO") + "\n");

            RedisFactory.redises.put(clusterName, client);
        }

        return redisFactory;
    }

    /**
     * 解析地址串
     *
     * @param mAddressString 主地址串：主1,主2,主3
     * @param sAddressString 备地址串：备1,备2,备3
     * @return
     */
    private TreeSet<RedisAddress> parse(String mAddressString, String sAddressString) {
        /** 针对主节点进行排序 */
        TreeSet<RedisAddress> addressTreeSet = new TreeSet<>();

        /** 先用 , 做桶切分 */
        String[] mBuckets = StringUtils.split(mAddressString, ',');
        String[] sBuckets = StringUtils.split(sAddressString, ',');

        for (int i = 0; i < mBuckets.length; i++) {
            RedisAddress redisAddress = new RedisAddress();
            redisAddress.setMaster(mBuckets[i]);
            if (i < sBuckets.length) {
                redisAddress.setSlave(sBuckets[i]);
            }

            addressTreeSet.add(redisAddress);
        }

        return addressTreeSet;
    }

    /**
     * 检查主备节点数量是否一致
     *
     * @param clusterName
     * @param redisAddressSet
     */
    private void check(String clusterName, TreeSet<RedisAddress> redisAddressSet) {

        int count = 0;

        for (RedisAddress redisAddress : redisAddressSet) {
            if (null != redisAddress.getSlave()) {
                count++;
            }
        }

        if (0 != count && count != redisAddressSet.size()) {
            log.error("### clusterNames: {}, 主备节点数量不一致！", clusterName);
            for (RedisAddress redisAddress : redisAddressSet) {
                log.error("  master " + redisAddress.getMaster() + " -> slave " + redisAddress.getSlave());
            }
        }

    }

}
