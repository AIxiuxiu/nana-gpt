package com.website.annotation;


import com.website.model.OperatingType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于需要记录操作日志的方法
 * @author XAGS-ZhaoXiaoxu
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLogger {
    /**
     * 操作类型-默认接口统计
     */
    OperatingType type() default OperatingType.API;

    String value() default "";
}
