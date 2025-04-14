package com.roc.admin.backend.aop.mybatis;

import com.roc.admin.backend.aop.mybatis.annotation.SensitiveData;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

/**
 * @Description 加密拦截器
 * @Author: Zhang Peng
 * @Date: 2025/4/2
 */
@Slf4j
@Component
@Intercepts({
        @Signature(
                type = ResultSetHandler.class,
                method = "handleResultSets",
                args = {Statement.class}
        )
})
public class DecryptInterceptor implements Interceptor {

    @Resource
    private CryptUtil decryptUtil;


    @Override
    public Object intercept(Invocation invocation) throws Exception {
        //取出查询的结果
        Object resultObject = invocation.proceed();
        if (Objects.isNull(resultObject)) {
            return null;
        }
        //基于selectList
        if (resultObject instanceof ArrayList) {
            ArrayList resultList = (ArrayList) resultObject;

            if (!CollectionUtils.isEmpty(resultList) && needToDecrypt(resultList.get(0))) {
                Class<?> objectClass = resultList.get(0).getClass();
                SensitiveData sensitiveData = AnnotationUtils.findAnnotation(objectClass, SensitiveData.class);
                if (Objects.nonNull(sensitiveData)) {
                    for (Object result : resultList) {
                        //逐一解密
                        decryptUtil.decrypt(result);
                    }
                }
            }
            //基于selectOne
        } else {
            if (needToDecrypt(resultObject)) {
                decryptUtil.decrypt(resultObject);
            }
        }
        return resultObject;
    }

    private boolean needToDecrypt(Object object) {
        Class<?> objectClass = object.getClass();
        SensitiveData sensitiveData = AnnotationUtils.findAnnotation(objectClass, SensitiveData.class);
        return Objects.nonNull(sensitiveData);
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        Interceptor.super.setProperties(properties);
    }
}
