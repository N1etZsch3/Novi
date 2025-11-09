package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.pojo.dto.Result;
import com.n1etzsch3.novi.pojo.dto.registrationRequest;
import com.n1etzsch3.novi.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping; // 导入
import org.springframework.web.bind.annotation.RestController;

// 1. 移除 @Controller
// 2. 使用 @RequestMapping 设置基础路径
@RestController
@RequestMapping("/api/v1/users")
@Slf4j
public class UserAccountController {

    @Autowired
    UserAccountService userAccountService;

    /**
     * 用户注册
     */
    @PostMapping("/register") // 路径现在是 /api/v1/users/register
    public Result register(@RequestBody registrationRequest registrationRequest) {

        userAccountService.registerUser(registrationRequest);

        log.info("用户注册成功: {}", registrationRequest.getUsername());
        return Result.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login") // 路径现在是 /api/v1/users/login
    public Result login() {
        return null;
    }
}