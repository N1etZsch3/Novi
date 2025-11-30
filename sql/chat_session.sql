CREATE TABLE chat_session (
                              id VARCHAR(100) PRIMARY KEY,        -- session_id (与 chat_message 中的 session_id 对应)
                              user_id BIGINT NOT NULL,            -- 所属用户
                              title VARCHAR(255),                 -- 会话标题 (例如: "帮我写个Java代码")
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 最后活跃时间 (用于排序)
                              is_deleted TINYINT(1) DEFAULT 0,    -- 软删除标记

                              FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
                              INDEX idx_user_updated (user_id, updated_at) -- 优化查询列表的索引
);