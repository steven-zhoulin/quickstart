package com.asiainfo.bits.core.redis.util;

import com.asiainfo.bits.core.redis.client.BinaryRedisClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright: Copyright (c) 2013 Asiainfo
 *
 * @className: RedisUtil
 * @description: RedisUtil工具类
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2014-9-18
 */
@Slf4j
public final class RedisUtil {

    private static final byte[] CRLF = "\r\n".getBytes();
    private static final byte[] INFO = "INFO".getBytes();
    private static final byte[] CLUSTER = "CLUSTER".getBytes();
    private static final byte[] NODES = "NODES".getBytes();
    private static final byte[] AUTH = "AUTH".getBytes();

    private RedisUtil() {
        // 工具类，无需实例化
    }

    /**
     * 获取Redis监控数据
     *
     * @param address
     * @return
     */
    public static Map<String, String> info(String address) throws Exception {
        return info(address, null);
    }

    /**
     * 获取Redis监控数据
     *
     * @param address
     * @param password
     * @return
     */
    public static Map<String, String> info(String address, String password) throws Exception {

        Map<String, String> rtn = new HashMap<String, String>();

        Socket socket = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;

        try {

            String[] part = StringUtils.split(address, ':');
            socket = new Socket(part[0], Integer.parseInt(part[1]));

            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());

            if (StringUtils.isNotBlank(password)) {
                if (!auth(in, out, password)) {
                    return rtn;
                }
            }

            out.write((byte) '*');
            out.write((byte) '1');
            out.write(CRLF);
            out.write((byte) '$');
            out.write((byte) '4');
            out.write(CRLF);
            out.write(INFO);
            out.write(CRLF);
            out.flush();

            // 1. 读取$字符
            in.read();

            // 2. 读取响应数据长度
            byte[] bytes = readLineBytes(in);
            long size = RedisIOUtil.parseLong(bytes);
            int iSize = (int) size;
            if (iSize < 0) {
                return null;
            }

            bytes = new byte[iSize];

            int cnt = 0;
            while (cnt < iSize) {
                cnt += in.read(bytes, cnt, (iSize - cnt));
            }

            // 3. 读取末尾CRLF
            readLineBytes(in);
            String content = new String(bytes);

            String[] lines = StringUtils.split(content, "\r\n");
            for (String line : lines) {
                if (line.startsWith("#")) {
                    continue;
                }

                String[] lineSplit = StringUtils.split(line, ":");
                rtn.put(lineSplit[0], lineSplit[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
                if (null != out) {
                    out.close();
                }
                if (null != socket) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return rtn;
    }

    public static String[] clusterNodes(String address) throws Exception {
        return clusterNodes(address, null);
    }

    public static String[] clusterNodes(String address, String password) throws Exception {

        String[] rtn = null;

        Socket socket = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;

        try {

            String[] part = StringUtils.split(address, ':');
            socket = new Socket(part[0], Integer.parseInt(part[1]));

            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());

            if (StringUtils.isNotBlank(password)) {
                if (!auth(in, out, password)) {
                    return rtn;
                }
            }

            out.write((byte) '*');
            out.write((byte) '2');
            out.write(CRLF);
            out.write((byte) '$');
            out.write((byte) '7');
            out.write(CRLF);
            out.write(CLUSTER);
            out.write(CRLF);
            out.write((byte) '$');
            out.write((byte) '5');
            out.write(CRLF);
            out.write(NODES);
            out.write(CRLF);
            out.flush();

            // 1. 读取$字符
            int c = in.read();
            if (BinaryRedisClient.REDIS_REPLY_ERROR == c) {
                byte[] bytes = readLineBytes(in);
                throw new Exception(new String(bytes));
            }

            // 2. 读取响应数据长度
            byte[] bytes = readLineBytes(in);
            long size = RedisIOUtil.parseLong(bytes);

            int iSize = (int) size;
            if (iSize < 0) {
                return null;
            }

            bytes = new byte[iSize];

            int cnt = 0;
            while (cnt < iSize) {
                cnt += in.read(bytes, cnt, (iSize - cnt));
            }

            // 3. 读取末尾CRLF
            readLineBytes(in);
            String content = new String(bytes);

            rtn = StringUtils.split(content, "\r\n");

        } catch (IOException e) {
            log.error("通过 " + address + " 探测 redis 集群信息失败!", e);
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
                if (null != out) {
                    out.close();
                }
                if (null != socket) {
                    socket.close();
                }
            } catch (IOException e) {

            }
        }

        return rtn;

    }

    private static boolean auth(BufferedInputStream in, BufferedOutputStream out, String password) throws Exception {

        boolean rtn = false;

        try {

            out.write((byte) '*');
            out.write((byte) '2');
            out.write(CRLF);
            out.write((byte) '$');
            out.write((byte) '4');
            out.write(CRLF);
            out.write(AUTH);
            out.write(CRLF);
            out.write((byte) '$');
            out.write(RedisIOUtil.encode(password.length()));
            out.write(CRLF);
            out.write(password.getBytes());
            out.write(CRLF);
            out.flush();


            // 1. 读取+字符
            in.read();

            // 2. 读取响应数据长度
            byte[] bytes = readLineBytes(in);
            String content = new String(bytes);
            if ("OK".equals(content)) {
                rtn = true;
            } else {
                throw new Exception(content);
            }

        } catch (IOException e) {
            e.printStackTrace();
            rtn = false;
        }

        return rtn;
    }

    /**
     * 读取一行数据
     *
     * @param in
     * @return
     * @throws IOException
     */
    private static byte[] readLineBytes(BufferedInputStream in) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] rtn = null;

        boolean eol = false;
        int one;
        while ((one = in.read()) != -1) {
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

    public static void main(String[] args) throws Exception {

        String[] lines = RedisUtil.clusterNodes("192.168.114.128:7001", "1q1w1e1r");
        for (String line : lines) {
            System.out.println(line);
        }

        Map<String, String> infos = RedisUtil.info("192.168.114.128:7001", "1q1w1e1r");

        /**
         * Server基本信息
         */
        System.out.println("Redis版本: " + infos.get("redis_version"));
        System.out.println("Redis运行模式: " + infos.get("redis_mode"));
        System.out.println("IO模式: " + infos.get("multiplexing_api"));
        System.out.println("TCP端口: " + infos.get("tcp_port"));
        System.out.println("角色: " + infos.get("role"));
        System.out.println("运行时长 :" + infos.get("uptime_in_seconds"));
        System.out.println("从库数: " + infos.get("connected_slaves"));

        /**
         * Client基本信息
         */

        System.out.println("在线客户端数: " + infos.get("connected_clients"));
        System.out.println("被阻塞的客户端数: " + infos.get("blocked_clients"));

        /**
         * 内存信息
         */
        System.out.println("已使用的内存(Byte): " + infos.get("used_memory"));
        System.out.println("已使用的内存(易读): " + infos.get("used_memory_human"));
        System.out.println("内存使用峰值(Byte): " + infos.get("used_memory_peak"));
        System.out.println("内存使用峰值(易读): " + infos.get("used_memory_peak_human"));
        System.out.println("系统已分配内存(Byte): " + infos.get("used_memory_rss"));
        System.out.println("已分配/已使用(越大表示内存碎片越多): " + infos.get("mem_fragmentation_ratio"));

        /**
         * 统计信息
         */
        System.out.println("共处理请求数: " + infos.get("total_commands_processed"));
        System.out.println("共建立过多少次连接: " + infos.get("total_connections_received"));
        System.out.println("共执行指令数: " + infos.get("total_commands_processed"));
        System.out.println("每秒执行的指令数: " + infos.get("instantaneous_ops_per_sec"));
        System.out.println("过期的KEY总数: " + infos.get("expired_keys"));
        System.out.println("内存限制被逐出的KEY总数: " + infos.get("evicted_keys"));
        System.out.println("命中次数: " + infos.get("keyspace_hits"));
        System.out.println("未命中次数: " + infos.get("keyspace_misses"));


    }
}
