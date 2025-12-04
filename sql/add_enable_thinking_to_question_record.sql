-- 为 question_generation_record 表添加 enable_thinking 字段
-- 用于记录该次出题是否启用了深度思考功能

ALTER TABLE `question_generation_record`
    ADD COLUMN `enable_thinking` TINYINT(1) DEFAULT 0 COMMENT '是否启用深度思考（0:否，1:是）' AFTER `quantity`;

-- 更新现有数据：默认都是未启用深度思考
-- 新数据会通过应用程序设置此字段
