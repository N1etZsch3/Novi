package com.n1etzsch3.novi.exception;

import com.n1etzsch3.novi.pojo.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. 捕获自定义的【业务异常】(例如 "用户名已存在")
     * 这是我们期望的、由用户操作不当引起的异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e) {
        // 对于业务异常，通常使用 warn 级别，因为它不是一个"服务器错误"
        log.warn("业务异常: {}", e.getMessage());
        // 消息是安全的 (例如 "用户名已存在")，可以直接返回给前端
        return Result.error(e.getMessage());
    }

    /**
     * 2. 捕获【数据库冲突异常】
     * 这是一个"兜底"，以防我们 Service 层的检查(if) 遗漏了某个唯一键
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据库键冲突异常: ", e);
        // 只返回一个通用的、安全的消息
        return Result.error("数据冲突，请检查您的输入");
    }

    /**
     * 3. 捕获所有【未知的服务器异常】(例如 NullPointerException)
     */
    @ExceptionHandler(Exception.class)
    public Result handleAllExceptions(Exception e) {
        log.error("捕获到未知的服务器异常: ", e);
        return Result.error("服务器异常，请联系管理员");
    }
}