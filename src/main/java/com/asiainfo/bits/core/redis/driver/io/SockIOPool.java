package com.asiainfo.bits.core.redis.driver.io;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @className: SockIOPool
 * @description: Redis Sock连接池
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-8-5
 */
public interface SockIOPool {

    /**
     * 是否分片
     *
     * @return
     */
    boolean isSharding();


    /**
     * 根据key获取一个socket连接
     *
     * @param key
     * @return
     */
    ISockIO getSock(String key);


}
