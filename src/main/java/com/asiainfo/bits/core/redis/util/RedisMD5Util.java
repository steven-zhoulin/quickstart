package com.asiainfo.bits.core.redis.util;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Copyright: Copyright (c) 2015 Asiainfo
 *
 * @className: MD5Util
 * @description: MD5工具
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2015-7-26
 */
@Slf4j
public final class RedisMD5Util {

    private static final char[] hexchar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private RedisMD5Util() {
    }

    public static final String hexdigest(String str) {

        MessageDigest alg = null;
        try {
            alg = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error("", e);
        }
        byte[] digest = alg.digest(str.getBytes());
        return bytesToHex(digest);

    }

    public static final String hexdigest(byte[] bytes) {
        MessageDigest alg = null;
        try {
            alg = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error("", e);
        }

        byte[] digest = alg.digest(bytes);
        return bytesToHex(digest);
    }

    private static final String bytesToHex(byte[] digest) {

        StringBuilder sb = new StringBuilder(digest.length * 2);

        for (int i = 0, size = digest.length; i < size; i++) {
            sb.append(hexchar[(digest[i] & 0xf0) >>> 4]);
            sb.append(hexchar[digest[i] & 0x0f]);
        }

        return sb.toString();

    }

}
