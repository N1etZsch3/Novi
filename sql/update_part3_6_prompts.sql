-- 1. 确保题型分类存在 (INSERT IGNORE)
-- 假设科目代码为 english_hubei
INSERT IGNORE INTO question_category (name, code, category_type, parent_id, sort_order)
SELECT '阅读理解-填空', 'reading_gap_filling', 2, id, 30 FROM question_category WHERE code = 'english_hubei';

INSERT IGNORE INTO question_category (name, code, category_type, parent_id, sort_order)
SELECT '阅读理解-问答', 'reading_qa', 2, id, 40 FROM question_category WHERE code = 'english_hubei';

INSERT IGNORE INTO question_category (name, code, category_type, parent_id, sort_order)
SELECT '翻译', 'translation', 2, id, 50 FROM question_category WHERE code = 'english_hubei';

INSERT IGNORE INTO question_category (name, code, category_type, parent_id, sort_order)
SELECT '书面表达', 'writing', 2, id, 60 FROM question_category WHERE code = 'english_hubei';


-- 2. 配置难度描述 (Config Type 4)

-- 2.1 阅读理解-填空 (Reading Gap Filling)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('desc:difficulty:english_hubei:reading_gap_filling:simple', '文章约200词，生词少，逻辑结构清晰，填空词多为高频实词。', '阅读填空-简单', 4),
('desc:difficulty:english_hubei:reading_gap_filling:medium', '文章约250词，包含少量长难句，填空需结合上下文推断，涉及词形变换。', '阅读填空-中等', 4),
('desc:difficulty:english_hubei:reading_gap_filling:hard', '文章约300词，题材抽象，句式复杂，填空涉及熟词生义或复杂语法结构。', '阅读填空-困难', 4)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), description = VALUES(description);

-- 2.2 阅读理解-问答 (Reading Q&A)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('desc:difficulty:english_hubei:reading_qa:simple', '文章通俗易懂，问题多为细节题，可直接在文中找到答案，翻译句结构简单。', '阅读问答-简单', 4),
('desc:difficulty:english_hubei:reading_qa:medium', '文章有一定深度，问题需简单归纳，翻译句包含定语从句或状语从句。', '阅读问答-中等', 4),
('desc:difficulty:english_hubei:reading_qa:hard', '文章逻辑复杂，问题需综合推理，翻译句包含倒装、虚拟语气等高级语法。', '阅读问答-困难', 4)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), description = VALUES(description);

-- 2.3 翻译 (Translation)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('desc:difficulty:english_hubei:translation:simple', '基础主谓宾结构，考察常用短语搭配，无复杂从句。', '翻译-简单', 4),
('desc:difficulty:english_hubei:translation:medium', '包含定语从句、状语从句或宾语从句，考察时态和语态的综合运用。', '翻译-中等', 4),
('desc:difficulty:english_hubei:translation:hard', '包含倒装句、强调句、独立主格或虚拟语气，考察成语或习语的翻译。', '翻译-困难', 4)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), description = VALUES(description);

-- 2.4 书面表达 (Writing)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('desc:difficulty:english_hubei:writing:simple', '日常应用文（如请假条、通知），格式固定，要点明确，词汇基础。', '书面表达-简单', 4),
('desc:difficulty:english_hubei:writing:medium', '正式信函（如建议信、求职信），要求逻辑清晰，用词准确，句式有一定变化。', '书面表达-中等', 4),
('desc:difficulty:english_hubei:writing:hard', '议论文或复杂情景应用文，要求观点鲜明，论证充分，使用高级词汇和多样化句式。', '书面表达-困难', 4)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), description = VALUES(description);


-- 3. 配置提示词模板 (Config Type 3)

-- 3.1 阅读理解-填空 (Reading Gap Filling)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('prompt:english_hubei:reading_gap_filling', 
'# Role
你是一位资深的英语命题专家。

# Task
请根据主题【{theme}】，编写 1 篇 "Part III 阅读理解（填空）" 大题。

# Constraints & Rules
1.  **文章素材**: 生成一篇约 250-300 词的短文，难度 {difficulty}。
2.  **设题要求**:
    * 在文章下方提供 5 个填空题（序号 17-21）。
    * 题目形式为：Summaries（摘要句）或 Information Matching（信息匹配）。
    * **挖空原则**：答案必须是文章中的原词，或者是文章信息的简单同义转换。**每空限填 1 个单词**。
3.  **内容对应**: 题目顺序应与文章段落顺序大致一致。

# Output Format (Standard JSON Protocol)
请严格按照以下 **标准 UI 结构** 输出 JSON 数组。
**严禁**包含任何 Markdown 标记。

JSON 结构示例：
[
  {
    "ui_type": "GROUP",
    "stem": "Part III 阅读理解（填空）<br>阅读下面短文，在空白处填入适当的单词（限填一词）。",
    "content": "The Dong Grand Choir is a unique musical tradition... (此处为完整文章内容)",
    "children": [
      {
        "ui_type": "INPUT",
        "stem": "17",
        "content": "Type of Tradition: The Dong Grand Choir is a unique ____ tradition.",
        "answer": "musical",
        "analysis": "根据第一段第一句... unique musical tradition..."
      },
      {
        "ui_type": "INPUT",
        "stem": "18",
        "content": "Unique Feature: Unlike most choirs, it has no ____.",
        "answer": "conductor",
        "analysis": "根据第二段... no conductor..."
      }
      // ... 必须包含全部 5 个小题 ...
    ],
    "meta": {
      "difficulty": "medium"
    }
  }
]', 
'阅读理解-填空出题模板', 3)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 3.2 阅读理解-问答 (Reading Q&A)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('prompt:english_hubei:reading_qa', 
'# Role
你是一位资深的英语命题专家。

# Task
请根据主题【{theme}】，编写 1 篇 "Part IV 阅读理解（问答）" 大题。

# Constraints & Rules
1.  **文章素材**: 生成一篇约 300 词的记叙文或应用文（如信件），难度 {difficulty}。
2.  **设题要求 (共5小题)**:
    * 前 4 题 (例如 27-30)：针对文章细节的特殊疑问句 (What/Where/Why/How)。答案应简洁。
    * 第 5 题 (例如 31)：**必须是翻译题**。选取文中一句长难句，要求"Translate the sentence ''...'' into Chinese."。
3.  **答案标准**: 问答题提供参考答案；翻译题提供标准译文。

# Output Format (Standard JSON Protocol)
请严格按照以下 **标准 UI 结构** 输出 JSON 数组。
**严禁**包含任何 Markdown 标记。

JSON 结构示例：
[
  {
    "ui_type": "GROUP",
    "stem": "Part IV 阅读理解（问答）<br>阅读下面短文，回答问题或将句子翻译成中文。",
    "content": "Anna and her friends decided to take a journey... (此处为完整文章内容)",
    "children": [
      {
        "ui_type": "INPUT",
        "stem": "27",
        "content": "What did Anna and her friends pack for their boat journey?",
        "answer": "Food, drinks, and blankets.",
        "analysis": "细节题。定位于第一段第二句..."
      },
      // ... 中间题目 ...
      {
        "ui_type": "INPUT",
        "stem": "31",
        "content": "Translate the sentence “Afterwards, they continued their journey...” into Chinese.",
        "answer": "后来，她们继续旅程，发现了新的景色。",
        "analysis": "翻译题。考查 afterwards (后来) 和 discover (发现) 的翻译。"
      }
    ],
    "meta": {
      "difficulty": "medium"
    }
  }
]', 
'阅读理解-问答出题模板', 3)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 3.3 翻译 (Translation)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('prompt:english_hubei:translation', 
'# Role
你是一位湖北省普通专升本英语考试命题专家。

# Task
请根据主题【{theme}】或通用考点，编写 {quantity} 道 "Part V 翻译" 题目。

# Constraints & Rules
1.  **难度**: {difficulty}。考察核心词汇搭配和基础语法。
2.  **题型结构**:
    * 给出中文句子。
    * 给出对应的英文句子，留出 1-2 个空。
    * 句末括号内提供一个 **Keyword (提示词)**。
    * **核心限制**：考生需要用提示词的正确形式填空，**每题答案不能超过 2 个单词**。

# Output Format (Standard JSON Protocol)
请严格按照以下 **标准 UI 结构** 输出 JSON 数组。
**严禁**包含任何 Markdown 标记。

JSON 结构示例：
[
  {
    "ui_type": "INPUT",
    "stem": "37",
    "content": "如果下雪，他们会取消活动。<br>They will cancel the event if ____. (snow)", 
    "answer": "it snows",
    "analysis": "考查if引导的条件状语从句，主将从现。snow作动词，主语it为三单。",
    "meta": { "difficulty": "medium" }
  },
  {
    "ui_type": "INPUT",
    "stem": "38",
    "content": "你能保持安静吗？我需要集中精力。<br>Can you ____? I need to concentrate. (quiet)",
    "answer": "keep quiet",
    "analysis": "考查固定搭配 keep quiet。",
    "meta": { "difficulty": "easy" }
  }
]', 
'翻译出题模板', 3)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 3.4 书面表达 (Writing)
INSERT INTO ai_prompt_config (config_key, config_value, description, config_type) VALUES 
('prompt:english_hubei:writing', 
'# Role
你是一位湖北省普通专升本英语考试命题专家。

# Task
请根据主题【{theme}】，编写 1 道 "Part VI 书面表达" 题目。

# Constraints & Rules
1.  **体裁**: 应用文（邮件、信函、通知、邀请函等）。
2.  **情景设定**: 设定一个具体的交际场景。
3.  **角色**: 假设考生是“李华”。
4.  **内容要点**: 必须列出 3 个具体的写作要点（Requirements）。
5.  **字数要求**: 不少于 100 词。

# Output Format (Standard JSON Protocol)
请严格按照以下 **标准 UI 结构** 输出 JSON 数组。
**严禁**包含任何 Markdown 标记。

JSON 结构示例：
[
  {
    "ui_type": "INPUT",
    "stem": "Part VI 书面表达",
    "content": "<strong>题目：</strong>国外某艺术团希望参加2025年在东湖举行的音乐节。假设你是组委会的工作人员李华，请你用英语给他们发一封邮件。<br><br><strong>内容包括：</strong><br>1. 表示欢迎；<br>2. 活动将于7月18号至20日举行；<br>3. 请他们准备2-3个节目参加表演。<br><br><small class=''text-muted''>注意：词数不少于100词。</small>",
    "answer": "Dear Sir/Madam,\n\nI am Li Hua... (此处生成范文)",
    "analysis": "本题考查公务邮件写作。注意语气礼貌、信息准确，涵盖所有要点。",
    "meta": {
      "difficulty": "medium"
    }
  }
]', 
'书面表达出题模板', 3)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);
