package com.spring.annotation;
/*
 * @author uv
 * @date 2018/9/29 10:00
 * 注入,通过类型注入
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//注解在成员变量使用
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UVAutowried {
}
