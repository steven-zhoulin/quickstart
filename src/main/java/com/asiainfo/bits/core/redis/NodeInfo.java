package com.asiainfo.bits.core.redis;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @className: NodeInfo
 * @description: Redis集群Node信息
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2018-10-13
 */
public class NodeInfo {
    /**
     * 节点唯一标识
     */
    public String id;

    /**
     * 节点IP
     */
    public String ip;

    /**
     * 节点端口
     */
    public int port;

    /**
     * 是否为master节点
     */
    public boolean isMater = false;

    /**
     * 是否可连通
     */
    public boolean isConnected = false;

    /**
     * 存储该节点的槽位信息
     */
    public Set<Integer> slots = new HashSet<Integer>();

    /**
     * 主节点ID
     */
    public String masterNodeId;

    @Override
    public boolean equals(Object anObject) {

        if (this == anObject) {
            return true;
        }

        if (anObject instanceof NodeInfo) {
            NodeInfo anotherNodeInfo = (NodeInfo) anObject;

            if (StringUtils.equals(this.id, anotherNodeInfo.id) &&
                StringUtils.equals(this.ip, anotherNodeInfo.ip) &&
                this.slots.equals(anotherNodeInfo.slots) &&
                this.port == anotherNodeInfo.port &&
                this.isMater == anotherNodeInfo.isMater &&
                this.isConnected == anotherNodeInfo.isConnected &&
                StringUtils.equals(this.masterNodeId, anotherNodeInfo.masterNodeId)) {
                return true;
            }
        }

        return false;

    }

    @Override
    public String toString() {
        return String.format("{id=%41s, ip=%15s, port=%5d, isMater=%5b, isConnected=%5b masterNodeId=%s slots=%s}", id, ip, port, isMater, isConnected, masterNodeId, slots);
    }
}
