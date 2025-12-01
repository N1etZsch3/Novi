# AI 出题功能模块

## 1. 简介
AI 出题功能模块 (`novi-question`) 旨在利用大语言模型 (LLM) 的能力，根据用户指定的科目、题型、难度和数量，自动生成高质量的练习题。

## 2. 核心流程
1. **前端请求**：用户在前端界面选择出题配置（科目、题型、难度、数量等），发送请求到后端。
2. **参数校验**：后端接收请求，校验参数有效性。
3. **Prompt 组装**：根据请求参数，动态组装发送给 AI 的 Prompt，包含具体的出题指令和格式要求（通常要求返回 JSON 格式）。
4. **AI 调用**：通过 `novi-ai-config` 模块提供的接口调用 AI 模型。
5. **结果解析与存储**：接收 AI 返回的 JSON 数据，解析并保存到数据库中，作为出题记录。
6. **响应返回**：将生成的题目数据返回给前端进行渲染。

## 3. 数据结构

### 3.1 请求参数 (QuestionGenerationRequest)

| 字段名 | 类型 | 必填 | 说明 | 示例 |
| :--- | :--- | :--- | :--- | :--- |
| subject | String | 是 | 科目 | "湖北专升本英语" |
| questionType | String | 是 | 题型 | "语法填空" |
| theme | String | 否 | 主题（部分题型需要） | "科技发展" |
| difficulty | String | 是 | 难度 (simple/medium/hard) | "medium" |
| quantity | Integer | 是 | 题目数量 (>=1) | 5 |

### 3.2 响应数据 (QuestionGenerationResponse)

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| recordId | Long | 出题记录 ID，用于后续查询 |
| questions | String | AI 生成的题目内容 (JSON 字符串) |

### 3.3 历史记录 (QuestionHistoryItem)

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Long | 记录ID |
| subject | String | 科目 |
| questionType | String | 题型 |
| difficulty | String | 难度 |
| createTime | LocalDateTime | 创建时间 |
| questionCount | Integer | 题目数量 |

## 4. 数据库设计
主要涉及以下表：
- `question_generation_record`: 存储每次出题的请求参数和生成结果。
- `question_example`: 存储各科目的题目示例，用于 Few-Shot Prompting。

## 5. 依赖模块
- `novi-ai-config`: 用于获取 AI 模型配置和 Prompt 模板。
- `novi-common`: 使用公共的工具类和异常处理。
