package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.pojo.dto.NoviPersonaSettings;
import com.n1etzsch3.novi.pojo.dto.Result;
import com.n1etzsch3.novi.service.UserPreferenceService;
import com.n1etzsch3.novi.utils.LoginUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    /**
     * 获取当前用户的角色设定。
     *
     * @return 包含角色设定的结果。
     */
    @GetMapping
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
    public Result updateSettings(@RequestBody NoviPersonaSettings settings) {
        Long userId = LoginUserContext.getUserId();
        log.info("Updated persona settings for user: {}", userId);
        return Result.success(userPreferenceService.updatePersonaSettings(userId, settings));
    }
}