package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.pojo.dto.Result;
import com.n1etzsch3.novi.pojo.entity.AiPromptConfig;
import com.n1etzsch3.novi.service.AiPromptConfigService;
import lombok.RequiredArgsConstructor;
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
        return Result.success(list);
    }
}
