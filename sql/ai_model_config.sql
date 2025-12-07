CREATE TABLE `ai_model_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `model_name` varchar(100) NOT NULL COMMENT '模型名称（如：doubao-seed-1-6-flash-250828）',
  `base_url` varchar(255) NOT NULL COMMENT 'API基础URL',
  `api_key` varchar(255) NOT NULL COMMENT 'API密钥',
  `completions_path` varchar(100) DEFAULT '/chat/completions' COMMENT '完成接口路径',
  `is_active` tinyint(1) DEFAULT '0' COMMENT '是否为当前激活模型（0:否，1:是）',
  `enable_thinking` tinyint(1) DEFAULT '0' COMMENT '是否启用深度思考功能（0:否，1:是）仅部分模型支持',
  `description` varchar(255) DEFAULT NULL COMMENT '模型描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_model_name` (`model_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';

-- 示例：添加一个支持深度思考的 Gemini 模型配置
-- INSERT INTO `ai_model_config` (`model_name`, `base_url`, `api_key`, `completions_path`, `is_active`, `enable_thinking`, `description`)
-- VALUES ('gemini-2.0-flash-thinking-exp', 'https://generativelanguage.googleapis.com/v1beta', 'your-api-key', '/chat/completions', 0, 1, 'Gemini 2.0 Flash Thinking - 支持深度推理');
