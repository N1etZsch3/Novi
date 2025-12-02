-- ==========================================
-- 1. 修复与补全 question_category
-- ==========================================

-- 确保科目存在
INSERT IGNORE INTO `question_category` (`name`, `code`, `category_type`, `parent_id`, `sort_order`) VALUES 
('湖北专升本英语', 'english_hubei', 1, 0, 1);

-- 获取科目ID (假设为1，但在脚本中最好动态获取或硬编码如果确定)
-- 这里我们假设 english_hubei 的 ID 是 1 (根据之前的 sql 文件)

-- 修复 'prompt.txt' 为 '连词成句'
UPDATE `question_category` SET `name` = '连词成句', `code` = 'sentence_ordering' WHERE `code` = 'prompt.txt';
UPDATE `question_category` SET `name` = '连词成句', `code` = 'sentence_ordering' WHERE `name` = 'prompt.txt';

-- 确保其他题型存在
INSERT IGNORE INTO `question_category` (`name`, `code`, `category_type`, `parent_id`, `sort_order`) VALUES 
('语法填空', 'grammar_fill_blank', 2, 1, 1),
('连词成句', 'sentence_ordering', 2, 1, 2),
('阅读理解-问答', 'reading_qa', 2, 1, 3),
('翻译', 'translation', 2, 1, 4),
('书面表达', 'writing', 2, 1, 5);

-- ==========================================
-- 2. 配置难度描述 (ai_prompt_config)
-- ==========================================

-- 2.1 连词成句 (User Provided)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('desc:difficulty:english_hubei:sentence_ordering:simple', '句长 5-8 词，简单句结构（主谓宾），无复杂从句。', '连词成句-简单', 4),
('desc:difficulty:english_hubei:sentence_ordering:medium', '句长 8-15 词，包含定语从句、状语从句或非谓语动词，逻辑清晰。', '连词成句-中等', 4),
('desc:difficulty:english_hubei:sentence_ordering:hard', '句长 15-20 词，包含倒装、强调、虚拟语气或多重从句嵌套。', '连词成句-困难', 4)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), config_type = VALUES(config_type);

-- 2.2 翻译 (User Provided)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('desc:difficulty:english_hubei:translation:simple', '考察基础词汇搭配（如 look at, wait for），时态为一般现在/过去时。', '翻译-简单', 4),
('desc:difficulty:english_hubei:translation:medium', '考察固定句型（如 It is... that...）或常用短语，涉及完成时或被动语态。', '翻译-中等', 4),
('desc:difficulty:english_hubei:translation:hard', '考察虚拟语气、倒装句或生僻习语搭配，要求精准的词性转换。', '翻译-困难', 4)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), config_type = VALUES(config_type);

-- 2.3 语法填空 (Migrated from Java Code)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('desc:difficulty:english_hubei:grammar_fill_blank:simple', '难度为simple，词汇量控制在 2500 词以内，句子结构简单，符合专升本英语基础水平。', '语法填空-简单', 4),
('desc:difficulty:english_hubei:grammar_fill_blank:medium', '难度为medium，词汇量控制在 4000 词以内，词汇范围大概是高考英语词汇范围，句子结构适中，大概十五句话，可以有几句长难句，符合专升本学生标准水平。', '语法填空-中等', 4),
('desc:difficulty:english_hubei:grammar_fill_blank:hard', '难度为hard，词汇量控制在 4500 词以内，增加长难句和生僻词，20+句话，考察综合语言运用能力，难度高于专升本平均水平。', '语法填空-困难', 4)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), config_type = VALUES(config_type);

-- ==========================================
-- 3. 配置提示词模板 (ai_prompt_config)
-- ==========================================

-- 3.1 语法填空 (Existing)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('prompt:english_hubei:grammar_fill_blank', 
'# Role
你是一位专业的{subject}出题专家。

# Task
请根据以下要求生成{quantity}道{difficulty}难度的【语法填空】题目。
题目主题：{theme}

# Constraints
1. 文章难度：{difficulty}
2. 题型结构：包含一篇约200-250词的短文，必须包含10个挖空处(1-10)。
3. 挖空类型：混合给词填空和盲填。
4. 格式要求：
   - 严格按照JSON数组格式输出。
   - 包含字段：content, answer, analysis, difficulty, type。
   - type字段固定为"语法填空"。

# Examples
{examples}', 
'语法填空出题模板', 3)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 3.2 连词成句 (New Placeholder)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('prompt:english_hubei:sentence_ordering', 
'# Role
你是一位专业的{subject}出题专家。

# Task
请生成{quantity}道【连词成句】题目。
题目主题：{theme}
难度描述：{difficulty}

# Constraints
1. 每道题提供一组打乱顺序的单词或短语。
2. 要求学生将其连成一个通顺、符合语法的句子。
3. 格式要求：
   - 严格按照JSON数组格式输出。
   - content: 包含打乱的词组（用斜杠/分隔）和题目说明。
   - answer: 正确的完整句子。
   - analysis: 句法分析和翻译。
   - type: "连词成句"。

# Examples
{examples}', 
'连词成句出题模板', 3)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 3.3 翻译 (New Placeholder)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('prompt:english_hubei:translation', 
'# Role
你是一位专业的{subject}出题专家。

# Task
请生成{quantity}道【翻译】题目（英译汉或汉译英）。
题目主题：{theme}
难度描述：{difficulty}

# Constraints
1. 题目应考察核心词汇和句型。
2. 格式要求：
   - 严格按照JSON数组格式输出。
   - content: 原句。
   - answer: 参考译文。
   - analysis: 重点词汇和句型解析。
   - type: "翻译"。

# Examples
{examples}', 
'翻译出题模板', 3)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);
