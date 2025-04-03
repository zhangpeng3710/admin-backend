package com.roc.admin.backend.aop.mybatis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description 注解敏感信息类的注解
 * @Author: Zhang Peng
 * @Date: 2025/4/2
 */

@Inherited
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveField {

}
