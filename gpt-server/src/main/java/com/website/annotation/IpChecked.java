package com.website.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IpChecked {
    /**
     * ip白名单
     */
    String value() default "0";
}

