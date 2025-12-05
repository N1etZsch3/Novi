-- 为 question_category 表添加 generation_count 字段
-- 用于配置自动组卷时该题型的生成次数

ALTER TABLE question_category 
ADD COLUMN generation_count INT DEFAULT 1 
COMMENT '自动组卷时的生成次数（如阅读理解需要2篇文章）';

-- 为阅读理解题型设置生成2次
UPDATE question_category 
SET generation_count = 2 
WHERE code IN ('reading_cloze', 'reading_qa') 
  AND parent_id = 1  -- 湖北专升本英语
  AND category_type = 2;

-- 验证配置
SELECT id, name, code, generation_count, parent_id
FROM question_category 
WHERE parent_id = 1 AND category_type = 2
ORDER BY sort_order;
