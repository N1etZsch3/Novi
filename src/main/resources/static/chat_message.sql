CREATE TABLE chat_message (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL,                       -- 关联的用户ID
                              session_id VARCHAR(100) NOT NULL,              -- 唯一会话ID（用于区分不同轮次的对话）
                              role VARCHAR(20) NOT NULL,                     -- 角色 ("user" 或 "assistant")
                              content TEXT NOT NULL,                         -- 消息的具体内容
                              timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 消息时间戳

                              FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
                              INDEX idx_user_session (user_id, session_id, timestamp) -- 快速检索会话历史
);