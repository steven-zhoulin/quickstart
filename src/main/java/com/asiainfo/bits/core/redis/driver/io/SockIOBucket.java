package com.asiainfo.bits.core.redis.driver.io;

import java.io.IOException;
import java.util.Set;

/**
 * Copyright: Copyright (c) 2013 Asiainfo
 *
 * @className: SockIOBucket
 * @description: SockIO桶
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-3-24
 */
public abstract class SockIOBucket implements Comparable<SockIOBucket> {

    /**
     * 桶的状态码
     */
    public static final int STATE_ERER = 0x00; // 主备地址都不可用
    public static final int STATE_OKER = 0x01; // 主地址可用，备地址不可用
    public static final int STATE_EROK = 0x02; // 主地址不可用，备地址可用
    public static final int STATE_OKOK = 0x03; // 主备地址都可用

    public static final int STATE_ER = 0x04; // 主地址不可用，无备地址
    public static final int STATE_OK = 0x05; // 主地址可用，无备地址

    public static final int STATE_RAW = -0x01; // 未初始化

    public static final String[] STATES = {"STATE_ERER", "STATE_OKER", "STATE_EROK", "STATE_OKOK", "STATE_ER", "STATE_OK"};

    /**
     * 槽位集合
     */
    private Set<Integer> slots;


    /**
     * 桶的初始化
     *
     * @return
     */
    public abstract boolean init() throws IOException;

    /**
     * 桶的销毁，释放资源
     */
    public abstract void close();

    public abstract ISockIO borrowSockIO();

    public abstract ISockIO borrowSockIO(long timeout);

    public abstract void returnSockIO(ISockIO sock);

    public abstract boolean delSock(ISockIO sock);

    public abstract String getAddress();

    /**
     * 健康检测函数，返回桶的状态码
     *
     * @return
     */
    public abstract int healthCheck() throws IOException, InterruptedException;

    public abstract int getStateCode();

    /**
     * 设置桶状态
     *
     * @param stateCode
     */
    public abstract void setStateCode(int stateCode);

    public abstract int compareTo(SockIOBucket o);

    public Set<Integer> getSlots() {
        return slots;
    }

    public void setSlots(Set<Integer> slots) {
        this.slots = slots;
    }

}
