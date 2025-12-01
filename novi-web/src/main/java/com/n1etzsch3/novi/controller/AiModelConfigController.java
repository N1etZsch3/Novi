package com.n1etzsch3.novi.controller;

import com.n1etzsch3.novi.aiconfig.factory.DynamicChatModelFactory;
import com.n1etzsch3.novi.aiconfig.pojo.dto.AiModelConfigDTO;
import com.n1etzsch3.novi.common.pojo.dto.Result;
import com.n1etzsch3.novi.aiconfig.service.AiModelConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;

import java.util.List;

/**
 * AI 模型配置控制器
 * <p>
 * 提供模型管理的 REST API，支持列出、添加、删除、更新和切换模型。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@RestController
@RequestMapping("/api/model/config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI 模型配置", description = "AI 模型配置管理接口")
@ApiSupport(author = "N1etzsch3", order = 4)
public class AiModelConfigController {

    private final AiModelConfigService aiModelConfigService;
    private final DynamicChatModelFactory dynamicChatModelFactory;

    /**
     * 获取所有模型配置列表（安全版本）
     * <p>
     * 仅返回模型名称、描述等安全字段，不包含 API Key
     * </p>
     *
     * @return 包含所有模型配置DTO的结果对象
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有模型", description = "获取所有可用的 AI 模型配置列表")
    @ApiOperationSupport(author = "N1etzsch3", order = 1)
    public Result listAllModels() {
        List<AiModelConfigDTO> models = aiModelConfigService.listAllModelsDTO();
        log.info("Listed {} AI models", models.size());
        return Result.success(models);
    }

    /**
     * 获取当前激活的模型配置（安全版本）
     * <p>
     * 仅返回模型名称、描述等安全字段，不包含 API Key
     * </p>
     *
     * @return 包含当前激活模型DTO的结果对象
     */
    @GetMapping("/active")
    @Operation(summary = "获取激活模型", description = "获取当前正在使用的 AI 模型配置")
    @ApiOperationSupport(author = "N1etzsch3", order = 2)
    public Result getActiveModel() {
        AiModelConfigDTO activeModel = aiModelConfigService.getActiveModelDTO();
        if (activeModel == null) {
            log.warn("No active model found");
            return Result.error("当前没有激活的模型");
        }
        log.info("Current active model: {}", activeModel.getModelName());
        return Result.success(activeModel);
    }

    /**
     * 根据模型名称切换到指定的模型
     * <p>
     * 前端只需要传递模型名称，避免暴露模型ID等内部信息
     * </p>
     *
     * @param modelName 要激活的模型名称
     * @return 切换结果
     */
    @PostMapping("/switch/{modelName}")
    @Operation(summary = "切换模型", description = "根据模型名称切换当前使用的 AI 模型")
    @ApiOperationSupport(author = "N1etzsch3", order = 3)
    public Result switchModel(@PathVariable String modelName) {
        log.info("Switching to model: {}", modelName);
        boolean success = aiModelConfigService.switchModelByName(modelName);
        if (success) {
            // 刷新模型缓存，使新模型立即生效
            dynamicChatModelFactory.refresh();
            log.info("Successfully switched to model: {} and refreshed cache", modelName);
            return Result.success("模型切换成功");
        } else {
            log.error("Failed to switch to model: {}", modelName);
            return Result.error("模型切换失败，模型不存在");
        }
    }
}
