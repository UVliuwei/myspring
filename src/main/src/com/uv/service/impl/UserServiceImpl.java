package com.uv.service.impl;

import com.spring.annotation.UVService;
import com.uv.entity.User;
import com.uv.service.UserService;

/*
 * @author uv
 * @date 2018/9/29 10:38
 *
 */
@UVService
public class UserServiceImpl implements UserService{

    public User getUser() {
        User user = new User("1", "Tom",18);
        return user;
    }

}
