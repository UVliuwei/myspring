package com.uv.controller;

import com.spring.annotation.UVAutowried;
import com.spring.annotation.UVController;
import com.spring.annotation.UVRequestMapping;
import com.spring.annotation.UVResponseBody;
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

    @UVRequestMapping("hello")
    public String hello(String name) {
        return "hello";
    }

}
