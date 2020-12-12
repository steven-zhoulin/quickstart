package com.asiainfo.bits.core.redis.driver.io;

import com.asiainfo.bits.core.redis.util.RedisIOUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 *
 * @className: SockOIO
 * @description: 基于OIO实现
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-3-24
 */
@Slf4j
public class SockBIO implements ISockIO {

    private static final int BUFFED_INITSIZE = 1024 * 64;

    private SockIOBucket bucket;
    private String host;
    private int port;
    private int timeout = 5000;
    private boolean tcpNoDelay = true;

    private Socket socket;

    private BufferedInputStream in;
    private BufferedOutputStream out;
    private ByteArrayOutputStream baos;
    private ByteArrayOutputStream bos;
    private boolean isMaster;
    private String password;
    private boolean authed;
    private int version = 0;

    /**
     * SockOIO构造函数
     *
     * @param bucket   归属桶
     * @param host     缓存服务器地址
     * @param port     缓存服务器端口
     * @param version
     * @param isMaster
     */
    public SockBIO(SockIOBucket bucket, String host, int port, int version, boolean isMaster, String password) {
        this.bucket = bucket;
        this.host = host;
        this.port = port;
        this.version = version;
        this.isMaster = isMaster;
        this.password = password;
        this.bos = new ByteArrayOutputStream();
    }

    @Override
    public boolean init() {
        try {
            InetAddress addr = InetAddress.getByName(host);
            this.socket = new Socket();

            // 连接3秒超时
            this.socket.connect(new InetSocketAddress(addr, port), 3000);

            if (timeout > 0) {
                this.socket.setSoTimeout(timeout);
            }

            this.socket.setTcpNoDelay(tcpNoDelay);
            this.in = new BufferedInputStream(socket.getInputStream(), BUFFED_INITSIZE);
            this.out = new BufferedOutputStream(socket.getOutputStream(), BUFFED_INITSIZE);
            this.baos = new ByteArrayOutputStream(300);
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
        out.write(b);

    }

    @Override
    public void write(byte[] bytes) throws IOException {
        out.write(bytes);
    }

    @Override
    public byte read() throws IOException {
        return (byte) in.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public boolean isConnected() {
        return (socket != null && socket.isConnected());
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

    @Override
    public boolean isAlive() {
        final byte[] CRLF = "\r\n".getBytes();
        final byte[] PING = "PING".getBytes();
        final byte[] PONG = "PONG".getBytes();

        if (!isConnected())
            return false;

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
        this.bucket.returnSockIO(this);
    }

    @Override
    public SockIOBucket getBucket() {
        return bucket;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void close() throws IOException {
        if (null != in) {
            in.close();
            in = null;
        }

        if (null != out) {
            out.close();
            out = null;
        }

        if (null != socket) {
            socket.close();
            socket = null;
        }

        bucket.delSock(this);
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
