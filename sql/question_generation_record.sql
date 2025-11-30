CREATE TABLE question_generation_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    subject VARCHAR(50) NOT NULL COMMENT '科目',
    question_type VARCHAR(50) NOT NULL COMMENT '题型',
    theme VARCHAR(100) COMMENT '主题（部分题型需要）',
    difficulty VARCHAR(20) NOT NULL COMMENT '难度（simple/medium/hard）',
    quantity INT NOT NULL COMMENT '题目数量',
    generated_questions JSON NOT NULL COMMENT 'AI生成的题目内容（JSON格式）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
    INDEX idx_user_created (user_id, created_at DESC) -- 优化查询用户历史记录的索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出题历史记录表 - 存储用户的AI出题记录';
