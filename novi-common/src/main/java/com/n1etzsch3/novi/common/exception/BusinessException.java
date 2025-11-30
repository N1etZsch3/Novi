package com.n1etzsch3.novi.common.exception;

/**
 * 自定义业务异常
 * 我们用它来封装所有"可预期的"、"用户导致的"错误
 * 例如: "用户名已存在", "密码格式错误"
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}