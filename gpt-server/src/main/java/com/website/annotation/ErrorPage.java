package com.website.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解：错误页面
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ErrorPage {

    String ERROR_PAGE = "error/404";

    // 允许为空，后续还有其它参数信息
    String value() default ERROR_PAGE;

}
