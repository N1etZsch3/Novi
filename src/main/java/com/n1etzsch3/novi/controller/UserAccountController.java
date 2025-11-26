package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.pojo.dto.*;
import com.n1etzsch3.novi.service.UserAccountService;
import com.n1etzsch3.novi.utils.LoginUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户账户控制器
 * <p>
 * 处理用户账户操作，如注册、登录和个人资料管理。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserAccountController {

    private final UserAccountService userAccountService;

    /**
     * 注册新用户
     * <p>
     * 目前因内部测试已禁用。
     * </p>
     *
     * @param registrationRequest 注册请求体。
     * @return 指示成功或失败的结果。
     */
    @PostMapping("/register") // 路径: /api/v1/users/register
    public Result register(@Validated @RequestBody RegistrationRequest registrationRequest) {
        // return Result.error("内部测试期间，暂不开放注册。请联系管理员获取账号。");
        userAccountService.registerUser(registrationRequest);
        log.info("User registered successfully: {}",
                registrationRequest.getUsername());
        return Result.success();
    }

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求体。
     * @return 包含登录响应（令牌、用户信息）的结果。
     */
    @PostMapping("/login") // 路径: /api/v1/users/login
    public Result login(@Validated @RequestBody LoginRequest loginRequest) {
        LoginRespond loginRespond = userAccountService.login(loginRequest);
        log.info("User logged in successfully");
        return Result.success(loginRespond);
    }

    /**
     * 获取当前用户的个人资料
     *
     * @return 包含用户个人资料 DTO 的结果。
     */
    @GetMapping("/me") // 路径: /api/v1/users/me
    public Result getCurrentUser() {
        Long userId = LoginUserContext.getUserId();
        UserProfileDto userProfileDto = userAccountService.getUserDetailsById(userId);
        log.info("Retrieved current user profile: {}", userProfileDto.getUsername());
        return Result.success(userProfileDto);
    }

    /**
     * 更新当前用户的个人资料
     *
     * @param updateRequest 个人资料更新请求体。
     * @return 包含更新后的用户个人资料 DTO 的结果。
     */
    @PutMapping("/me") // 路径: /api/v1/users/me
    public Result updateCurrentUser(@Validated @RequestBody UserProfileUpdateRequest updateRequest) {
        Long userId = LoginUserContext.getUserId();
        userAccountService.updateUserProfile(userId, updateRequest);
        log.info("Updated current user profile: {}", userId);
        return Result.success(userAccountService.getUserDetailsById(userId));
    }

}