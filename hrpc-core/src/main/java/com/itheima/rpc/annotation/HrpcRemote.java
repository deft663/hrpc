package com.itheima.rpc.annotation;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;

/**
 * @description
 * @author: ts
 * @create:2021-05-10 17:56
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HrpcRemote {

    String value() default "";

    /**
     * 服务接口Class
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 服务接口名称
     * @return
     */
    String interfaceName() default "";

    /**
     * 服务版本号
     * @return
     */
    String version() default "";

    /**
     * 服务分组
     * @return
     */
    String group() default "";
}
