CREATE TABLE `question_generation_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `subject` varchar(50) NOT NULL COMMENT '科目',
  `question_type` varchar(50) NOT NULL COMMENT '题型',
  `theme` varchar(100) DEFAULT NULL COMMENT '主题（部分题型需要）',
  `difficulty` varchar(20) NOT NULL COMMENT '难度（simple/medium/hard）',
  `quantity` int(11) NOT NULL COMMENT '题目数量',
  `enable_thinking` tinyint(1) DEFAULT '0' COMMENT '是否启用深度思考（0:否，1:是）',
  `generated_questions` json NOT NULL COMMENT 'AI生成的题目内容（JSON格式）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目生成记录表';
