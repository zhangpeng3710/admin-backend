package com.roc.admin.backend.aop.mybatis.annotation;

import java.lang.annotation.*;

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
