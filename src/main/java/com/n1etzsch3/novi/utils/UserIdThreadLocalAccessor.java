package com.n1etzsch3.novi.utils;

import io.micrometer.context.ThreadLocalAccessor;
import org.springframework.lang.NonNull;

/**
 * 实现了 ThreadLocalAccessor 接口，
 * 用于"教会" context-propagation 库如何读/写 LoginUserContext 的 ThreadLocal。
 * 这对应研究报告中的“方案 B”。
 */
public class UserIdThreadLocalAccessor implements ThreadLocalAccessor<Long> {

    // 定义一个唯一的 Key
    public static final String KEY = "novi.userId";

    @Override
    public Object key() {
        return KEY;
    }

    @Override
    public Long getValue() {
        // 如何读取
        return LoginUserContext.getUserId();
    }

    @Override
    public void setValue(@NonNull Long value) {
        // 如何恢复
        LoginUserContext.setUserId(value);
    }

    @Override
    public void setValue() {
        // 如何清理 (当值不存在时)
        LoginUserContext.clear();
    }
}