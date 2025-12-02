# Novi API 接口汇总文档

## 📋 接口概览

本文档汇总了 Novi 项目的所有 REST API 接口。

## 🔐 认证说明

大部分接口需要携带 JWT Token 进行认证：

```
Authorization: Bearer <your-jwt-token>
```

## 📡 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 1️⃣ 用户账号管理 API

**Base Path**: `/api/v1/users`

| 方法 | 路径 | 功能 | 是否需要认证 |
|------|------|------|-------------|
| POST | `/register` | 用户注册 | ❌ |
| POST | `/login` | 用户登录 | ❌ |
| GET | `/me` | 获取用户信息 | ✅ |
| PUT | `/me` | 更新用户信息 | ✅ |
| GET | `/preferences` | 获取用户偏好(Map) | ✅ |
| PUT | `/preferences` | 更新用户偏好(Map) | ✅ |

### 1.1 用户注册

```http
POST /api/v1/users/register
Content-Type: application/json

{
  "username": "user123",
  "password": "password123",
  "email": "user@example.com",
  "nickname": "小明"
}

> [!WARNING]
> 内部测试期间，暂不开放注册。请联系管理员获取账号。

```

### 1.2 用户登录

```http
POST /api/v1/users/login
Content-Type: application/json

{
  "username": "user123",
  "password": "password123"
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userId": 1,
    "username": "user123"
  }
}
```

## 2️⃣ AI 聊天 API

**Base Path**: `/api/v1/chat`

| 方法 | 路径 | 功能 | 响应类型 |
|------|------|------|---------|
| POST | `/send/call` | 发送消息（阻塞式） | JSON |
| POST | `/send/stream` | 发送消息（流式） | SSE |

### 2.1 阻塞式聊天

```http
POST /api/v1/chat/send/call
Authorization: Bearer <token>
Content-Type: application/json

{
  "sessionId": "session-uuid-123",
  "message": "你好，Novi！"
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "sessionId": "session-uuid-123",
    "content": "你好！很高兴见到你～",
    "timestamp": 1732851234000
  }
}
```

### 2.2 流式聊天

```http
POST /api/v1/chat/send/stream
Authorization: Bearer <token>
Content-Type: application/json
Accept: text/event-stream

{
  "sessionId": null,
  "message": "介绍一下你自己"
}

Response (SSE):
data: {"type":"content","data":"我"}
data: {"type":"content","data":"是"}
data: {"type":"content","data":"Novi"}
data: {"type":"done","sessionId":"new-session-id"}
```

## 3️⃣ 会话管理 API

**Base Path**: `/api/v1/sessions`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/` | 获取用户会话列表 |
| GET | `/{sessionId}/messages` | 获取会话消息历史 |
| DELETE | `/{sessionId}` | 删除会话 |

### 3.1 获取会话列表

```http
GET /api/v1/sessions
Authorization: Bearer <token>

Response:
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "sessionId": "session-123",
      "sessionTitle": "今天天气真不错...",
      "createdAt": "2025-11-29T10:00:00",
      "lastActiveTime": "2025-11-29T12:30:00"
    }
  ]
}
```

### 3.2 获取会话消息

```http
GET /api/v1/sessions/session-123/messages
Authorization: Bearer <token>

Response:
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "role": "user",
      "content": "你好",
      "timestamp": "2025-11-29T10:00:00"
    },
    {
      "id": 2,
      "role": "assistant",
      "content": "你好！",
      "timestamp": "2025-11-29T10:00:05"
    }
  ]
}
```

## 4️⃣ AI 模型配置 API

**Base Path**: `/api/model/config`

> [!IMPORTANT]
> 为了安全考虑，所有API仅返回安全的DTO数据，**不包含API Key等敏感信息**。
> 模型的增删改操作已移除，仅保留查询和切换功能。

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/list` | 获取所有模型配置 | 返回安全DTO |
| GET | `/active` | 获取当前激活模型 | 返回安全DTO |
| POST | `/switch/{modelName}` | 切换模型 | 使用模型名称 |

### 4.1 获取所有模型

```http
GET /api/model/config/list
Authorization: Bearer <token>

Response:
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "modelName": "doubao-seed-1-6-flash-250828",
      "description": "豆包模型",
      "isActive": true
    },
    {
      "id": 2,
      "modelName": "gpt-3.5-turbo",
      "description": "OpenAI GPT-3.5",
      "isActive": false
    }
  ]
}
```

> [!NOTE]
> 返回的数据中**不包含** `baseUrl`、`apiKey`、`completionsPath` 等敏感配置信息。

### 4.2 切换模型

```http
POST /api/model/config/switch/doubao-seed-1-6-flash-250828
Authorization: Bearer <token>

Response:
{
  "code": 200,
  "message": "success",
  "data": "模型切换成功"
}
```

**错误响应**：
```http
{
  "code": 500,
  "message": "error",
  "data": "模型切换失败，模型不存在"
}
```

> [!WARNING]
> **模型的增删改接口已移除**
> 
> 模型配置应由系统管理员直接在数据库中管理。前端用户只能查看模型列表和执行切换操作。


## 5️⃣ AI 提示词配置 API

**Base Path**: `/api/prompt/config`

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/` | 添加新配置 |
| DELETE | `/{key}` | 删除配置 |
| GET | `/type/{type}` | 根据类型列出配置 |

**类型说明**：
- `0` - 系统提示词
- `1` - 性格
- `2` - 语气风格

### 5.1 添加配置

```http
POST /api/prompt/config
Authorization: Bearer <token>
Content-Type: application/json

{
  "configKey": "gentle",
  "configValue": "你说话温柔体贴，善于倾听...",
  "configType": 1,
  "description": "温柔性格"
}
```

### 5.2 按类型查询

```http
GET /api/prompt/config/type/1
Authorization: Bearer <token>

Response:
{
  "code": 200,
  "data": [
    {
      "configKey": "gentle",
      "configValue": "你说话温柔体贴...",
      "configType": 1,
      "description": "温柔性格",
      "createTime": "2025-11-29T10:00:00",
      "updateTime": "2025-11-29T10:00:00"
    }
  ]
}
```

## 6️⃣ 用户偏好设置 API

**Base Path**: `/api/v1/preferences`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/` | 获取用户偏好 |
| POST | `/` | 保存用户偏好 |
| PUT | `/` | 更新用户偏好 |

### 6.1 保存用户偏好

```http
POST /api/v1/preferences
Authorization: Bearer <token>
Content-Type: application/json

{
  "personalityKey": "gentle",
  "toneStyleKey": "casual"
}


```

## 7️⃣ AI 出题 API

**Base Path**: `/api/v1/questions`

| 方法   | 路径                  | 功能         |
| ------ | --------------------- | ------------ |
| POST   | `/generate`           | 生成题目     |
| GET    | `/history`            | 获取出题历史 |
| GET    | `/history/{recordId}` | 获取记录详情 |
| DELETE | `/history/{recordId}` | 删除记录     |
| DELETE | `/history`            | 批量删除记录 |

### 7.1 生成题目

```http
POST /api/v1/questions/generate
Authorization: Bearer <token>
Content-Type: application/json

{
  "topic": "Java基础",
  "difficulty": "medium",
  "count": 5
}
```

### 7.2 获取历史记录

```http
GET /api/v1/questions/history
Authorization: Bearer <token>

Response:
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "topic": "Java基础",
      "createdAt": "2025-11-29T14:00:00"
    }
  ]
}
```
## 🔍 错误码说明

| 错误码 | 说明              |
| ------ | ----------------- |
| 200    | 成功              |
| 400    | 请求参数错误      |
| 401    | 未认证或Token无效 |
| 403    | 无权限            |
| 404    | 资源不存在        |
| 500    | 服务器内部错误    |

## 📝 请求示例（JavaScript）

### 使用 Fetch API

```javascript
// 登录
async function login(username, password) {
  const response = await fetch('/api/v1/users/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  const data = await response.json();
  return data.data.token;
}

// 发送消息
async function sendMessage(token, sessionId, message) {
  const response = await fetch('/api/v1/chat/send/call', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ sessionId, message })
  });
  return await response.json();
}

// 流式聊天
function sendStreamMessage(token, sessionId, message) {
  const eventSource = new EventSource(
    '/api/v1/chat/send/stream',
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  
  eventSource.onmessage = (event) => {
    const data = JSON.parse(event.data);
    if (data.type === 'content') {
      console.log(data.data);
    } else if (data.type === 'done') {
      eventSource.close();
    }
  };
}
```

## 📚 相关文档

- [AI聊天功能模块](file:///C:/Users/35666/.gemini/antigravity/brain/774ebe23-99e1-46d9-a3e1-52263e77b58e/AI聊天功能模块.md)
- [会话管理模块](file:///C:/Users/35666/.gemini/antigravity/brain/774ebe23-99e1-46d9-a3e1-52263e77b58e/会话管理模块.md)
- [AI模型配置模块](file:///C:/Users/35666/.gemini/antigravity/brain/774ebe23-99e1-46d9-a3e1-52263e77b58e/AI模型配置模块.md)
