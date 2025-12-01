package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.user.pojo.dto.NoviPersonaSettings;
import com.n1etzsch3.novi.common.pojo.dto.Result;
import com.n1etzsch3.novi.user.service.UserPreferenceService;
import com.n1etzsch3.novi.common.utils.LoginUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;

/**
 * 用户偏好控制器
 * <p>
 * 管理用户偏好，例如角色设定。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户偏好", description = "用户偏好设置接口")
@ApiSupport(author = "N1etzsch3", order = 7)
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    /**
     * 获取当前用户的角色设定。
     *
     * @return 包含角色设定的结果。
     */
    @GetMapping
    @Operation(summary = "获取角色设定", description = "获取当前用户的 AI 角色设定")
    @ApiOperationSupport(author = "N1etzsch3", order = 1)
    public Result getSettings() {
        Long userId = LoginUserContext.getUserId();
        log.info("Retrieved persona settings for user: {}", userId);
        return Result.success(userPreferenceService.getPersonaSettings(userId));
    }

    /**
     * 更新当前用户的角色设定。
     *
     * @param settings 新的角色设定。
     * @return 包含更新后设定的结果。
     */
    @PutMapping
    @Operation(summary = "更新角色设定", description = "更新当前用户的 AI 角色设定")
    @ApiOperationSupport(author = "N1etzsch3", order = 2)
    public Result updateSettings(@RequestBody NoviPersonaSettings settings) {
        Long userId = LoginUserContext.getUserId();
        log.info("Updated persona settings for user: {}", userId);
        return Result.success(userPreferenceService.updatePersonaSettings(userId, settings));
    }
}