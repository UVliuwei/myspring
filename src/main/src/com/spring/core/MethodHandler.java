package com.spring.core;
/*
 * @author liuwei
 * @date 2018/9/30 10:33
 *
 */

import java.lang.reflect.Method;
import java.util.List;
import lombok.Data;

@Data
public class MethodHandler {

    //方法所在的类
    private Object object;

    private Method method;
    //参数顺序
    private List<String> params;
    //参数列表中是否有UVModel，如果有则modelIndex为参数列表的索引，否则为-1
    private Integer modelIndex = -1;
}
