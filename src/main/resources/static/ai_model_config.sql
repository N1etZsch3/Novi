-- AI 模型配置表
-- 用于存储各种 AI 模型的配置信息，支持模型热切换

DROP TABLE IF EXISTS `ai_model_config`;

CREATE TABLE `ai_model_config`
(
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `model_name`       VARCHAR(100) NOT NULL COMMENT '模型名称（如：doubao-seed-1-6-flash-250828）',
    `base_url`         VARCHAR(255) NOT NULL COMMENT 'API基础URL',
    `api_key`          VARCHAR(255) NOT NULL COMMENT 'API密钥',
    `completions_path` VARCHAR(100) DEFAULT '/chat/completions' COMMENT '完成接口路径',
    `is_active`        TINYINT(1)   DEFAULT 0 COMMENT '是否为当前激活模型（0:否，1:是）',
    `description`      VARCHAR(255) COMMENT '模型描述',
    `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_model_name` (`model_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='AI模型配置表';

-- 插入默认模型配置（从 application.yml 迁移）
INSERT INTO `ai_model_config` (`model_name`, `base_url`, `api_key`, `completions_path`, `is_active`,
                                `description`)
VALUES ('doubao-seed-1-6-flash-250828', 'https://ark.cn-beijing.volces.com/api/v3', '${AI_API_KEY}',
        '/chat/completions', 1, '豆包模型 - 快速响应版本');

-- 可添加更多模型配置示例
-- INSERT INTO `ai_model_config` (`model_name`, `base_url`, `api_key`, `completions_path`, `is_active`, `description`)
-- VALUES ('gpt-3.5-turbo', 'https://api.openai.com/v1', 'your-api-key', '/chat/completions', 0, 'OpenAI GPT-3.5 Turbo');
