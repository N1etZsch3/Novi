CREATE TABLE user_account (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              username VARCHAR(50) NOT NULL UNIQUE,          -- 登录名
                              hashed_password VARCHAR(255) NOT NULL,         -- 加密后的密码
                              nickname VARCHAR(50),                           -- Novi 称呼用户的昵称
                              email VARCHAR(100) UNIQUE,                     -- 邮箱，用于登录或通知
                              preferences JSON,                              -- 存储用户的个性化偏好
    -- (例如: {"personality": "witty", "voice": "female_A"})
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 更新时间
);