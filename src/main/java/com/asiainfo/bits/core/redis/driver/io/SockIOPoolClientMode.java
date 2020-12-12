package com.asiainfo.bits.core.redis.driver.io;

import com.asiainfo.bits.core.redis.RedisAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @className: SockIOPoolClientMode
 * @description: Redis Sock连接池(基于客户端做集群的模式)
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2018-10-18
 */
@Slf4j
public class SockIOPoolClientMode implements SockIOPool {

    /**
     * 一个Pool可以跨多个redis实例，每个实例是一个SockIOBucket桶，根据key的hash值定位到桶
     */
    private List<SockIOBucket> buckets = new ArrayList<SockIOBucket>();

    /**
     * 挂掉的桶列表
     */
    private List<SockIOBucket> deadBuckets = new ArrayList<SockIOBucket>();

    private RedisAddress[] address;
    private boolean sharding = false;
    private int poolSize;
    private int heartbeatSecond = 5;
    private boolean useNIO = false;
    private String password;

    /**
     * 连接池初始化
     *
     * @param address
     * @param poolSize
     * @param heartbeatSecond
     * @param useNIO
     */
    public SockIOPoolClientMode(RedisAddress[] address, int poolSize, int heartbeatSecond, boolean useNIO, String password) {

        this.address = address;
        this.poolSize = poolSize;
        this.heartbeatSecond = heartbeatSecond;
        this.useNIO = useNIO;
        this.password = password;

        if (this.address.length > 1) {
            this.sharding = true;
        }

        for (RedisAddress addr : address) {

            // 主地址解析
            String[] masterPart = StringUtils.split(addr.getMaster(), ':');
            if (2 != masterPart.length) {
                throw new IllegalArgumentException("redis主地址格式不正确！" + addr.getMaster());
            }

            String masterHost = masterPart[0];
            int masterPort = Integer.parseInt(masterPart[1]);

            // 备地址解析
            String[] slavePart = StringUtils.split(addr.getSlave(), ':');
            if (null != slavePart) {
                if (2 != slavePart.length) {
                    throw new IllegalArgumentException("redis备地址格式不正确！" + addr.getSlave());
                }
            }

            SockIOBucket bucket = null;

            try {
                if (null == addr.getSlave()) {
                    bucket = new SimpleSockIOBucket(masterHost, masterPort, poolSize, useNIO, this.password);
                } else {
                    String slaveHost = slavePart[0];
                    int slavePort = Integer.parseInt(slavePart[1]);
                    bucket = new HASockIOBucket(masterHost, masterPort, slaveHost, slavePort, poolSize, useNIO, this.password);
                }
                buckets.add(bucket);
                bucket.init();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 启动连接池心跳线程
         */
        ClientMaintTask task = new ClientMaintTask();
        task.setDaemon(true);
        task.start();

    }

    /**
     * 根据key获取一个socket连接
     *
     * @param key
     * @return
     */
    @Override
    public ISockIO getSock(String key) {

        int hashCode = key.hashCode();
        int divisor = buckets.size();

        // 当桶全部挂死时
        if (0 == divisor) {
            return null;
        }

        int position = hashCode % divisor;
        position = (position < 0) ? -position : position;

        SockIOBucket bucket = buckets.get(position);
        return bucket.borrowSockIO();

    }

    /**
     * 是否分片
     *
     * @return
     */
    @Override
    public boolean isSharding() {
        return this.sharding;
    }

    /**
     * 连接池后台维护线程
     */
    private class ClientMaintTask extends Thread {

        @Override
        public void run() {

            while (true) {
                try {
                    Thread.sleep(1000 * heartbeatSecond);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 桶心跳检查
                bucketHeartbeat();

                // 桶失败重连
                bucketReconnect();
            }
        }

        /**
         * 桶的心跳检查，当桶中的连接数为0时，将桶标挪至待检查桶列表里
         */
        private void bucketHeartbeat() {
            try {
                Iterator<SockIOBucket> iter = buckets.iterator();
                while (iter.hasNext()) {
                    SockIOBucket bucket = iter.next();

                    int preStateCode = bucket.getStateCode();
                    int curStateCode = bucket.healthCheck();

                    if (SockIOBucket.STATE_ER == curStateCode || SockIOBucket.STATE_ERER == curStateCode) {
                        bucket.close();
                        iter.remove();
                        deadBuckets.add(bucket);
                        log.error("redis桶心跳失败！" + bucket.getAddress());
                    } else if (preStateCode != curStateCode) {
                        // 触发状态变更
                        log.info("桶状态变更: " + SockIOBucket.STATES[preStateCode] + " -> " + SockIOBucket.STATES[curStateCode]);

                        // 1. 回收资源
                        bucket.close();

                        // 2. 设置新状态
                        bucket.setStateCode(curStateCode);

                        // 3. 按新状态申请资源
                        bucket.init();
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        /**
         * 重试心跳失败的桶，重试成功的桶放回原来的位置，并从待检查桶列表里挪走
         */
        private void bucketReconnect() {

            Iterator<SockIOBucket> iter = deadBuckets.iterator();
            while (iter.hasNext()) {
                SockIOBucket bucket = iter.next();
                try {
                    boolean success = bucket.init();
                    if (!success) {
                        continue;
                    }

                    iter.remove();

                    buckets.add(bucket);

                    Collections.sort(buckets);
                    log.info("-------------------------");
                    for (SockIOBucket bkt : buckets) {
                        log.info("-- " + bkt.getAddress());
                    }
                    log.info("-------------------------");
                    log.info("redis桶复活！" + bucket.getAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
