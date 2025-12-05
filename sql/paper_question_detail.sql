CREATE TABLE IF NOT EXISTS `paper_question_detail` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `paper_id` BIGINT NOT NULL COMMENT '套卷ID',
    `question_type` VARCHAR(50) NOT NULL COMMENT '题型编码',
    `question_type_name` VARCHAR(50) NOT NULL COMMENT '题型名称',
    `difficulty` VARCHAR(20) NOT NULL COMMENT '难度（simple/medium/hard）',
    `quantity` INT NOT NULL COMMENT '题目数量',
    `theme` VARCHAR(100) COMMENT '主题（可选）',
    `generated_questions` JSON NOT NULL COMMENT '生成的题目内容（JSON格式）',
    `display_order` INT NOT NULL COMMENT '显示顺序',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    PRIMARY KEY (`id`),
    FOREIGN KEY (`paper_id`) REFERENCES `paper_generation_record`(`id`) ON DELETE CASCADE,
    INDEX `idx_paper_order` (`paper_id`, `display_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套卷题目明细表 - 存储每个题型的具体题目内容';
