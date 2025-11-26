package com.n1etzsch3.novi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1etzsch3.novi.exception.BusinessException;
import com.n1etzsch3.novi.mapper.UserAccountMapper;
import com.n1etzsch3.novi.pojo.dto.NoviPersonaSettings;
import com.n1etzsch3.novi.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserAccountMapper userAccountMapper; // 复用 Mapper
    private final ObjectMapper objectMapper;

    @Override
    public NoviPersonaSettings getPersonaSettings(Long userId) {
        // 1. 只查询需要的字段 (需要在 Mapper 中添加 findPreferencesJsonById)
        // 如果不想改 Mapper，也可以用 findById 查全量，但性能略低
        com.n1etzsch3.novi.pojo.entity.UserAccount userAccount = userAccountMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.n1etzsch3.novi.pojo.entity.UserAccount>()
                        .select(com.n1etzsch3.novi.pojo.entity.UserAccount::getPreferences)
                        .eq(com.n1etzsch3.novi.pojo.entity.UserAccount::getId, userId));
        String json = userAccount != null ? userAccount.getPreferences() : null;

        if (!StringUtils.hasText(json)) {
            return new NoviPersonaSettings(); // 返回默认配置
        }

        try {
            return objectMapper.readValue(json, NoviPersonaSettings.class);
        } catch (JsonProcessingException e) {
            log.error("解析用户偏好失败: userId={}", userId, e);
            // 容错处理：如果解析失败，返回默认值，而不是报错让前端崩溃
            return new NoviPersonaSettings();
        }
    }

    @Override
    public NoviPersonaSettings updatePersonaSettings(Long userId, NoviPersonaSettings settings) {
        // 1. 业务校验逻辑 (这里是独立 Service 的优势体现)
        if ("tsundere".equals(settings.getPersonalityMode())) {
            // 比如：傲娇模式下，语气不能是“professional”
            if ("professional".equals(settings.getToneStyle())) {
                throw new BusinessException("傲娇性格不能搭配专业语气！");
            }
        }

        // 2. 序列化并保存
        try {
            String json = objectMapper.writeValueAsString(settings);
            userAccountMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.n1etzsch3.novi.pojo.entity.UserAccount>()
                            .eq(com.n1etzsch3.novi.pojo.entity.UserAccount::getId, userId)
                            .set(com.n1etzsch3.novi.pojo.entity.UserAccount::getPreferences, json)
                            .set(com.n1etzsch3.novi.pojo.entity.UserAccount::getUpdatedAt,
                                    java.time.LocalDateTime.now()));
            return settings;
        } catch (JsonProcessingException e) {
            throw new BusinessException("偏好设置格式错误");
        }
    }
}