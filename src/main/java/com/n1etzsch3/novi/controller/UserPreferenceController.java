package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.pojo.dto.NoviPersonaSettings;
import com.n1etzsch3.novi.pojo.dto.Result;
import com.n1etzsch3.novi.service.UserPreferenceService;
import com.n1etzsch3.novi.utils.LoginUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService; // 注入新 Service

    @GetMapping
    public Result getSettings() {
        Long userId = LoginUserContext.getUserId();
        return Result.success(userPreferenceService.getPersonaSettings(userId));
    }

    @PutMapping
    public Result updateSettings(@RequestBody NoviPersonaSettings settings) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(userPreferenceService.updatePersonaSettings(userId, settings));
    }
}