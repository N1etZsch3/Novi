package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.pojo.dto.*;
import com.n1etzsch3.novi.service.UserAccountService;
import com.n1etzsch3.novi.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 1. 移除 @Controller
// 2. 使用 @RequestMapping 设置基础路径
@RestController
@RequestMapping("/api/v1/users")
@Slf4j
public class UserAccountController {

    @Autowired
    UserAccountService userAccountService;

    @Autowired
    JwtUtils jwtUtils;

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
    public Result login(@RequestBody LoginRequest loginRequest) {
        LoginRespond loginRespond = userAccountService.login(loginRequest);

        log.info("用户登录成功");
        return Result.success(loginRespond);
    }

    /**
     * 获取当前用户资料
     * @param authorizationHeader 鉴权
     */
    @GetMapping("/me")  // 路径现在是 /api/v1/users/me
    public Result getCurrentUser(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String username = String.valueOf(jwtUtils.parseToken(token));
        UserProfileDto userProfileDto = userAccountService.getUserDetailsByUsername(username);
        log.info("获取当前用户资料: {}", username);
        return Result.success(userProfileDto);
    }


}