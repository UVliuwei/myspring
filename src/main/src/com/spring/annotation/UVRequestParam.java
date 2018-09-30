package com.spring.annotation;
/*
 * @author liuwei
 * @date 2018/9/29 10:59
 *
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//注解在参数上使用
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UVRequestParam {
    String value() default "";
}
