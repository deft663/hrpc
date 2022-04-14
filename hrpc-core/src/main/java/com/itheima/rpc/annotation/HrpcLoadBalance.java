package com.itheima.rpc.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @description
 * @author: ts
 * @create:2021-05-13 14:17
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HrpcLoadBalance {

    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * lb策略
     * @return
     */
    String strategy() default "random";
}
