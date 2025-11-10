package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.exception.BusinessException;
import com.n1etzsch3.novi.pojo.dto.*;
import com.n1etzsch3.novi.service.UserAccountService;
import com.n1etzsch3.novi.utils.JwtUtils;
import com.n1etzsch3.novi.utils.LoginUserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
     * @param registrationRequest 注册请求体
     */
    @PostMapping("/register") // 路径现在是 /api/v1/users/register
    public Result register(@RequestBody registrationRequest registrationRequest) {

        userAccountService.registerUser(registrationRequest);

        log.info("用户注册成功: {}", registrationRequest.getUsername());
        return Result.success();
    }

    /**
     * 用户登录
     * @param loginRequest 登录请求体
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
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("无法获取用户信息，请重新登录");
        }

        UserProfileDto userProfileDto = userAccountService.getUserDetailsById(userId);

        log.info("获取当前用户资料成功: {}", userProfileDto.getUsername());
        return Result.success(userProfileDto);
    }

    /**
     * 更新当前用户资料
     * @param authorizationHeader 鉴权
     */
    @PutMapping("/me")  // 路径现在是 /api/v1/users/me
    public Result updateCurrentUser(@RequestHeader("Authorization") String authorizationHeader,
                                    @RequestBody UserProfileUpdateRequest updateRequest) {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("无法获取用户信息，请重新登录");
        }

        userAccountService.updateUserProfile(userId, updateRequest);
        log.info("更新当前用户资料成功: {}", userId);
        return Result.success(userAccountService.getUserDetailsById(userId));
    }

    /**
     * 获取当前用户的偏好设置
     */
    @GetMapping("/me/preferences") // 路径: /api/v1/users/me/preferences
    public Result getUserPreferences() {
        Long userId = LoginUserContext.getUserId();
        Map<String, Object> preferences = userAccountService.getUserPreferences(userId);
        return Result.success(preferences);
    }

    /**
     * 更新(完全替换)当前用户的偏好设置
     */
    @PutMapping("/me/preferences") // 路径: /api/v1/users/me/preferences
    public Result updateUserPreferences(@RequestBody Map<String, Object> preferences) {
        Long userId = LoginUserContext.getUserId();
        Map<String, Object> updatedPreferences = userAccountService.updateUserPreferences(userId, preferences);
        return Result.success(updatedPreferences);
    }


}