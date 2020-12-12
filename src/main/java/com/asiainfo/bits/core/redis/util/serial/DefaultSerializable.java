package com.asiainfo.bits.core.redis.util.serial;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 *
 * @className: DefaultSerializable
 * @description: 默认Java序列化与反序列化实现
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-3-20
 */
@Slf4j
public class DefaultSerializable extends AbstractSerializable {

    /**
     * 将对象编码成byte数组
     *
     * @param obj
     * @return
     */
    @Override
    public byte[] encode(Object obj) {

        byte[] rtn = null;

        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;

        try {

            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            rtn = baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != oos) {
                try {
                    oos.close();
                } catch (IOException e) {

                }
            }
        }

        return rtn;
    }

    /**
     * 将byte数组解码成对象
     *
     * @param bytes
     * @return
     */
    @Override
    public Object decode(byte[] bytes) {

        Object rtn = null;

        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;

        try {
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
            rtn = ois.readObject();
        } catch (IOException e) {
            log.error("", e);
        } catch (ClassNotFoundException e) {
            log.error("", e);
        } finally {
            if (null != ois) {
                try {
                    ois.close();
                } catch (IOException e) {
                }
            }
        }

        return rtn;
    }

}
