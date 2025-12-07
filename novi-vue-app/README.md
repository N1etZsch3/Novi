# Novi Vue App

这是 Novi 项目的 Vue 3 前端应用，从 `novi-v5.html` 单文件应用重构而来。

## 技术栈

- **Vue 3** - Composition API
- **Vite** - 构建工具
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Axios** - HTTP 客户端
- **Bootstrap 5** - UI 框架
- **Marked.js** - Markdown 渲染

## 项目结构

```
src/
├── assets/          # 资源文件（样式、图片）
├── components/      # 可复用组件
│   ├── common/     # 通用组件
│   ├── chat/       # 聊天相关组件
│   └── exam/       # 出题相关组件
├── views/           # 页面视图
├── stores/          # Pinia 状态管理
├── api/             # API 接口
├── router/          # 路由配置
├── composables/     # 组合式函数
└── utils/           # 工具函数
```

## 开发指南

### 安装依赖

由于 npm 目前有些问题，请先修复 Node.js 安装。完成后运行：

```bash
npm install
```

### 开发服务器

```bash
npm run dev
```

访问 http://localhost:5173

### 构建生产版本

```bash
npm run build
```

## 环境变量

- `.env.development` - 开发环境配置
- `.env.production` - 生产环境配置

主要配置：
- `VITE_API_BASE` - 后端 API 地址（默认：http://localhost:8080）

## API 代理

开发环境下已配置 API 代理，以下路径会自动转发到后端：
- `/api/*`
- `/sessions/*`
- `/chat/*`
- `/questions/*`

## 状态管理

使用 Pinia 进行状态管理，主要 stores：
- `theme` - 主题切换(深色/浅色模式)
- `auth` - 用户认证
- `chat` - 聊天功能
- `exam` - AI 出题
- `user` - 用户信息

## 路由

- `/auth` - 登录/注册
- `/chat` - 聊天界面
- `/exam` - AI 出题
- `/settings` - 设置

## 下一步开发计划

1. ✅ 项目初始化和配置
2. ✅ 样式系统迁移
3. ✅ 状态管理搭建
4. ✅ API 层封装
5. ✅ 路由配置
6. ⏳ 组件开发（进行中）
7. 核心功能实现
8. 响应式适配
9. 功能增强
10. 测试与部署

## 注意事项

### npm 问题解决

如果遇到 npm 错误，可以：
1. 重新安装 Node.js（推荐 LTS 版本）
2. 或使用 Yarn 代替：`npm install -g yarn`，然后 `yarn install`

### 开发建议

- 使用 VSCode + Volar 插件获得最佳开发体验
- 代码格式化：建议安装 ESLint + Prettier
- 调试：使用 Vue DevTools 扩展

## 许可证

MIT
