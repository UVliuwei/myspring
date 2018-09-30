package com.spring.annotation;
/*
 * @author uv
 * @date 2018/9/29 9:59
 *
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//注解在类和方法使用
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UVRequestMapping {
    String value() default "";
}
