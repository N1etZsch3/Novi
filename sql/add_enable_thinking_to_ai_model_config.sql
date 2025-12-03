-- 为 ai_model_config 表添加 enable_thinking 字段
-- 用于控制该模型是否启用深度思考功能（仅部分模型支持，如 Gemini 2.0 Flash Thinking）

ALTER TABLE `ai_model_config`
    ADD COLUMN `enable_thinking` TINYINT(1) DEFAULT 0 COMMENT '是否启用深度思考功能（0:否，1:是）仅部分模型支持' AFTER `is_active`;

-- 更新现有数据：豆包模型不支持深度思考，保持为 0
-- 如果你有 Gemini 2.0 Flash Thinking 模型，可以手动将其设置为 1

-- 示例：添加一个支持深度思考的 Gemini 模型配置
-- INSERT INTO `ai_model_config` (`model_name`, `base_url`, `api_key`, `completions_path`, `is_active`, `enable_thinking`, `description`)
-- VALUES ('gemini-2.0-flash-thinking-exp', 'https://generativelanguage.googleapis.com/v1beta', 'your-api-key', '/chat/completions', 0, 1, 'Gemini 2.0 Flash Thinking - 支持深度推理');
