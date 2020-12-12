package com.asiainfo.bits.core.redis.driver.io;

import com.asiainfo.bits.core.redis.ClusterNodesInfo;
import com.asiainfo.bits.core.redis.NodeInfo;
import com.asiainfo.bits.core.redis.RedisAddress;
import com.asiainfo.bits.core.redis.util.RedisUtil;
import com.asiainfo.bits.core.redis.util.SlotUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @className: SockIOPoolClientMode
 * @description: Redis Sock连接池(基于服务端做集群的模式)
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2018-10-18
 */
@Slf4j
public class SockIOPoolServerMode implements SockIOPool {

    /**
     * 一个Pool可以跨多个redis实例，每个实例负责一段槽位范围
     */
    private List<SockIOBucket> buckets = new ArrayList<SockIOBucket>();

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
    public SockIOPoolServerMode(RedisAddress[] address, int poolSize, int heartbeatSecond, boolean useNIO, String password) {

        this.address = address;
        this.poolSize = poolSize;
        this.heartbeatSecond = heartbeatSecond;
        this.useNIO = useNIO;
        this.password = password;

        if (this.address.length > 1) {
            this.sharding = true;
        }

        ServerManitTask task = new ServerManitTask(address);
        task.doTask();
        task.setDaemon(true);
        task.start();

    }

    /**
     * 根据key计算其归属的槽位，并获取一个socket连接
     *
     * @param key
     * @return
     */
    @Override
    public ISockIO getSock(String key) {

        int slotPosition = SlotUtil.getSlot(key);

        for (SockIOBucket bucket : buckets) {
            if (bucket.getSlots().contains(slotPosition)) {
                return bucket.borrowSockIO();
            }
        }

        return null;

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
     * 重新初始化
     *
     * @param masterNodes
     */
    private void reset(Map<String, NodeInfo> masterNodes) {

        List<SockIOBucket> oldBuckets = this.buckets;
        List<SockIOBucket> newBuckets = new ArrayList<SockIOBucket>();

        for (NodeInfo node : masterNodes.values()) {
            SockIOBucket bucket = new SimpleSockIOBucket(node.ip, node.port, this.poolSize, this.useNIO, this.password);
            bucket.setSlots(node.slots);

            try {
                bucket.init();
                newBuckets.add(bucket);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        this.buckets = newBuckets;
        for (SockIOBucket bucket : oldBuckets) {
            bucket.close();
        }
        oldBuckets.clear();

    }

    /**
     * 连接池后台维护线程
     */
    private class ServerManitTask extends Thread {

        private List<String> monitorAddressList = new ArrayList<String>();

        /**
         * 活动的探测地址。
         */
        private String monitorAddress;

        /**
         * 当前集群信息
         */
        private ClusterNodesInfo oldClusterNodesInfo;

        public ServerManitTask(RedisAddress[] address) {

            for (RedisAddress addr : address) {
                monitorAddressList.add(addr.getMaster());

            }

            // 乱序，防止每次都是探测的同一个地址
            Collections.shuffle(this.monitorAddressList);
            this.monitorAddress = this.monitorAddressList.get(0);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // 周期性探测集群信息
                    doTask();

                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    log.error("周期性探测Redis集群信息失败!", e);
                }
            }
        }

        public ClusterNodesInfo getOldClusterNodesInfo() {
            return this.oldClusterNodesInfo;
        }

        public void doTask() {

            ClusterNodesInfo newClusterNodesInfo = loadClusterNodesInfo();

            if (null != newClusterNodesInfo) {

                if (!newClusterNodesInfo.equals(this.oldClusterNodesInfo)) {
                    // 按新配置重新初始化集群。
                    reset(newClusterNodesInfo.masterNodes());
                    this.oldClusterNodesInfo = newClusterNodesInfo;
                }
            }

        }

        /**
         * 加载最新的集群信息
         *
         * @return
         */
        private ClusterNodesInfo loadClusterNodesInfo() {
            try {
                String[] lines = RedisUtil.clusterNodes(this.monitorAddress, password);
                if (null != lines) {
                    ClusterNodesInfo cluster = parse(lines);
                    return cluster;
                } else {
                    for (String addr : this.monitorAddressList) {
                        if (this.monitorAddress.equals(addr)) {
                            continue;
                        }

                        try {
                            lines = RedisUtil.clusterNodes(addr, password);
                            if (null != lines) {
                                ClusterNodesInfo cluster = parse(lines);
                                // 一旦从某个节点获取到响应无需继续，直接返回!
                                this.monitorAddress = addr;
                                return cluster;
                            }
                        } catch (Exception e) {
                            log.error("从 " + addr + " 定期探测集群信息出错, 尝试切换到下一节点重试!", e);
                            continue;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("定期探测集群信息出错!", e);
            }

            return null;
        }

        /**
         * 解析集群信息
         *
         * @param lines
         * @return
         */
        private ClusterNodesInfo parse(String[] lines) {

            ClusterNodesInfo cluster = new ClusterNodesInfo();

            for (String line : lines) {

                String[] parts = StringUtils.split(line);
                if (null == parts) {
                    continue;
                }

                NodeInfo node = new NodeInfo();

                node.id = parts[0];
                String[] part1 = StringUtils.split(parts[1], ":@");
                node.ip = part1[0];
                node.port = Integer.parseInt(part1[1]);

                if (StringUtils.contains(parts[2], "master") && (!StringUtils.contains(parts[2], "fail"))) {
                    node.isMater = true;
                }

                String masterNodeId = parts[3];
                if (!StringUtils.equals("-", masterNodeId)) {
                    node.masterNodeId = masterNodeId;
                }

                if ("connected".equals(parts[7])) {
                    node.isConnected = true;
                }

                if (9 <= parts.length) {
                    for (int i = 8; i < parts.length; i++) {
                        String oneSlotString = parts[i];

                        if (0 <= oneSlotString.indexOf('-')) {
                            // 槽位段
                            String[] slotRange = StringUtils.split(oneSlotString, '-');
                            int slotStart = Integer.parseInt(slotRange[0]);
                            int slotEnd = Integer.parseInt(slotRange[1]);
                            for (int k = slotStart; k <= slotEnd; k++) {
                                node.slots.add(k);
                            }
                        } else {
                            // 单个槽位
                            int oneSlot = Integer.parseInt(oneSlotString);
                            node.slots.add(oneSlot);
                        }
                    }


                }

                cluster.put(node.id, node);

            }

            return cluster;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(500);
            Map<String, NodeInfo> clusterNodes = this.oldClusterNodesInfo.getClusterNodes();
            for (String key : clusterNodes.keySet()) {
                NodeInfo node = clusterNodes.get(key);
                sb.append(node.toString()).append("\n");
            }
            return sb.toString();
        }
    }
}
