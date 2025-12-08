# AI 出题功能模块文档

## 📋 功能概述

AI 出题模块允许用户根据指定的科目、题型、难度和数量生成个性化的练习题。利用大型语言模型（LLM）的生成能力，结合预设的题目结构 (Schema)，生成结构化（JSON）的题目数据。

## 🎯 核心特性

- **多维度定制**：支持选择科目、题型、难度、数量和主题。
- **结构化输出**：AI 严格按照定义的 JSON Schema 生成题目，便于前端渲染。
- **深度思考支持**：可选开启 "Deep Thinking" 模式，提升题目生成的逻辑性和质量。
- **Few-Shot Learning**：基于 `question_example` 表中的示例进行少样本学习，确保生成风格一致。
- **历史记录**：自动保存用户的出题记录和生成的题目内容。

## 🔌 API 接口

### 生成题目

**接口路径**：`POST /api/v1/questions/generate`

**请求参数**：
```json
{
  "subject": "湖北专升本英语",
  "questionType": "语法填空",
  "difficulty": "medium",
  "quantity": 3,
  "theme": "科技与生活",
  "enableThinking": true
}
```

**响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 101, // 记录ID
    "generatedQuestions": "JSON String of questions..."
  }
}
```

## 📊 业务流程

```mermaid
graph TD
    A[用户提交出题请求] --> B[Controller 接收请求]
    B --> C[Service 处理业务逻辑]
    C --> D[加载 Prompt 模板]
    D --> E[查询 Few-Shot 示例]
    E --> F[组装完整 Prompt]
    F --> G[调用 AI 模型 (call/stream)]
    G --> H[解析 AI 返回的 JSON]
    H --> I[保存记录到数据库]
    I --> J[返回结果给前端]
```

## 🧩 核心组件

### Controller 层

**文件**：`QuestionGenerationController.java`

**主要职责**：
- 接收并校验前端的 `QuestionGenerationRequest`。
- 调用 Service 层执行生成逻辑。
- 处理出题历史的查询和删除。

### Service 层

**文件**：`QuestionGenerationService.java`

**主要职责**：
- **Prompt 组装**：结合系统提示词、用户请求参数和数据库中的示例题目。
- **AI 调用**：根据配置选择是否启用深度思考 (Deep Thinking) 模型。
- **数据持久化**：将生成的题目和请求参数保存到 `question_generation_record` 表。

### 题目分类管理

**文件**：`QuestionCategoryService.java`

**主要职责**：
- 管理科目、题型和套卷的层级关系。
- 提供前端所需的分类树和选项列表。

## 💾 数据模型

### QuestionGenerationRecord (出题记录)

```java
@TableName("question_generation_record")
public class QuestionGenerationRecord {
    private Long id;
    private Long userId;
    private String subject;        // 科目
    private String questionType;   // 题型
    private String theme;          // 主题
    private String difficulty;     // 难度
    private Integer quantity;      // 数量
    private String generatedQuestions; // 生成的题目JSON (存储为字符串)
    private LocalDateTime createdAt;
}
```

### QuestionCategory (题目分类)

```java
@TableName("question_category")
public class QuestionCategory {
    private Long id;
    private String name;           // 名称 (如：湖北专升本英语)
    private String code;           // 编码 (如：english_hubei)
    private Long parentId;         // 父级ID
    private Integer categoryType;  // 1:科目, 2:题型, 3:套卷
    // ...
}
```

## 🎨 Prompt 设计

出题 Prompt 通常包含以下部分：

1.  **角色设定**：你是一个专业的 {subject} 出题专家。
2.  **任务描述**：请生成 {quantity} 道 {difficulty} 难度的 {questionType} 题目。
3.  **格式约束**：必须严格按照以下 JSON 格式输出，不要包含 Markdown 标记。
4.  **示例学习 (Few-Shot)**：以下是该题型的标准示例：{example_json}。
5.  **主题要求**：题目内容需围绕 "{theme}" 展开。

## 📚 相关文档

- [套卷生成功能模块](AI套卷功能模块.md)
- [API 接口文档 - AI出题](../04-API文档/接口文档/1.7-AI出题.md)
