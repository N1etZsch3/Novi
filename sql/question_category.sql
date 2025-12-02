CREATE TABLE IF NOT EXISTS `question_category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(50) NOT NULL COMMENT '分类名称',
    `code` varchar(50) NOT NULL COMMENT '分类编码',
    `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父级ID',
    `category_type` int NOT NULL COMMENT '类型 1:科目 2:题型 3:套卷',
    `sort_order` int DEFAULT '0' COMMENT '排序',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目分类表';

-- 初始化数据
INSERT INTO `question_category` (`id`, `name`, `code`, `parent_id`, `category_type`, `sort_order`) VALUES
(1, '湖北专升本英语', 'english_hubei', 0, 1, 1),
-- 英语题型 (ID 从 100 开始，预留 1-99 给科目)
(101, '语法填空', 'grammar_fill_blank', 1, 2, 1),
(102, 'prompt.txt', 'sentence_ordering', 1, 2, 2),
(103, '阅读理解-问答', 'reading_qa', 1, 2, 3),
(104, '翻译', 'translation', 1, 2, 4),
(105, '书面表达', 'writing', 1, 2, 5);
