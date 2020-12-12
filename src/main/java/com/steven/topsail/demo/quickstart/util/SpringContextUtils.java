package com.steven.topsail.demo.quickstart.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

/**
 * @description: Spring 上下文工具类
 * @author: Steven
 * @create: 2019-10-27 12:29
 **/
@Component
public final class SpringContextUtils implements ApplicationContextAware, EmbeddedValueResolverAware {

    private static ApplicationContext context;
    private static StringValueResolver stringValueResolver;

    /**
     * 获取 Bean 对象
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        return getContext().getBean(clazz);
    }

    /**
     * 获取 Bean 对象，调用其带参数的构造函数
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz, Object... args) {
        return getContext().getBean(clazz, args);
    }

    /**
     * 获取 Spring 上下文对象
     *
     * @return
     */
    public static ApplicationContext getContext() {
        return context;
    }

    /**
     * 获取 Bean 对象
     *
     * @param name
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name) {
        return (T) getContext().getBean(name);
    }

    /**
     * 动态获取配置文件中的值
     *
     * @param key 配置文件中的key
     * @return
     */
    public static String getPropertyValue(String key) {
        key = "${" + key + "}";
        try {
            return stringValueResolver.resolveStringValue(key);
        } catch (Exception e) {
            throw new RuntimeException("找不到配置，请检查配置文件或阿波罗配置中心。属性：" + key);
        }

    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * 获取 Bean 对象
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getContext().getBean(name, clazz);
    }

    /**
     * 获取 Bean 对象
     *
     * @param name
     * @param args
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Object... args) {
        return (T) getContext().getBean(name, args);
    }

    @Override
    public void setEmbeddedValueResolver(@NonNull StringValueResolver resolver) {
        stringValueResolver = resolver;
    }


}
