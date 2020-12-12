package com.asiainfo.bits.core.redis.driver.io;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Copyright: Copyright (c) 2013 Asiainfo
 *
 * @className: HASockIOBucket
 * @description: 带主备功能的SockIO桶
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-3-24
 */
@Slf4j
public class HASockIOBucket extends SockIOBucket {

    private boolean useNIO = true;
    private String password;

    /**
     * 主地址连接池
     */
    private LinkedBlockingQueue<ISockIO> masterSocks = new LinkedBlockingQueue<ISockIO>();

    /**
     * 备地址连接池
     */
    private LinkedBlockingQueue<ISockIO> slaveSocks = new LinkedBlockingQueue<ISockIO>();

    /**
     * 主地址
     */
    private String masterHost;

    /**
     * 主端口
     */
    private int masterPort;

    /**
     * 备地址
     */
    private String slaveHost;

    /**
     * 备端口
     */
    private int slavePort;

    /**
     * 连接池大小
     */
    private int poolSize;

    /**
     * 桶的版本号
     */
    private int version = 0;

    /**
     * 桶状态
     */
    private int stateCode = STATE_RAW;

    /**
     * 构造桶
     *
     * @param masterHost 主地址
     * @param masterPort 主端口
     * @param slaveHost  备地址
     * @param slavePort  备端口
     * @param poolSize   连接池大小
     */
    public HASockIOBucket(String masterHost, int masterPort, String slaveHost, int slavePort, int poolSize, boolean useNIO, String password) {
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        this.slaveHost = slaveHost;
        this.slavePort = slavePort;
        this.poolSize = poolSize;
        this.useNIO = useNIO;
        this.password = password;
    }

    /**
     * 桶的初始化
     *
     * @throws IOException
     */
    @Override
    public boolean init() throws IOException {

        // 每次初始化桶递增版本号
        this.version++;

        // 初始化主地址
        for (int i = 0; i < poolSize; i++) {
            ISockIO sock = null;
            if (this.useNIO) {
                sock = new SockNIO(this, masterHost, masterPort, version, true, this.password);
            } else {
                sock = new SockBIO(this, masterHost, masterPort, version, true, this.password);
            }
            if (sock.init()) {
                masterSocks.add(sock);
            } else { // 如果连不上, 就没必要初始化第二次，不卡
                break;
            }
        }

        // 初始化备地址，默认只初始化一个连接
        {
            ISockIO sock = null;
            if (this.useNIO) {
                sock = new SockNIO(this, slaveHost, slavePort, version, false, this.password);
            } else {
                sock = new SockBIO(this, slaveHost, slavePort, version, false, this.password);
            }

            if (sock.init()) {
                slaveSocks.add(sock);
            }
        }

        if (masterSocks.size() == this.poolSize) {
            if (slaveSocks.size() == 1) {
                this.stateCode = STATE_OKOK;
            } else {
                this.stateCode = STATE_OKER;
            }
            return true;
        } else {
            if (slaveSocks.size() == 1) { // 主地址不可用的情况下，启用备地址
                this.stateCode = STATE_EROK;

                // 1. 备地址再初始化(poolSize - 1)个连接
                for (int i = 0; i < poolSize - 1; i++) {
                    ISockIO sock = null;
                    if (this.useNIO) {
                        sock = new SockNIO(this, slaveHost, slavePort, version, false, this.password);
                    } else {
                        sock = new SockBIO(this, slaveHost, slavePort, version, false, this.password);
                    }

                    if (sock.init()) {
                        slaveSocks.add(sock);
                    }
                }

                // 释放主地址资源
                this.closeMaster();
                return true;
            } else {
                this.stateCode = STATE_ERER;
                this.close(); // 释放主地址资源
                return false;
            }
        }
    }

    /**
     * 桶的销毁，关闭桶中所有的连接
     */
    @Override
    public void close() {

        // 释放主地址资源
        for (int i = 0; i < poolSize; i++) {
            try {
                ISockIO sock = masterSocks.poll();
                if (null != sock) {
                    sock.close();
                }
            } catch (Exception e) {
                log.error("redis释放主地址连接发生异常!", e);
            }
        }

        // 释放备地址资源
        for (int i = 0; i < poolSize; i++) {
            try {
                ISockIO sock = slaveSocks.poll();
                if (null != sock) {
                    sock.close();
                }
            } catch (Exception e) {
                log.error("redis释放备地址连接发生异常!", e);
            }
        }

        masterSocks.clear();
        slaveSocks.clear();
    }

    /**
     * 释放主地址资源
     */
    private void closeMaster() {
        for (int i = 0; i < poolSize; i++) {
            try {
                ISockIO sock = masterSocks.poll();
                if (null != sock) {
                    sock.close();
                }
            } catch (Exception e) {
                log.error("redis心跳发生异常!", e);
            }
        }
    }

    /**
     * 借走一个socket连接
     *
     * @return
     */
    @Override
    public ISockIO borrowSockIO() {
        return borrowSockIO(5);
    }

    /**
     * 借走一个socket连接
     *
     * @return
     */
    @Override
    public ISockIO borrowSockIO(long timeout) {

        ISockIO sock = null;

        try {
            switch (this.stateCode) {
                case STATE_OKOK:
                    sock = masterSocks.poll(timeout, TimeUnit.SECONDS);
                    break;
                case STATE_OKER:
                    sock = masterSocks.poll(timeout, TimeUnit.SECONDS);
                    break;
                case STATE_EROK:
                    sock = slaveSocks.poll(timeout, TimeUnit.SECONDS);
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return sock;

    }

    /**
     * 归还一个socket连接（有没有可能还错位置呢? 即便还错也只是链接增多，不会减少）
     *
     * @param sock
     */
    @Override
    public void returnSockIO(ISockIO sock) {

        if (this.version != sock.getVersion()) {
            // 连接的版本号和桶的版本号不一致，说明是前次桶资源释放不完全。
            try {
                sock.close();
            } catch (IOException e) {
                log.error("redis释放过期连接发生异常!", e);
            }
            return;
        }

        if (sock.isMaster()) {
            masterSocks.add(sock);
        } else {
            slaveSocks.add(sock);
        }
    }

    @Override
    public boolean delSock(ISockIO sock) {

        if (sock.isMaster()) {
            return masterSocks.remove(sock);
        } else {
            return slaveSocks.remove(sock);
        }

    }

    @Override
    public int healthCheck() throws IOException, InterruptedException {

        boolean masterAlive = false;
        boolean slaveAlive = false;

        // 1. 主地址健康检查
        {
            ISockIO io = masterSocks.poll(1, TimeUnit.SECONDS);
            if (null != io) {
                if (io.isAlive()) {
                    masterAlive = true;
                }
                io.release(); // 记得释放
            } else {
                ISockIO sock = null;
                if (this.useNIO) {
                    sock = new SockNIO(this, masterHost, masterPort, -1, false, this.password);
                } else {
                    sock = new SockBIO(this, masterHost, masterPort, -1, false, this.password);
                }

                if (null != sock) {
                    if (sock.init()) {
                        if (sock.isAlive()) {
                            masterAlive = true;
                        }
                    }
                    sock.close(); // 记得释放
                }
            }
        }

        // 2. 备地址健康检查
        {
            ISockIO io = slaveSocks.poll(1, TimeUnit.SECONDS);
            if (null != io) {
                if (io.isAlive()) {
                    slaveAlive = true;
                }
                io.release(); // 记得释放
            } else {
                ISockIO sock = null;
                if (this.useNIO) {
                    sock = new SockNIO(this, slaveHost, slavePort, -1, false, this.password);
                } else {
                    sock = new SockBIO(this, slaveHost, slavePort, -1, false, this.password);
                }

                if (sock.init()) {
                    if (sock.isAlive()) {
                        slaveAlive = true;
                    }
                    sock.close(); // 记得释放
                }
            }
        }

        if (masterAlive) {
            return slaveAlive ? STATE_OKOK : STATE_OKER;
        } else {
            return slaveAlive ? STATE_EROK : STATE_ERER;
        }
    }

    @Override
    public int getStateCode() {
        return this.stateCode;
    }

    @Override
    public void setStateCode(int stateCode) {
        this.stateCode = stateCode;
    }

    @Override
    public String getAddress() {
        return masterHost + ":" + masterPort;
    }

    @Override
    public int compareTo(SockIOBucket o) {
        return getAddress().compareTo(o.getAddress());
    }
}
