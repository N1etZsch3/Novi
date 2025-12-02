package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.common.pojo.dto.Result;
import com.n1etzsch3.novi.common.pojo.entity.AiPromptConfig;
import com.n1etzsch3.novi.aiconfig.service.AiPromptConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 提示词配置控制器
 * <p>
 * 提供用于管理 AI 提示词配置（如性格和语气风格）的 API。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@RestController
@RequestMapping("/api/prompt/config")
@RequiredArgsConstructor
@Slf4j
public class AiPromptConfigController {

    private final AiPromptConfigService aiPromptConfigService;

    /**
     * 添加新配置
     *
     * @param config 要添加的配置对象。
     * @return 成功结果。
     */
    @PostMapping
    public Result addConfig(@RequestBody AiPromptConfig config) {
        aiPromptConfigService.addConfig(config);
        log.info("Added new prompt config: {}", config.getConfigKey());
        return Result.success();
    }

    /**
     * 根据 Key 删除配置
     *
     * @param key 要删除的配置的唯一 Key。
     * @return 成功结果。
     */
    @DeleteMapping("/{key}")
    public Result removeConfig(@PathVariable String key) {
        aiPromptConfigService.removeConfig(key);
        log.info("Removed prompt config: {}", key);
        return Result.success();
    }

    /**
     * 根据类型列出配置
     *
     * @param type 要列出的配置类型 (例如: 0:系统, 1:性格, 2:语气风格)。
     * @return 包含配置列表的结果对象。
     */
    @GetMapping("/type/{type}")
    public Result listConfigsByType(@PathVariable Integer type) {
        List<AiPromptConfig> list = aiPromptConfigService.listConfigsByType(type);
        log.info("Listed prompt configs by type: {}", type);
        return Result.success(list);
    }

    /**
     * 获取所有性格配置
     *
     * @return 性格配置列表
     */
    @GetMapping("/personalities")
    public Result getPersonalities() {
        List<AiPromptConfig> personalities = aiPromptConfigService.listConfigsByType(1);
        log.info("Fetched all personality options");
        return Result.success(personalities);
    }

    /**
     * 获取所有语气配置
     *
     * @return 语气配置列表
     */
    @GetMapping("/tones")
    public Result getTones() {
        List<AiPromptConfig> tones = aiPromptConfigService.listConfigsByType(2);
        log.info("Fetched all tone options");
        return Result.success(tones);
    }
}
