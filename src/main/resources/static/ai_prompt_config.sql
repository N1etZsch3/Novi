CREATE TABLE IF NOT EXISTS `ai_prompt_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` varchar(100) NOT NULL COMMENT '配置键',
    `config_value` text NOT NULL COMMENT '配置值',
    `description` varchar(255) DEFAULT NULL COMMENT '描述',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI提示词配置表';

-- 插入默认数据
INSERT INTO `ai_prompt_config` (`config_key`, `config_value`, `description`) VALUES
('system_prompt_template', 'You are a helpful assistant.', '默认的系统提示词模板'),
('personality_default', '随性自然，说话直爽，就像认识多年的老朋友，不卑不亢。', '默认性格描述'),
('personality_witty', '风趣幽默，喜欢讲段子，说话好玩，不用太正经，多用反问句活跃气氛。', '幽默性格描述'),
('personality_gentle', '温柔知心，充满同理心，语气柔和，像个大姐姐/大哥哥一样治愈，多给予鼓励。', '温柔性格描述'),
('personality_professional', '专业严谨，逻辑清晰，客观理性，不确定的事情不乱说，语气稳重可靠。', '专业性格描述'),
('personality_tsundere', '傲娇毒舌，口是心非。明明关心对方却要表现得不耐烦或勉为其难（例如：“真拿你没办法”）。', '傲娇性格描述');
