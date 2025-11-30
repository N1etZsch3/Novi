package com.n1etzsch3.novi.common.exception;

import com.n1etzsch3.novi.common.pojo.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获自定义的【业务异常】(例如 "用户名已存在")
     * 这是期望的、由用户操作不当引起的异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e) {
        // 对于业务异常，通常使用 warn 级别，因为它不是一个"服务器错误"
        log.warn("业务异常: {}", e.getMessage());
        // 消息是安全的 (例如 "用户名已存在")，可以直接返回给前端
        return Result.error(e.getMessage());
    }

    /**
     * 2捕获【数据库冲突异常】
     * 这是一个"兜底"，以防 Service 层的检查(if) 遗漏了某个唯一键
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据库键冲突异常: ", e);
        if (e.getMessage() != null && e.getMessage().contains("EMAIL")) {
            return Result.error("邮箱已被注册");
        }
        // 只返回一个通用的、安全的消息
        return Result.error("数据冲突，请检查您的输入");
    }

    /**
     * 新增：捕获 @Validated 校验失败的异常
     * 这会在 DTO 校验失败时触发
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationExceptions(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        // 将所有错误信息收集成一个字符串
        String errorMessage = bindingResult.getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("参数校验失败: {}", errorMessage);
        return Result.error(errorMessage);
    }

    /**
     * 捕获【非法参数异常】
     * 通常由 Service 层抛出，用于参数校验或业务逻辑检查
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 捕获所有【未知的服务器异常】(例如 NullPointerException)
     */
    @ExceptionHandler(Exception.class)
    public Result handleAllExceptions(Exception e) {
        log.error("捕获到未知的服务器异常: ", e);
        return Result.error("服务器异常，请联系管理员");
    }
}