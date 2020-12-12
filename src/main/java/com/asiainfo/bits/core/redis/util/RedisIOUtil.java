package com.asiainfo.bits.core.redis.util;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 *
 * @className: IOUtil
 * @description: 数字编码工具类
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-8-5
 */
public final class RedisIOUtil {

    private final static int NUM_MAPPING_SIZE = 10000;

    /**
     * 10000以内的整数快速进行byte[]转换映射，可比JDK中原生String.valueOf(i).getBytes()快35倍。
     */
    private final static Object[] NUM_MAPPING = new Object[NUM_MAPPING_SIZE];

    static {
        for (int i = 0; i < NUM_MAPPING_SIZE; i++) {
            NUM_MAPPING[i] = String.valueOf(i).getBytes();
        }
    }

    /**
     * 数字字符表示快速查找表
     */
    private final static byte[] digits = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z'
    };

    /**
     * 十位数快速查找表
     */
    private final static byte[] DigitTens = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };

    /**
     * 个位数快速查找表
     */
    private final static byte[] DigitOnes = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    /**
     * 数字大小快速查找表
     */
    private final static long[] sizeTable = {
        9L,
        99L,
        999L,
        9999L,
        99999L,
        999999L,
        9999999L,
        99999999L,
        999999999L,
        9999999999L,
        99999999999L,
        999999999999L,
        9999999999999L,
        99999999999999L,
        999999999999999L,
        9999999999999999L,
        99999999999999999L,
        999999999999999999L,
        Long.MAX_VALUE
    };

    /**
     * 返回字符串表示法的长度
     *
     * @param x
     * @return
     */
    public static final int stringSize(long x) {
        for (int i = 0; ; i++) {
            if (x <= sizeTable[i]) {
                return i + 1;
            }
        }
    }

    /**
     * 长整型转byte数组
     *
     * @param i
     * @return
     */
    public static final byte[] encode(long i) {

        if (i >= 0 && i < NUM_MAPPING_SIZE) {
            return (byte[]) NUM_MAPPING[(int) i];
        }

        int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
        byte[] rtn = new byte[size];

        long q;
        int r;
        int charPos = size;
        byte sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            rtn[--charPos] = DigitOnes[r];
            rtn[--charPos] = DigitTens[r];
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            rtn[--charPos] = DigitOnes[r];
            rtn[--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        for (; ; ) {
            q2 = (i2 * 52429) >>> (19);
            r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
            rtn[--charPos] = digits[r];
            i2 = q2;
            if (i2 == 0) break;
        }
        if (sign != 0) {
            rtn[--charPos] = sign;
        }

        return rtn;
    }

    /**
     * 转换正确性对比函数
     *
     * @param n
     * @return
     */
    private static final byte[] encodeSlow(long n) {
        return Long.toString(n).getBytes();
    }

    public static final int parseInt(byte[] bytes) {
        int rtn = 0;
        for (int i = 0; i < bytes.length; i++) {
            rtn = rtn * 10 + (bytes[i] - '0');
        }
        return rtn;
    }

    public static final long parseLong(byte[] bytes) {
        long rtn = 0;
        for (int i = 0; i < bytes.length; i++) {
            rtn = rtn * 10 + (bytes[i] - '0');
        }
        return rtn;
    }

    public static final int decodeSlow(byte[] bytes) {
        return Integer.parseInt(new String(bytes));
    }

    public static void main(String[] args) throws Exception {

        long start = 0;
        for (int k = 0; k < 10; k++) {

            start = System.currentTimeMillis();
            for (int j = 0; j < 1000; j++) {
                for (long i = 0; i < 10000; i++) {
                    encodeSlow(i);
                }
            }
            System.out.print(" 慢速编码耗时:" + (System.currentTimeMillis() - start) + "毫秒");

            start = System.currentTimeMillis();
            for (int j = 0; j < 1000; j++) {
                for (long i = 0; i < 10000; i++) {
                    encode(i);
                }
            }
            System.out.println(" 快速编码耗时:" + (System.currentTimeMillis() - start) + "毫秒");
        }

    }

}
