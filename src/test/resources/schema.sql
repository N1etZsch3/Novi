-- UserAccount
CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    hashed_password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    preferences TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ChatSession
CREATE TABLE IF NOT EXISTS chat_session (
    id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- ChatMessage
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- AiPromptConfig
CREATE TABLE IF NOT EXISTS ai_prompt_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT,
    description VARCHAR(255),
    config_type INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- UserMemory
CREATE TABLE IF NOT EXISTS user_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fact_key VARCHAR(100),
    fact_value TEXT,
    fact_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP,
    user_id BIGINT NOT NULL,
    source_message_id BIGINT
);
