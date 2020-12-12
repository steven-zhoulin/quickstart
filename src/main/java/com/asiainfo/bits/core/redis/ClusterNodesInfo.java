package com.asiainfo.bits.core.redis;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @className: ClusterNodesInfo
 * @description: Redis集群
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2018-10-13
 */
public class ClusterNodesInfo {

    /**
     * 集群节点集合, Key: 节点的Id
     */
    private Map<String, NodeInfo> clusterNodes = new HashMap<>();


    public void put(String id, NodeInfo node) {
        clusterNodes.put(id, node);
    }

    public Map<String, NodeInfo> getClusterNodes() {
        return this.clusterNodes;
    }

    /**
     * 返回master节点
     *
     * @return
     */
    public Map<String, NodeInfo> masterNodes() {
        Map<String, NodeInfo> rtn = new HashMap<String, NodeInfo>();
        for (String id : clusterNodes.keySet()) {
            NodeInfo node = clusterNodes.get(id);
            if (node.isMater && node.isConnected) {
                rtn.put(id, node);
            }
        }
        return rtn;
    }

    @Override
    public String toString() {
        return clusterNodes.toString();
    }

    @Override
    public boolean equals(Object anObject) {

        if (this == anObject) {
            return true;
        }

        if (anObject instanceof ClusterNodesInfo) {
            ClusterNodesInfo anotherCluster = (ClusterNodesInfo) anObject;
            if (clusterNodes.size() != anotherCluster.clusterNodes.size()) {
                return false;
            }

            for (String id : clusterNodes.keySet()) {
                NodeInfo node = clusterNodes.get(id);
                NodeInfo anotherNode = anotherCluster.clusterNodes.get(id);
                if (null == anotherNode) {
                    return false;
                }
                if (!node.equals(anotherNode)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

}
