package com.spring.annotation;
/*
 * @author uv
 * @date 2018/9/29 9:58
 *
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//注解在类上使用
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UVController {
    String value() default "";
}
