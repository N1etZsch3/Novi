CREATE TABLE user_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,                        -- 关联的用户ID
    fact_key VARCHAR(100) NOT NULL,                 -- 事实的"键" (例如: "pet_name", "birthday")
    fact_value TEXT NOT NULL,                       -- 事实的"值" (例如: "旺财", "10-01")
    fact_type VARCHAR(50),                          -- 事实的分类 (例如: "Pet", "Family", "Work", "Preference")
    source_message_id BIGINT,                       -- (可选) 该记忆来源于哪条聊天记录
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 记忆形成时间
    last_accessed_at TIMESTAMP,                     -- 最近一次被Novi"想起"的时间

    FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
    FOREIGN KEY (source_message_id) REFERENCES chat_message(id) ON DELETE SET NULL,
    UNIQUE KEY uk_user_fact (user_id, fact_key)     -- 确保一个用户的一个key是唯一的
);