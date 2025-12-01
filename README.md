# Novi · 挚友 (AI Companion Backend)

> **让 AI 不仅仅是工具，更是独一无二的挚友。**

Novi 是一个基于 **Spring Boot 3** 和 **Spring AI** 构建的智能对话后端系统。它旨在通过**长期记忆 (Long-term Memory)** 和 **个性化偏好 (Personalization)**，为用户提供具有连贯性、同理心和独特人格的 AI 伴侣体验。

---

## 🛠 技术栈 (Tech Stack)

- **核心框架**: Spring Boot 3.5.7 (Java 21)
- **AI 接入**: Spring AI (兼容 OpenAI 协议，当前适配讯飞星火/通义千问等)
- **数据库**: MySQL 8.0
- **ORM 框架**: MyBatis 3.0.5
- **鉴权安全**: Spring Security + JWT (Json Web Token)
- **JSON 处理**: Jackson
- **构建工具**: Maven

---

## ✨ 核心特性 (Features)

### 1. 🔐 用户与安全体系
- **JWT 无状态认证**：基于 Token 的安全验证机制。
- **密码加密**：使用 BCrypt 强哈希算法存储密码。
- **个性化配置**：支持存储用户偏好（如 AI 性格、说话语气），动态调整 Prompt。

### 2. 💬 智能对话系统 (Core Chat)
- **双模式响应**：
  - **阻塞式 (Blocking)**：适合简单问答，一次性返回完整结果。
  - **流式 (Streaming)**：基于 SSE (Server-Sent Events)，实现打字机效果，首字延迟极低。
- **自动会话管理**：
  - 智能识别新旧会话。
  - **自动起标题**：根据首条消息内容自动生成会话标题（前20字）。
- **上下文记忆**：
  - 基于数据库 (`chat_message` 表) 实现的持久化聊天记忆 (`NoviDatabaseChatMemory`)。
  - 支持多轮对话，AI 能记住之前的聊天内容。
  - *(计划中)* 长期事实记忆提取 (Long-term Memory Extraction)。

### 3. 📚 会话与历史管理
- **侧边栏会话列表**：按最后活跃时间排序。
- **历史回溯**：随时加载查看任意会话的完整历史记录。
- **软删除**：支持逻辑删除会话，保障数据安全。

### 4. 📖 API 文档 (API Documentation)
- **Knife4j 集成**：集成 Knife4j 4.4.0，提供美观、强大的在线接口文档。
- **文档地址**：启动项目后访问 `http://localhost:8080/doc.html`。
- **特性**：
  - **接口分组**：按功能模块（如用户管理、AI聊天、AI出题等）清晰分组。
  - **调试功能**：支持在线调试 API，方便前端开发与测试。
  - **作者信息**：每个接口均标注作者信息 (N1etzsch3)，方便责任溯源。
  - **JWT 支持**：文档页面已配置 JWT 认证，支持直接在文档中进行鉴权调试。

---

## 🚀 快速开始 (Quick Start)

### 1. 环境准备
- JDK 21+ (推荐 JDK 25)
- MySQL 8.0+
- Maven 3.8+

### 2. 数据库配置
请在 MySQL 中创建一个名为 `novi` 的数据库，并依次执行 `src/main/resources/static/` 下的 SQL 脚本：
1. `user_account.sql` (用户表)
2. `chat_session.sql` (会话元数据表)
3. `chat_message.sql` (消息内容表)
4. `user_memory.sql` (用户画像/事实记忆表)

### 3. 配置文件
设置环境变量：

```env
SPRING_DATASOURCE_URL=""
SPRING_DATASOURCE_USERNAME=""
SPRING_DATASOURCE_PASSWORD=""
JWT_SECRET_KEY=""
```

### 4. 启动项目

运行 `NoviApplication.java` 的 `main` 方法。 服务默认启动在端口: `8080`。

## 📂 目录结构

```Plaintext
Novi (Root)
├── novi-common      # 公共模块：实体类(Entity), DTO, 工具类(Utils), 异常(Exception), 枚举(Enums)
├── novi-user        # 用户模块：用户管理 Service, Mapper, DTO, 拦截器
├── novi-chat        # 聊天模块：聊天 Service, Mapper, Repository (ChatMemory), DTO
├── novi-question    # 出题模块：AI出题 Service, Mapper, DTO
├── novi-ai-config   # AI配置模块：模型与提示词配置 Service, Mapper, Factory, DTO
├── novi-web         # Web模块：启动类, 控制器(Controller), 全局配置(Config), 资源文件(Resources)
└── novi-view        # 视图模块：包含控制台客户端 (Console Client) 和 GUI 客户端 (GUI Client)
```
