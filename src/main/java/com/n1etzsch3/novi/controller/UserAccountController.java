package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.pojo.dto.*;
import com.n1etzsch3.novi.service.UserAccountService;
import com.n1etzsch3.novi.utils.LoginUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserAccountController {

    private final UserAccountService userAccountService;

    /**
     * 用户注册
     * 
     * @param registrationRequest 注册请求体
     */
    @PostMapping("/register") // 路径现在是 /api/v1/users/register
    public Result register(@Validated @RequestBody RegistrationRequest registrationRequest) {

        return Result.error("内部测试期间，暂不开放注册。请联系管理员获取账号。");

        // userAccountService.registerUser(registrationRequest);
        //
        // log.info("用户注册成功: {}", registrationRequest.getUsername());
        // return Result.success();
    }

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求体
     */
    @PostMapping("/login") // 路径现在是 /api/v1/users/login
    public Result login(@Validated @RequestBody LoginRequest loginRequest) {
        LoginRespond loginRespond = userAccountService.login(loginRequest);

        log.info("用户登录成功");
        return Result.success(loginRespond);
    }

    /**
     * 获取当前用户资料
     */
    @GetMapping("/me") // 路径现在是 /api/v1/users/me
    public Result getCurrentUser() {
        Long userId = LoginUserContext.getUserId();

        UserProfileDto userProfileDto = userAccountService.getUserDetailsById(userId);
        log.info("获取当前用户资料成功: {}", userProfileDto.getUsername());
        return Result.success(userProfileDto);
    }

    /**
     * 更新当前用户资料
     */
    @PutMapping("/me") // 路径现在是 /api/v1/users/me
    public Result updateCurrentUser(@Validated @RequestBody UserProfileUpdateRequest updateRequest) { // 添加 @Validated
        Long userId = LoginUserContext.getUserId();

        userAccountService.updateUserProfile(userId, updateRequest);
        log.info("更新当前用户资料成功: {}", userId);

        // 按照文档，返回更新后的完整用户信息
        return Result.success(userAccountService.getUserDetailsById(userId));
    }

    // /**
    // * 获取当前用户的偏好设置
    // */
    // @GetMapping("/me/preferences") // 路径: /api/v1/users/me/preferences
    // public Result getUserPreferences() {
    // Long userId = LoginUserContext.getUserId();
    // Map<String, Object> preferences =
    // userAccountService.getUserPreferences(userId);
    // return Result.success(preferences);
    // }
    //
    // /**
    // * 更新(完全替换)当前用户的偏好设置
    // */
    // @PutMapping("/me/preferences") // 路径: /api/v1/users/me/preferences
    // public Result updateUserPreferences(@RequestBody Map<String, Object>
    // preferences) {
    // Long userId = LoginUserContext.getUserId();
    // Map<String, Object> updatedPreferences =
    // userAccountService.updateUserPreferences(userId, preferences);
    // return Result.success(updatedPreferences);
    // }

}