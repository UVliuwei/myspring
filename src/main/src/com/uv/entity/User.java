package com.uv.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
 * @author uv
 * @date 2018/9/29 10:39
 *
 */
@Data
@AllArgsConstructor
public class User {

    private String id;

    private String name;

    private int age;

}
