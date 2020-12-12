package com.asiainfo.bits.core.redis.util.serial;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 *
 * @className: ISerializable
 * @description: 序列化接口
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-2-25
 */
public interface ISerializable {

    /**
     * 将对象编码成byte数组
     *
     * @param obj
     * @return
     */
    byte[] encode(Object obj);

    /**
     * 对字节数组进行压缩
     *
     * @param data
     * @return
     */
    byte[] encodeGzip(byte[] data);

    /**
     * 将byte数组解码成对象
     *
     * @param bytes
     * @return
     */
    Object decode(byte[] bytes);

    /**
     * byte数组解压
     *
     * @param bytes
     * @return
     */
    byte[] decodeGzip(byte[] bytes);
}
