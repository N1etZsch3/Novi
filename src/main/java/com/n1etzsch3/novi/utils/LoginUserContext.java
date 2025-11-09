package com.n1etzsch3.novi.utils;

/**
 * ThreadLocal 工具类，用于存储当前登录的用户ID
 */
public class LoginUserContext {

    // ThreadLocal 存储 Long 类型的用户ID
    private static final ThreadLocal<Long> userHolder = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        userHolder.set(userId);
    }

    public static Long getUserId() {
        return userHolder.get();
    }

    public static void clear() {
        userHolder.remove();
    }
}