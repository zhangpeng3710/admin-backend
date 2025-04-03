package com.roc.admin.backend.utils.crypt;

import java.lang.reflect.Field;

/**
 * @Description
 * @Author: Zhang Peng
 * @Date: 2025/4/2
 */
public interface ICryptUtil {
    /**
     * 加密
     *
     * @param declaredFields paramsObject所声明的字段
     * @param paramsObject   mapper中paramsType的实例
     * @return T
     * @throws IllegalAccessException 字段不可访问异常
     */
    <T> T encrypt(Field[] declaredFields, T paramsObject) throws Exception;

    /**
     * 解密
     *
     * @param result resultType的实例
     * @return T
     * @throws IllegalAccessException 字段不可访问异常
     */
    <T> T decrypt(T result) throws Exception;
}
