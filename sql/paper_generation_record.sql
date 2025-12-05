CREATE TABLE IF NOT EXISTS `paper_generation_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `subject_id` BIGINT NOT NULL COMMENT '科目ID',
    `paper_name` VARCHAR(100) NOT NULL COMMENT '套卷名称',
    `total_questions` INT NOT NULL COMMENT '总题目数量',
    `enable_thinking` TINYINT(1) DEFAULT 0 COMMENT '是否启用深度思考',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user_account`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`subject_id`) REFERENCES `question_category`(`id`),
    INDEX `idx_user_created` (`user_id`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套卷生成记录表 - 存储用户生成的整套试卷';
