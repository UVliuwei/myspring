package com.uv.controller;

import com.spring.annotation.UVAutowried;
import com.spring.annotation.UVController;
import com.spring.annotation.UVRequestMapping;
import com.spring.annotation.UVRequestParam;
import com.spring.annotation.UVResponseBody;
import com.spring.core.UVModel;
import com.uv.entity.User;
import com.uv.service.UserService;

/*
 * @author uv
 * @date 2018/9/29 10:46
 *
 */
@UVController
@UVRequestMapping("user")
public class UserController {

    @UVAutowried
    private UserService userService;

    @UVRequestMapping("user")
    @UVResponseBody
    public User getUser() {
        return userService.getUser();
    }
    /**
     在Java 8之前的版本，代码编译为class文件后，方法参数的类型是固定的，但参数名称却丢失了，
     这和动态语言严重依赖参数名称形成了鲜明对比。
     现在，Java 8开始在class文件中保留参数名，给反射带来了极大的便利。
     但是！！！！换成JDK8以后，也配置了编辑器，但是参数名始终不对，所以就暂时所有参数使用用@UVRequestParam
     **/
    @UVRequestMapping("hello")
    public String hello(@UVRequestParam("name") String name, @UVRequestParam("model") UVModel model) {
        model.addAttribute("name", name);
        return "hello";
    }

}
