package com.asiainfo.bits.core.redis.driver.io;

import java.io.IOException;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 *
 * @className: ISockIO
 * @description: Redis Sock接口
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-8-5
 */
public interface ISockIO {

    /**
     * 初始化
     *
     * @throws IOException
     */
    public boolean init() throws IOException;

    /**
     * 写byte
     *
     * @param b
     * @throws IOException
     */
    public void write(byte b) throws IOException;

    /**
     * 写byte[]
     *
     * @param bytes
     * @throws IOException
     */
    public void write(byte[] bytes) throws IOException;

    /**
     * 读byte
     *
     * @return
     * @throws IOException
     */
    public byte read() throws IOException;

    /**
     * 读指定长度的byte[]
     *
     * @param b
     * @param off
     * @param len
     * @return
     * @throws IOException
     */
    public int read(byte[] b, int off, int len) throws IOException;

    /**
     * 刷新
     *
     * @throws IOException
     */
    public void flush() throws IOException;

    /**
     * 是否连接
     *
     * @return
     */
    public boolean isConnected();

    /**
     * 心跳检测
     *
     * @return
     */
    public boolean isAlive();

    /**
     * 读一行
     *
     * @return
     * @throws IOException
     */
    public byte[] readLineBytes() throws IOException;

    public void release();

    public SockIOBucket getBucket();

    public String getHost();

    public int getPort();

    public void close() throws IOException;

    /**
     * 连接版本号
     *
     * @return
     */
    public int getVersion();

    /**
     * 是否为主地址连接
     *
     * @return
     */
    public boolean isMaster();

    /**
     * 认证
     *
     * @param password
     * @return
     */
    public boolean auth(String password);

    /**
     * 是否已认证
     *
     * @return
     */
    public boolean isAuthed();

    /**
     * @param authed
     */
    public void setAuthed(boolean authed);
}
