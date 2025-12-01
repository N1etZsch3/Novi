# AI 聊天功能模块文档

## 📋 功能概述

AI 聊天模块 (`novi-chat`) 是 Novi 项目的核心功能，提供智能对话能力。支持阻塞式和流式两种响应模式，具备上下文记忆、会话管理以及动态系统提示词切换功能。

## 🎯 核心特性

- **双响应模式**：阻塞式（一次性返回）和流式（SSE实时推送）
- **上下文记忆**：基于数据库的持久化聊天记忆
- **会话管理**：自动创建会话、生成标题
- **多轮对话**：AI 能记住之前的对话内容
- **动态提示词**：支持根据上下文（如普通聊天、出题模式）动态切换系统提示词
- **个性化配置**：支持动态调整 AI 性格和语气

## 🔌 API 接口

### 1. 发送消息（阻塞式）

**接口路径**：`POST /api/v1/chat/send/call`

**请求参数**：
```json
{
  "sessionId": "会话ID（可选，新会话传null）",
  "message": "用户消息内容"
}
```

**响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sessionId": "会话ID",
    "content": "AI回复内容",
    "timestamp": 1732851234000
  }
}
```

### 2. 发送消息（流式）

**接口路径**：`POST /api/v1/chat/send/stream`

**请求参数**：同上

**响应格式**：Server-Sent Events (SSE)
```
data: {"type":"content","data":"你"}
data: {"type":"content","data":"好"}
data: {"type":"content","data":"！"}
data: {"type":"done","sessionId":"session-123"}
```

## 📊 业务流程

### 聊天处理流程

```mermaid
graph TD
    A[用户发送消息] --> B{是否存在会话?}
    B -->|否| C[创建新会话]
    B -->|是| D[加载会话上下文]
    C --> E[生成会话标题]
    D --> F[获取用户偏好设置]
    E --> F
    F --> G[确定 PromptContextType]
    G --> H[获取对应 System Prompt]
    H --> I[组装完整Prompt]
    I --> J[调用AI模型]
    J --> K{响应模式?}
    K -->|阻塞式| L[等待完整回复]
    K -->|流式| M[实时推送回复]
    L --> N[保存消息到数据库]
    M --> N
    N --> O[返回响应给用户]
```

## 🧩 核心组件

### Controller 层

**文件**：`ChatController.java` (位于 `novi-web` 模块)

**主要方法**：
- `sendMessage()` - 处理阻塞式聊天请求
- `sendMessageStream()` - 处理流式聊天请求

### Service 层

**文件**：`ChatService.java` 及其实现 (位于 `novi-chat` 模块)

**主要职责**：
- 会话管理（创建、识别、更新）
- 提示词构建（动态系统提示词 + 用户偏好）
- AI 模型调用
- 消息持久化

**关键方法**：
- `handleCallMessage()` - 处理阻塞式消息
- `handleStreamMessage()` - 处理流式消息
- `buildSystemPrompt()` - 构建系统提示词

### Repository 层

**自定义聊天记忆**：`NoviDatabaseChatMemory` (位于 `novi-chat` 模块)

**功能**：
- 实现 Spring AI 的 `ChatMemory` 接口
- 将对话历史存储到 MySQL
- 支持多轮对话上下文

## 💾 数据模型

### ChatSession（会话）

```java
@TableName("chat_session")
public class ChatSession {
    private Long id;              // 会话ID
    private Long userId;          // 用户ID
    private String sessionTitle;  // 会话标题
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveTime;
    // ...
}
```

### ChatMessage（消息）

```java
@TableName("chat_message")
public class ChatMessage {
    private Long id;
    private String sessionId;  // 会话ID
    private Long userId;       // 用户ID
    private String role;       // user/assistant
    private String content;    // 消息内容
    private LocalDateTime timestamp;
    // ...
}
```

## 🔄 动态提示词切换

系统支持根据不同的上下文场景（`PromptContextType`）动态切换系统提示词。

### PromptContextType 枚举

- `CHAT`: 普通聊天模式
- `QUESTION_GENERATION`: 出题模式
- `TRANSLATION`: 翻译模式 (预留)

### 实现机制

1. **配置存储**：`ai_prompt_config` 表存储不同类型的提示词模板。
2. **上下文识别**：Service 层根据业务逻辑确定当前请求的 `PromptContextType`。
3. **动态加载**：通过 `AiPromptConfigService` 获取对应类型的激活提示词。

## 🎨 提示词构建

### 系统提示词结构

```
[动态系统提示词 (基于ContextType)] + [用户偏好设定 (性格/语气)] + [上下文信息]
```

**示例 (普通聊天)**：
```
你是 Novi，一个温暖、善解人意的 AI 伴侣。
性格：温柔体贴
语气：轻松随和
当前时间：2025-11-29 13:24
记住用户之前告诉你的信息...
```

## 📝 使用示例

### 前端调用示例（阻塞式）

```javascript
fetch('/api/v1/chat/send/call', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  },
  body: JSON.stringify({
    sessionId: sessionId || null,
    message: '你好，Novi！'
  })
})
.then(res => res.json())
.then(data => {
  console.log('AI回复:', data.data.content);
});
```

## ⚙️ 配置说明

### AI 模型配置
通过 `novi-ai-config` 模块管理。

### 提示词配置
通过 `ai_prompt_config` 表配置：
- 系统提示词模板
- 提示词类型 (`prompt_type`)
- AI 性格选项
- 语气风格选项

## 📚 相关文档

- [会话管理模块](file:///Users/n1etzsch3/Documents/Novi/docs/03-功能模块/会话管理模块.md)
- [AI提示词管理模块](file:///Users/n1etzsch3/Documents/Novi/docs/03-功能模块/AI提示词管理模块.md)
- [AI模型配置模块](file:///Users/n1etzsch3/Documents/Novi/docs/03-功能模块/AI模型配置模块.md)
