package com.spring.core;
/*
 * @author uv
 * @date 2018/9/30 15:06
 *
 */

import java.util.HashMap;
import java.util.Set;

public class UVModel extends HashMap<String, Object>{


    public void addAttribute(String key, Object value) {
        super.put(key, value);
    }
    public Object getAttribute(String key) {
        return super.get(key);
    }
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return super.entrySet();
    }
}
