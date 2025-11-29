package com.n1etzsch3.novi.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI模型配置实体类
 * <p>
 * 对应数据库表 `ai_model_config`。
 * 用于存储不同AI模型的配置信息，支持模型热切换。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_model_config")
public class AiModelConfig {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模型名称（如：doubao-seed-1-6-flash-250828）
     */
    private String modelName;

    /**
     * API基础URL
     */
    private String baseUrl;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 完成接口路径（默认：/chat/completions）
     */
    private String completionsPath;

    /**
     * 是否为当前激活模型（0:否，1:是）
     */
    private Boolean isActive;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
