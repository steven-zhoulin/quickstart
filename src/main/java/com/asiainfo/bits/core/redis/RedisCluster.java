package com.asiainfo.bits.core.redis;

import java.util.TreeSet;

/**
 * Copyright: Copyright (c) 2014 Asiainfo
 *
 * @className: RedisCluster
 * @description: Redis集群对象
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2014-7-14
 */
public class RedisCluster {

    private String name = null;

    private String password = "";

    /**
     * 集群模式:
     * client: 默认模式，客户端负责实现集群机制
     * distribute:	Redis服务端负责实现集群机制
     */
    private boolean isServerCluster = false;

    private int heartbeatSecond = 5;
    private int poolSize = 5;
    private boolean useNIO = false;

    public boolean isUseNIO() {
        return useNIO;
    }

    public void setUseNIO(boolean useNIO) {
        this.useNIO = useNIO;
    }

    public boolean isServerCluster() {
        return isServerCluster;
    }

    public void setServerCluster(boolean serverCluster) {
        isServerCluster = serverCluster;
    }

    private TreeSet<RedisAddress> address = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getHeartbeatSecond() {
        return heartbeatSecond;
    }

    public void setHeartbeatSecond(int heartbeatSecond) {
        this.heartbeatSecond = heartbeatSecond;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public TreeSet<RedisAddress> getAddress() {
        return address;
    }

    public void setAddress(TreeSet<RedisAddress> address) {
        this.address = address;
    }

    @Override
    public String toString() {
        StringBuilder sbuff = new StringBuilder();
        sbuff.append("{ name:" + name);
        sbuff.append(", heartbeatSecond:" + heartbeatSecond);
        sbuff.append(", poolSize:" + poolSize);
        sbuff.append(", isServerCluster: " + isServerCluster);
        sbuff.append(", address:" + address.toString());
        sbuff.append("}");
        return sbuff.toString();
    }
}
