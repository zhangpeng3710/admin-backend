package com.roc.admin.backend.aop.mybatis;

import cn.hutool.crypto.SmUtil;
import com.roc.admin.backend.aop.mybatis.annotation.SensitiveField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @Description
 * @Author: Zhang Peng
 * @Date: 2025/4/2
 */
@Slf4j
@Component
public class CryptUtil {
    /**
     * 此处使用AES-128-ECB加密模式，key需要为16位。
     */
    @Value("${crypt.key.sm4}")
    String cKey;


    /**
     * 加密
     *
     * @param declaredFields paramsObject所声明的字段
     * @param paramsObject   mapper中paramsType的实例
     * @return T
     * @throws IllegalAccessException 字段不可访问异常
     */
    public <T> T encrypt(Field[] declaredFields, T paramsObject) throws Exception {
        for (Field field : declaredFields) {
            //取出所有被EncryptDecryptField注解的字段
            SensitiveField sensitiveField = field.getAnnotation(SensitiveField.class);
            if (!Objects.isNull(sensitiveField)) {
                field.setAccessible(true);
                Object object = field.get(paramsObject);
                //暂时只实现String类型的加密
                if (object instanceof String) {
                    String value = (String) object;
                    //加密  这里我使用自定义的AES加密工具
                    field.set(paramsObject, SmUtil.sm4(cKey.getBytes(StandardCharsets.UTF_8)).encryptBase64(value));
                }
            }
        }
        return paramsObject;
    }


    /**
     * 解密
     *
     * @param result resultType的实例
     * @return T
     * @throws IllegalAccessException 字段不可访问异常
     */
    public <T> T decrypt(T result) throws Exception {
        //取出resultType的类
        Class<?> resultClass = result.getClass();
        Field[] declaredFields = resultClass.getDeclaredFields();
        for (Field field : declaredFields) {
            //取出所有被EncryptDecryptField注解的字段
            SensitiveField sensitiveField = field.getAnnotation(SensitiveField.class);
            if (!Objects.isNull(sensitiveField)) {
                field.setAccessible(true);
                Object object = field.get(result);
                //只支持String的解密
                if (object instanceof String) {
                    String value = (String) object;
                    //对注解的字段进行逐一解密
                    field.set(result, SmUtil.sm4(cKey.getBytes(StandardCharsets.UTF_8)).decryptStr(value, StandardCharsets.UTF_8));
                }
            }
        }
        return result;
    }

}
