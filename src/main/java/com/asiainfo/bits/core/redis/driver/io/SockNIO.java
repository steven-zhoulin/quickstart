package com.asiainfo.bits.core.redis.driver.io;

import com.asiainfo.bits.core.redis.util.RedisIOUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 *
 * @className: SockNIO
 * @description: 基于NIO方式实现的ISockIO
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-8-5
 */
@Slf4j
public class SockNIO implements ISockIO {

    private String host;
    private int port;
    private SocketChannel channel;

    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private ByteArrayOutputStream bos;
    private SockIOBucket bucket;
    private boolean isMaster;
    private String password;
    private boolean authed;
    private int version = 0;

    /**
     * SockNIO构造函数
     *
     * @param bucket
     * @param host
     * @param port
     * @param version
     * @param isMaster
     * @throws IOException
     */
    public SockNIO(SockIOBucket bucket, String host, int port, int version, boolean isMaster, String password) throws IOException {
        this.bucket = bucket;
        this.host = host;
        this.port = port;
        this.isMaster = isMaster;
        this.password = password;
        this.version = version;
        this.readBuffer = ByteBuffer.allocateDirect(1024 * 8);
        this.writeBuffer = ByteBuffer.allocateDirect(1024 * 8);
        this.bos = new ByteArrayOutputStream();
        readBuffer.flip(); // 为读取做准备
    }

    @Override
    public boolean init() throws IOException {
        try {
            InetAddress addr = InetAddress.getByName(host);
            channel = SocketChannel.open(new InetSocketAddress(addr, this.port));
            channel.configureBlocking(true); // 阻塞模式
            if (!this.authed) {
                auth(this.password);
            }
            return true;
        } catch (IOException e) {
            log.error("redis socket连接建立失败" + host + ":" + port + " " + e);
        }
        return false;
    }

    @Override
    public void write(byte b) throws IOException {
        write(new byte[]{b}, 0, 1);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }

    /**
     * 将byte[]写入到channel，先填充满buffer，再一次性写出
     *
     * @param b
     * @param off
     * @param len
     * @throws IOException
     */
    private final void write(byte[] b, int off, int len) throws IOException {

        if (len == 0) {
            return;
        }

        if (writeBuffer.remaining() >= len) {
            writeBuffer.put(b, off, len);
        } else {
            int written = 0;
            int size = 0;
            int remain = 0;
            while ((remain = len - written) > 0) {
                size = writeBuffer.remaining(); // 缓冲区还能容纳的字节数
                size = size < remain ? size : remain; // Math.min(缓冲区还能容纳字节数,
                // 还剩多少字节要write);
                writeBuffer.put(b, off + written, size);
                flush();
                written += size;
            }
        }

    }

    /**
     * 推送当前发送缓存区中的数据
     */
    @Override
    public void flush() throws IOException {
        writeBuffer.flip();
        this.channel.write(writeBuffer);
        writeBuffer.clear();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        int remain = readBuffer.limit() - readBuffer.position();
        if (len < remain) { // 缓存区未读完的数据能够满足要求
            readBuffer.get(b, off, len);
            return len;
        } else {
            readBuffer.get(b, off, remain); // 缓存区未读完的数据能满足一部分要求
            readChannel();
            return remain;
        }

    }

    @Override
    public byte read() throws IOException {
        try {
            byte b = readBuffer.get();
            return b;
        } catch (BufferUnderflowException e) {
            readChannel();
            return readBuffer.get();
        }
    }

    private final void readChannel() throws IOException {
        readBuffer.clear();
        this.channel.read(readBuffer);
        readBuffer.flip();
    }

    @Override
    public boolean isConnected() {
        return (this.channel != null && this.channel.isConnected());
    }

    @Override
    public boolean auth(String password) {

        if (StringUtils.isBlank(password)) {
            return true;
        }

        boolean rtn = false;

        final byte[] CRLF = "\r\n".getBytes();
        final byte[] AUTH = "AUTH".getBytes();

        try {

            this.write((byte) '*');
            this.write((byte) '2');
            this.write(CRLF);
            this.write((byte) '$');
            this.write((byte) '4');
            this.write(CRLF);
            this.write(AUTH);
            this.write(CRLF);
            this.write((byte) '$');
            this.write(RedisIOUtil.encode(password.length()));
            this.write(CRLF);
            this.write(password.getBytes());
            this.write(CRLF);
            this.flush();

            // 1. 读取+字符
            this.read();

            // 2. 读取响应数据长度
            byte[] bytes = this.readLineBytes();
            String content = new String(bytes);
            if ("OK".equals(content)) {
                rtn = true;
            } else {
                log.error(content);
            }

        } catch (IOException e) {
            e.printStackTrace();
            rtn = false;
        }

        return rtn;
    }

    /**
     * 客户端与服务端心跳，通过PING命令实现
     */
    @Override
    public boolean isAlive() {

        final byte[] CRLF = "\r\n".getBytes();
        final byte[] PING = "PING".getBytes();
        final byte[] PONG = "PONG".getBytes();

        if (!isConnected()) {
            return false;
        }

        try {

            this.write((byte) '*');
            this.write((byte) '1');
            this.write(CRLF);
            this.write((byte) '$');
            this.write((byte) '4');
            this.write(CRLF);
            this.write(PING);
            this.write(CRLF);

            this.flush();
            byte b = this.read();
            if ('+' == b) {
                byte[] bytes = this.readLineBytes();
                return Arrays.equals(bytes, PONG);
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    /**
     * 读取一行数据
     */
    @Override
    public byte[] readLineBytes() throws IOException {

        byte[] rtn = null;

        boolean eol = false;
        int one;
        while ((one = read()) != -1) {
            if (13 == one) {
                eol = true;
                continue;
            } else {
                if ((eol) && (10 == one)) {
                    break;
                }
                eol = false;
            }

            bos.write((byte) one);
        }

        if (null == bos || bos.size() <= 0) {
            return null;
        }

        rtn = bos.toByteArray();
        bos.reset(); // 重置
        return rtn;
    }

    @Override
    public void release() {
        bucket.returnSockIO(this);
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public void close() throws IOException {
        if (null != this.channel) {
            this.channel.close();
        }

        bucket.delSock(this);
    }

    @Override
    public SockIOBucket getBucket() {
        return this.bucket;
    }

    @Override
    public int getVersion() {
        return this.version;
    }

    @Override
    public boolean isMaster() {
        return isMaster;
    }

    @Override
    public boolean isAuthed() {
        return this.authed;
    }

    @Override
    public void setAuthed(boolean authed) {
        this.authed = authed;
    }
}
