# Long AI Code Mother - AI 代码生成平台

一个基于 Spring Boot 3 + Vue 3 的智能代码生成平台，通过与 AI 对话来创建、部署和管理 Web 应用程序。

## 🚀 项目简介

Long AI Code Mother 是一个全栈 AI 代码生成器，用户可以通过自然语言描述需求，AI 会自动生成相应的 Web 应用代码。支持多种代码生成类型，包括 HTML 页面、Vue 项目等，并提供一键部署功能。

## ✨ 核心功能

### 用户功能
- 🤖 **AI 对话生成**：通过自然语言与 AI 对话生成代码
- 🎨 **多种生成类型**：支持 HTML、Vue 项目等多种代码生成
- 👀 **实时预览**：生成过程中实时查看代码效果
- 🚀 **一键部署**：将生成的应用部署到云端
- 📱 **应用管理**：创建、编辑、删除个人应用
- 📋 **应用列表**：查看个人应用和精选应用

### 管理员功能
- 🔧 **应用管理**：管理所有用户的应用
- 👥 **用户管理**：用户信息管理和权限控制
- 💬 **对话管理**：查看和管理用户对话记录
- ⭐ **精选设置**：设置优质应用为精选推荐

## 🏗️ 技术架构

### 后端技术栈
- **框架**：Spring Boot 3.5.4 + Java 21
- **数据库**：MySQL 8.0 + MyBatis-Flex
- **缓存**：Redis + Caffeine
- **权限认证**：Sa-Token
- **AI 集成**：LangChain4j + OpenAI API + 阿里云 DashScope
- **消息队列**：RabbitMQ
- **对象存储**：腾讯云 COS
- **监控**：Spring Boot Actuator + Prometheus
- **文档**：Knife4j (Swagger)
- **Web 服务器**：Undertow

### 前端技术栈
- **框架**：Vue 3 + TypeScript
- **UI 组件**：Ant Design Vue
- **状态管理**：Pinia
- **路由**：Vue Router 4
- **构建工具**：Vite
- **HTTP 客户端**：Axios
- **代码规范**：ESLint + Prettier

### AI 能力
- **多模型支持**：OpenAI GPT、阿里云通义千问(支持所有兼容openai规范的模型厂商)
- **工作流引擎**：LangGraph4j 实现复杂 AI 工作流
- **代码生成**：支持多种前端生成模式（HTML模式，HTML+CSS+javaScript模式,VUE工程模式）
- **质量检查**：AI 代码质量检查和优化建议
- **智能路由**：根据需求自动选择最适合的生成策略

## 📁 项目结构

```
long-ai-code-mother/
├── src/main/java/cn/longwingstech/intelligence/longaicodemother/
│   ├── ai/                          # AI 相关服务
│   │   ├── AiCodeGeneratorService.java      # AI 代码生成服务
│   │   ├── TitleGeneratorService.java       # 标题生成服务
│   │   ├── guardrail/                       # AI 安全防护
│   │   ├── memory/                          # 对话记忆管理
│   │   ├── model/                           # AI 模型配置
│   │   └── tools/                           # AI 工具集成
│   ├── aop/                         # 切面编程
│   │   ├── RateLimiter.java                 # 限流注解
│   │   └── RateLimiterAspect.java           # 限流切面
│   ├── common/                      # 公共组件
│   │   ├── BaseResponse.java                # 统一响应格式
│   │   ├── PageRequest.java                 # 分页请求
│   │   └── ResultUtils.java                 # 结果工具类
│   ├── config/                      # 配置类
│   │   ├── CorsConfig.java                  # 跨域配置
│   │   ├── CosClientConfig.java             # 对象存储配置
│   │   ├── PrototypeChatModelConfig.java    # AI 模型配置
│   │   └── StpInterfaceImpl.java            # 权限接口实现
│   ├── controller/                  # 控制器层
│   │   ├── AppController.java               # 应用管理
│   │   ├── ChatHistoryController.java       # 对话历史
│   │   ├── UserController.java              # 用户管理
│   │   └── WorkFlowController.java          # 工作流控制
│   ├── core/                        # 核心业务
│   │   ├── AiCodeGeneratorFacade.java       # 代码生成门面
│   │   ├── CodeParserExecutor.java          # 代码解析执行器
│   │   ├── builder/                         # 构建器
│   │   ├── handler/                         # 处理器
│   │   ├── parser/                          # 解析器
│   │   └── saver/                           # 保存器
│   ├── langgraph4j/                 # LangGraph4j 工作流
│   │   ├── CodeGenWorkflow.java             # 代码生成工作流
│   │   ├── ai/                              # AI 节点
│   │   ├── model/                           # 工作流模型
│   │   ├── node/                            # 工作流节点
│   │   ├── state/                           # 状态管理
│   │   └── tools/                           # 工具节点
│   ├── model/                       # 数据模型
│   │   ├── dto/                             # 数据传输对象
│   │   ├── entity/                          # 实体类
│   │   └── vo/                              # 视图对象
│   ├── service/                     # 服务层
│   │   ├── AppService.java                  # 应用服务
│   │   ├── ChatHistoryService.java          # 对话历史服务
│   │   └── UserService.java                 # 用户服务
│   └── utils/                       # 工具类
├── ai-code-mother-frontend/         # 前端项目
│   ├── src/
│   │   ├── api/                             # API 接口
│   │   ├── components/                      # 公共组件
│   │   ├── pages/                           # 页面组件
│   │   ├── router/                          # 路由配置
│   │   ├── stores/                          # 状态管理
│   │   └── utils/                           # 工具函数
│   ├── package.json
│   └── vite.config.ts
├── sql/                             # 数据库脚本
│   └── 建表语句.sql
├── pom.xml                          # Maven 配置
└── Dockerfile                       # Docker 配置
```

## 🛠️ 快速开始

### 环境要求
- Java 21+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+

### 后端启动

1. **克隆项目**
```bash
git clone git@gitee.com:Long-Boy-Project/ai-code-code-mother.git
cd long-ai-code-mother
```

2. **配置数据库**
```bash
# 创建数据库
mysql -u root -p
source sql/new_create_database.sql
```

3. **配置应用**
```yaml
# 修改 src/main/resources/application.yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_code_mother
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

4. **启动应用**
```bash
mvn spring-boot:run
```

### 前端启动

1. **进入前端目录**
```bash
cd ai-code-mother-frontend
```

2. **安装依赖**
```bash
npm install
```

3. **启动开发服务器**
```bash
npm run dev
```

4. **访问应用**
- 前端地址：http://localhost:5173
- 后端 API：http://localhost:8123/api
- API 文档：http://localhost:8123/api/doc.html

## 🔧 配置说明

### AI 模型配置
```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: your_openai_api_key
      model-name: 兼容openai的模型（如GPT5）
      base-url: https://api.openai.com/v1
```

### 对象存储配置
```yaml
cos:
  client:
    access-key: your_access_key
    secret-key: your_secret_key
    region: ap-beijing
    bucket: your_bucket_name
```

### 部署配置
```yaml
deploy:
  code_deploy_host: http://your-deploy-domain.com(你的部署地址)
```

## 📊 数据库设计

### 核心表结构
- **user**：用户信息表
- **app**：应用信息表
- **chat_history**：对话历史表

详细建表语句请参考 `sql/建表语句.sql`

## 🔐 权限控制

基于 Sa-Token 实现的权限控制：
- **用户角色**：user（普通用户）、admin（管理员）
- **接口鉴权**：通过注解实现接口级别的权限控制
- **登录状态**：支持 Token 自动续期和多端登录控制

## 🚀 部署指南

### Docker 部署
```bash
# 构建镜像
docker build -t long-ai-code-mother .

# 运行容器
docker run -d -p 8123:8123 long-ai-code-mother
```

### 生产环境配置
1. 配置生产环境数据库连接
2. 设置 Redis 集群
3. 配置 RabbitMQ 集群
4. 设置对象存储服务
5. 配置域名和 SSL 证书

## 📈 监控和运维

- **健康检查**：`/api/actuator/health`
- **指标监控**：`/api/actuator/prometheus`
- **API 文档**：Knife4j 在线文档

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add amazing feature'`
4. 推送分支：`git push origin feature/amazing-feature`
5. 提交 Pull Request

## 📝 开发规范

- **代码风格**：遵循阿里巴巴 Java 开发手册
- **提交规范**：使用 Conventional Commits 规范
- **分支管理**：采用 Git Flow 工作流
- **测试覆盖**：核心业务逻辑需要单元测试

## 🐛 问题反馈

如果您在使用过程中遇到问题，请通过以下方式反馈：
- 提交 Issue
- 发送邮件至：a13695191730@163.com

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

感谢以下开源项目的支持：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [LangChain4j](https://github.com/langchain4j/langchain4j)
- [MyBatis-Flex](https://mybatis-flex.com/)
- [Sa-Token](https://sa-token.cc/)
- [Ant Design Vue](https://antdv.com/)

---
