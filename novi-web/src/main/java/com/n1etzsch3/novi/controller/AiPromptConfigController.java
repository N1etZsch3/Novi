package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.common.pojo.dto.Result;
import com.n1etzsch3.novi.common.pojo.entity.AiPromptConfig;
import com.n1etzsch3.novi.aiconfig.service.AiPromptConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;

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
@Tag(name = "AI 提示词配置", description = "AI 提示词配置管理接口")
@ApiSupport(author = "N1etzsch3", order = 6)
public class AiPromptConfigController {

    private final AiPromptConfigService aiPromptConfigService;

    /**
     * 添加新配置
     *
     * @param config 要添加的配置对象。
     * @return 成功结果。
     */
    @PostMapping
    @Operation(summary = "添加配置", description = "添加新的 AI 提示词配置")
    @ApiOperationSupport(author = "N1etzsch3", order = 1)
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
    @Operation(summary = "删除配置", description = "根据 Key 删除 AI 提示词配置")
    @ApiOperationSupport(author = "N1etzsch3", order = 2)
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
    @Operation(summary = "按类型列出配置", description = "根据类型获取 AI 提示词配置列表 (0:系统, 1:性格, 2:语气风格)")
    @ApiOperationSupport(author = "N1etzsch3", order = 3)
    public Result listConfigsByType(@PathVariable Integer type) {
        List<AiPromptConfig> list = aiPromptConfigService.listConfigsByType(type);
        log.info("Listed prompt configs by type: {}", type);
        return Result.success(list);
    }
}
