package com.asiainfo.bits.core.redis;

import lombok.Getter;
import lombok.Setter;

/**
 * Copyright: Copyright (c) 2014 Asiainfo
 *
 * @className: RedisAddress
 * @description: Redis地址对象
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2014-7-14
 */
public class RedisAddress implements Comparable<RedisAddress> {

    /**
     * 主地址
     */
    @Getter
    @Setter
    private String master;

    /**
     * 备地址
     */
    @Getter
    @Setter
    private String slave;

    /**
     * 仅按 master 排序
     *
     * @param anotherRedisAddress
     * @return
     */
    @Override
    public int compareTo(RedisAddress anotherRedisAddress) {
        String anotherMaster = anotherRedisAddress.getMaster();
        return this.master.compareTo(anotherMaster);
    }

    @Override
    public String toString() {
        return this.master + " -> " + this.slave;
    }
}
