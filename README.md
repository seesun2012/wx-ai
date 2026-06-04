# 万象AI

> 基于 LangChain4j 的聚合 AI 大模型平台 — 统一接入各大模型厂商，一站式提供对话、RAG、工具调用、MCP 等能力

## 项目介绍

万象AI 是一个聚合各大模型厂商平台的 AI 基座项目，通过统一的 OpenAI 兼容接口接入多种大模型（通义千问、智谱等），在此基础上构建对话、RAG 检索增强、MCP 工具调用、安全防护等丰富能力。所有功能均基于这个基座进行扩展，可快速对接不同模型供应商。

**核心能力：**

- **SSE 流式对话** — 基于 Server-Sent Events + Reactor Flux 实时推送 AI 回复，打字机效果
- **多会话管理** — 支持创建/重命名/删除多个对话会话，消息持久化到 MySQL
- **会话记忆** — 基于 MySQL 自定义 ChatMemoryStore，保持多轮对话上下文连贯
- **RAG 检索增强** — 加载 `resources/docs/` 下的 Markdown 知识库文档，向量化后增强回答（可选开启）
- **MCP 工具调用** — 接入智谱 BigModel MCP Server（联网搜索），AI 可自主调用外部工具
- **输入安全防护** — SafeInputGuardrail 拦截敏感词、提示注入攻击、超长输入
- **答案缓存** — Caffeine 本地缓存（最大 5000 条，6 小时过期），相同问题直接返回
- **用户认证** — 注册/登录获取 Token，拦截器统一校验 `Authorization: Bearer <token>`
- **模型可插拔** — 统一 OpenAI 兼容接口，切换模型厂商只需改配置，无需改代码

---

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 运行环境 |
| Spring Boot | 3.5.3 | Web 框架 |
| LangChain4j | 1.12.2 | AI 应用框架（OpenAI 兼容接口，可对接各大模型厂商） |
| LangChain4j OpenAI | 1.12.2 | OpenAI 兼容模型集成 |
| LangChain4j Reactor | 1.12.2-beta22 | 响应式 Flux 流式输出 |
| LangChain4j MCP | 1.1.0-beta7 | MCP 协议，接入智谱联网搜索 |
| LangChain4j Spring Boot Starter | 1.12.2-beta22 | LangChain4j 自动装配 |
| MyBatis Spring Boot Starter | 3.0.4 | ORM 持久层 |
| MySQL Connector | runtime | MySQL 驱动 |
| Caffeine | 3.1.8 | 答案本地缓存 |
| Jsoup | 1.20.1 | 网页抓取（InterviewQuestionTool 使用） |
| Lombok | - | 简化实体类代码 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue.js | 3.4 | 前端框架 |
| Vite | 5.0 | 构建工具 |
| Axios | 1.6.2 | HTTP 请求（REST 接口） |
| Vue Router | 4.6.4 | 路由管理（登录页 / 聊天页） |

### AI 服务

- **接入方式**：通过 OpenAI 兼容接口统一对接各大模型厂商，切换厂商只需改 `base-url` 和 `api-key`
- **当前接入**：通义千问（阿里云 DashScope）
- **文本/流式模型**：`qwen-plus`
- **视觉多模态模型**：`qwen-vl-plus`（支持图片输入）
- **Embedding 向量模型**：`text-embedding-v3`（RAG 文档向量化）
- **MCP Server**：智谱 BigModel 联网搜索（SSE 传输协议）

> **扩展模型**：由于采用 OpenAI 兼容接口，可快速接入 DeepSeek、Moonshot、百川、零一万物等任何支持该协议的模型厂商，只需修改配置文件中的 `base-url`、`api-key` 和模型名称即可。

---

## 项目结构

```
wx-ai/
├── wx-ai-frontend/                          # 前端项目（Vue 3 + Vite）
│   ├── src/
│   │   ├── api/chat.js                      #   API 封装：Axios 实例 + SSE fetch 流式请求
│   │   ├── components/
│   │   │   ├── ChatLayout.vue               #   整体布局：左侧会话列表 + 右侧对话区
│   │   │   ├── ChatRoom.vue                 #   对话组件：消息列表 + Markdown 渲染 + 输入框
│   │   │   └── LoginPage.vue                #   登录/注册页面
│   │   ├── App.vue                          #   根组件
│   │   ├── main.js                          #   应用入口（Vue Router 路由配置）
│   │   └── style.css                        #   全局样式
│   ├── index.html                           #   HTML 入口
│   ├── vite.config.js                       #   Vite 配置（端口 3000 + 反向代理到后端）
│   └── package.json
│
├── src/main/java/com/wxai/
│   ├── ai/                                  # ===== AI 核心模块 =====
│   │   ├── AiCode.java                      #   AI 助手入口，组装 AI Service
│   │   ├── AiCodeService.java               #   对话服务接口（流式/同步/结构化输出）
│   │   ├── AiCodeServiceFactory.java        #   AI Service 工厂（手动装配模型+记忆+工具）
│   │   ├── guardrail/
│   │   │   └── SafeInputGuardrail.java      #   输入安全检测（敏感词/注入攻击/长度限制）
│   │   ├── listener/
│   │   │   └── ChatModelListenerConfig.java #   对话监听器（日志记录等）
│   │   ├── mcp/
│   │   │   └── McpConfig.java               #   MCP 协议配置（智谱联网搜索连接池）
│   │   ├── rag/
│   │   │   └── RagConfig.java               #   RAG 配置（加载 docs/*.md → 向量化 → 检索）
│   │   └── tool/
│   │       └── InterviewQuestionTool.java   #   AI 工具：面试题搜索（Jsoup 抓取网页）
│   ├── config/                              # ===== 配置类 =====
│   │   ├── AiModelConfig.java               #   模型 Bean 配置（Chat/Stream/Vision/Embedding）
│   │   ├── AuthInterceptor.java             #   Token 认证拦截器
│   │   ├── CorsConfig.java                  #   跨域配置 + 拦截器注册
│   │   └── JacksonConfig.java               #   JSON 序列化配置
│   ├── controller/                          # ===== REST API =====
│   │   ├── AiController.java                #   POST /ai/chat（SSE 流式对话）
│   │   ├── SessionController.java           #   会话 CRUD（创建/列表/重命名/消息/删除）
│   │   └── UserController.java              #   用户注册/登录/登出/信息
│   ├── entity/                              # 数据库实体（User, AiChatMemory, AiChatMessage）
│   ├── mapper/                              # MyBatis Mapper 接口
│   ├── memory/
│   │   └── MySqlChatMemoryStore.java        # MySQL 实现 LangChain4j ChatMemoryStore
│   ├── request/
│   │   └── LoginRequest.java                # 登录请求 DTO
│   ├── service/                             # ===== 业务服务 =====
│   │   ├── AnswerCacheService.java          #   Caffeine 答案缓存（5000条/6h过期）
│   │   ├── TextChatService.java             #   文本对话服务
│   │   ├── TextChatWithMemoryService.java   #   带记忆的对话服务
│   │   ├── TextChatWithMemoryServiceConfig.java
│   │   └── UserService.java                 #   用户服务（SHA-256 密码哈希 + Token 管理）
│   └── WxAiApplication.java                 # Spring Boot 启动入口
│
├── src/main/resources/
│   ├── db/schema.sql                        # 数据库建表脚本（3 张表）
│   ├── docs/                                # RAG 知识库文档（Markdown 格式）
│   │   ├── Java 编程学习路线.md
│   │   ├── 程序员常见面试题.md
│   │   ├── 鱼皮的求职指南.md
│   │   └── 鱼皮的项目学习建议.md
│   ├── static/
│   │   └── chat.html                        # 静态调试页面（可直接访问 /chat.html）
│   ├── application.yml                      # 主配置（激活 local profile）
│   ├── application-local.yml                # 本地开发配置（数据库/API Key/端口）
│   └── system-prompt.txt                    # AI 系统提示词（角色定义 + 回答规范）
│
└── pom.xml                                  # Maven 依赖配置
```

---

## 环境准备

### 一、JDK 21

项目使用了 Java 21 特性（如 Record 类型、虚拟线程等），必须安装 JDK 21 及以上版本。

**下载地址（任选其一）：**

| 发行版 | 下载地址 | 说明 |
|--------|----------|------|
| Oracle JDK 21 | https://www.oracle.com/java/technologies/downloads/#java21 | 官方版本 |
| Eclipse Temurin 21 | https://adoptium.net/temurin/releases/?version=21 | 开源免费，推荐 |
| Amazon Corretto 21 | https://docs.aws.amazon.com/corretto/latest/userguide/downloads-list.html | AWS 维护的免费发行版 |
| Azul Zulu 21 | https://www.azul.com/downloads/?version=java-21-lts#zulu | 免费商用 |

**安装步骤（Windows）：**

1. 下载对应平台的 `.msi` 或 `.zip` 安装包
2. 安装/解压到目标目录，例如 `C:\Program Files\Java\jdk-21`
3. 配置环境变量：
   - 新建系统变量 `JAVA_HOME`，值为 JDK 安装路径，如 `C:\Program Files\Java\jdk-21`
   - 编辑系统变量 `Path`，添加 `%JAVA_HOME%\bin`
4. 验证安装：
```bash
java -version
# 应输出：openjdk version "21.x.x" 或 java version "21.x.x"
```

**安装步骤（Linux / CentOS 7.5）：**

```bash
# 方式一：yum 安装（以 Temurin 为例）
wget https://packages.adoptium.net/artifactory/rpm/centos/7/x86_64/Packages/temurin-21-jdk-21.0.4+7-1.x86_64.rpm
yum localinstall -y temurin-21-jdk-21.0.4+7-1.x86_64.rpm

# 方式二：手动解压
wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz
tar -xzf jdk-21_linux-x64_bin.tar.gz -C /usr/local/

# 配置环境变量
echo 'export JAVA_HOME=/usr/local/jdk-21' >> /etc/profile
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/profile
source /etc/profile

# 验证
java -version
```

### 二、Maven 3.6+

Maven 用于管理后端 Java 依赖和构建。

> 项目自带 `mvnw`（Maven Wrapper），运行 `mvnw.cmd` 会自动下载对应版本的 Maven，**可以不单独安装**。但如果想使用全局 `mvn` 命令，建议手动安装。

**下载地址：**
- 官方下载页：https://maven.apache.org/download.cgi
- 推荐直接下载 zip：https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip

**安装步骤（Windows）：**

1. 下载 zip 包并解压，例如 `D:\tools\apache-maven-3.9.9`
2. 配置环境变量：
   - 新建系统变量 `MAVEN_HOME`，值为 Maven 解压路径
   - 编辑系统变量 `Path`，添加 `%MAVEN_HOME%\bin`
3. 配置 Maven 仓库镜像（加速依赖下载）：
   - 编辑 `conf/settings.xml`，在 `<mirrors>` 节点中添加阿里云镜像：
   ```xml
   <mirror>
     <id>aliyunmaven</id>
     <mirrorOf>*</mirrorOf>
     <name>阿里云公共仓库</name>
     <url>https://maven.aliyun.com/repository/public</url>
   </mirror>
   ```
4. 验证安装：
```bash
mvn -version
# 应输出：Apache Maven 3.9.x ...
```

**安装步骤（Linux / CentOS 7.5）：**

```bash
cd /usr/local/
wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
tar -xzf apache-maven-3.9.9-bin.tar.gz

echo 'export MAVEN_HOME=/usr/local/apache-maven-3.9.9' >> /etc/profile
echo 'export PATH=$MAVEN_HOME/bin:$PATH' >> /etc/profile
source /etc/profile

mvn -version
```

### 三、MySQL 5.7+

用于存储用户、会话、消息数据。

**下载地址：**

| 版本 | 下载地址 | 说明 |
|------|----------|------|
| MySQL 8.0（推荐） | https://dev.mysql.com/downloads/mysql/ | 官方最新版 |
| MySQL 5.7 | https://dev.mysql.com/downloads/mysql/5.7.html | 最低兼容版本 |
| MySQL Installer（Windows） | https://dev.mysql.com/downloads/installer/ | Windows 图形化安装器 |

**安装要点（Windows）：**
- 推荐使用 MySQL Installer 图形化安装，安装时记住设置的 root 密码
- 确保 MySQL 服务已启动（`services.msc` 中查看 MySQL 服务状态）

**安装要点（Linux / CentOS 7.5）：**

```bash
# 安装 MySQL 8.0
wget https://dev.mysql.com/get/mysql80-community-release-el7-11.noarch.rpm
rpm -ivh mysql80-community-release-el7-11.noarch.rpm
yum install -y mysql-community-server

# 启动并设置开机自启
systemctl start mysqld
systemctl enable mysqld

# 获取初始密码并登录
grep 'temporary password' /var/log/mysqld.log
mysql -u root -p

# 登录后修改密码
ALTER USER 'root'@'localhost' IDENTIFIED BY '你的新密码';
```

**验证连接：**
```bash
mysql -u root -p
# 输入密码后进入 MySQL 命令行即表示安装成功
```

### 四、Node.js 16+（推荐 18 LTS 或 20 LTS）

用于前端项目的依赖安装和构建。

**下载地址：**
- 官方下载页：https://nodejs.org/en/download
- 推荐使用 LTS 版本：https://nodejs.org/en/download/prebuilt-installer

**安装步骤（Windows）：**

1. 下载 `.msi` 安装包，按向导安装（会自动配置 PATH）
2. 安装完成后验证：
```bash
node -v    # 应输出：v18.x.x 或 v20.x.x
npm -v     # 应输出：9.x.x 或 10.x.x
```
3. 配置 npm 镜像（加速依赖下载，可选）：
```bash
npm config set registry https://registry.npmmirror.com
```

**安装步骤（Linux / CentOS 7.5）：**

```bash
# 使用 NodeSource 安装 Node.js 20 LTS
curl -fsSL https://rpm.nodesource.com/setup_20.x | bash -
yum install -y nodejs

node -v
npm -v

# 配置 npm 镜像（可选）
npm config set registry https://registry.npmmirror.com
```

### 环境依赖汇总

| 依赖 | 最低版本 | 下载地址 | 是否必须 |
|------|----------|----------|----------|
| JDK | 21+ | https://adoptium.net/temurin/releases/?version=21 | 必须 |
| Maven | 3.6+ | https://maven.apache.org/download.cgi | 可选（项目自带 mvnw） |
| MySQL | 5.7+ | https://dev.mysql.com/downloads/installer/ | 必须 |
| Node.js | 16+ | https://nodejs.org/en/download | 必须 |

---

## 本地开发部署

> 适用于日常开发调试，前后端分别在本地启动。

### 一、数据库初始化

确保 MySQL 服务已启动，然后执行建表脚本：

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

该脚本会自动完成：
1. 创建数据库 `wx_ai`（字符集 `utf8mb4`，排序规则 `utf8mb4_unicode_ci`）
2. 创建 `sys_user` 表 — 用户信息（用户名、SHA-256 密码哈希、Token）
3. 创建 `ai_chat_memory` 表 — 会话记忆（会话 ID、标题、AI 记忆摘要 JSON）
4. 创建 `ai_chat_message` 表 — 消息明细（会话 ID、角色 USER/AI、消息内容）

### 二、后端配置

编辑 `src/main/resources/application-local.yml`：

```yaml
server:
  port: 8080                                # 后端监听端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wx_ai?useSSL=false&characterEncoding=utf-8
    username: root                          # ← 改为你的 MySQL 用户名
    password: 123456                        # ← 改为你的 MySQL 密码
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis:
  configuration:
    map-underscore-to-camel-case: true      # 下划线转驼峰映射

rag:
  enabled: false                            # 是否启用 RAG（true 会加载 docs/*.md 并消耗 Embedding Token）

ai:
  model:
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1   # 通义千问 OpenAI 兼容端点
    api-key: sk-xxxxxx                      # ← 改为你的通义千问 API Key
    model-text: qwen-plus                   # 文本对话模型
    model-vision: qwen-vl-plus              # 视觉多模态模型（支持图片输入）
    embedding-model: text-embedding-v3      # 向量嵌入模型（RAG 用）
    stream-model: qwen-plus                 # 流式输出模型

bigmodel:
  api-key: xxxxxx                           # ← 改为你的智谱 BigModel API Key（MCP 联网搜索用）
  mcp:
    pool-size: 1                            # MCP 连接池大小（开发时设 1，生产建议 3）
```

**API Key 获取：**

| 厂商 | 获取地址 | 用途 |
|------|----------|------|
| 通义千问（当前默认） | https://dashscope.aliyun.com/ → API-KEY 管理 | AI 对话 + 向量嵌入 |
| 智谱 BigModel | https://open.bigmodel.cn/ → API Keys | MCP 联网搜索工具 |

> **切换模型厂商**：由于项目使用 OpenAI 兼容接口，切换到其他厂商只需修改 `base-url`、`api-key` 和模型名称，例如：
> - DeepSeek：`base-url` 改为 `https://api.deepseek.com/v1`
> - Moonshot：`base-url` 改为 `https://api.moonshot.cn/v1`
> - 百川：`base-url` 改为 `https://api.baichuan-ai.com/v1`
> - 零一万物：`base-url` 改为 `https://api.lingyiwanwu.com/v1`

> **关于 RAG：** 设为 `true` 后，应用启动时会自动读取 `resources/docs/` 下的所有 `.md` 文件，按段落切分后调用 Embedding 模型向量化存入内存。首次启动会消耗 Embedding API Token。默认为 `false`（关闭）。

### 三、启动后端

```bash
# 方式一：Maven Wrapper（推荐，无需安装 Maven）
# Windows
mvnw.cmd spring-boot:run
# Linux/Mac
./mvnw spring-boot:run

# 方式二：系统 Maven
mvn spring-boot:run
```

启动成功后控制台会输出 Spring Boot 启动日志，后端默认监听 `http://localhost:8080`。

**验证后端是否正常：**

```bash
curl -X POST http://localhost:8080/user/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"test\",\"password\":\"123456\"}"
```

### 四、启动前端

```bash
# 进入前端目录
cd wx-ai-frontend

# 安装依赖（仅首次需要）
npm install

# 启动开发服务器
npm run dev
```

前端默认监听 `http://localhost:3000`。

### 五、前端代理配置

`wx-ai-frontend/vite.config.js` 中配置了反向代理，将前端的 `/api` 请求转发到后端：

```js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',          // 监听所有网卡（方便局域网内其他设备访问）
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',    // ← 确保与后端地址一致
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
```

- 前端所有 API 请求（如 `/api/ai/chat`、`/api/user/login`）都会通过代理转发到后端
- **如果后端不在 localhost**（如部署在局域网其他机器），需要修改 `target` 为实际地址（如 `http://192.168.x.x:8080`）

### 六、访问与使用

1. 浏览器打开 `http://localhost:3000`
2. 注册账号 → 登录（获取 Token，存储在 localStorage）
3. 点击「新建对话」创建会话
4. 在输入框输入问题，AI 会以流式方式实时返回回答

---

## 服务器部署（CentOS 7.5）

> 适用于生产环境部署，以 CentOS 7.5 为例。

### 一、服务器环境安装

#### 1. 安装 JDK 21

```bash
cd /usr/local/
wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz
tar -xzf jdk-21_linux-x64_bin.tar.gz

# 配置环境变量
cat >> /etc/profile << 'EOF'
export JAVA_HOME=/usr/local/jdk-21
export PATH=$JAVA_HOME/bin:$PATH
EOF
source /etc/profile
java -version
```

#### 2. 安装 Maven

```bash
cd /usr/local/
wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
tar -xzf apache-maven-3.9.9-bin.tar.gz

cat >> /etc/profile << 'EOF'
export MAVEN_HOME=/usr/local/apache-maven-3.9.9
export PATH=$MAVEN_HOME/bin:$PATH
EOF
source /etc/profile

# 配置阿里云镜像加速
sed -i 's|</mirrors>|<mirror><id>aliyunmaven</id><mirrorOf>*</mirrorOf><name>阿里云公共仓库</name><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors>|' $MAVEN_HOME/conf/settings.xml

mvn -version
```

#### 3. 安装 MySQL 8.0

```bash
wget https://dev.mysql.com/get/mysql80-community-release-el7-11.noarch.rpm
rpm -ivh mysql80-community-release-el7-11.noarch.rpm
yum install -y mysql-community-server

systemctl start mysqld
systemctl enable mysqld

# 获取临时密码
grep 'temporary password' /var/log/mysqld.log

# 登录并修改密码
mysql -u root -p
# ALTER USER 'root'@'localhost' IDENTIFIED BY '你的强密码';
```

#### 4. 安装 Node.js 20

```bash
curl -fsSL https://rpm.nodesource.com/setup_20.x | bash -
yum install -y nodejs
npm config set registry https://registry.npmmirror.com
```

#### 5. 安装 Nginx

```bash
yum install -y epel-release
yum install -y nginx
systemctl start nginx
systemctl enable nginx
```

### 二、上传项目代码

```bash
# 在服务器上创建项目目录
mkdir -p /opt/wx-ai
# 将项目代码上传到服务器（通过 git clone 或 scp）
cd /opt/wx-ai
git clone <你的仓库地址> .
```

### 三、初始化数据库

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

### 四、创建生产配置文件

新建 `src/main/resources/application-prod.yml`：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wx_ai?useSSL=false&characterEncoding=utf-8
    username: root
    password: 你的生产密码
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis:
  configuration:
    map-underscore-to-camel-case: true

rag:
  enabled: false

ai:
  model:
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    api-key: 你的生产API-Key
    model-text: qwen-plus
    model-vision: qwen-vl-plus
    embedding-model: text-embedding-v3
    stream-model: qwen-plus

bigmodel:
  api-key: 你的智谱API-Key
  mcp:
    pool-size: 3
```

> **安全提醒**：生产配置文件包含密钥，**不要提交到 Git 仓库**，请加入 `.gitignore`。

### 五、后端打包与启动

```bash
cd /opt/wx-ai

# 编译打包（跳过测试）
mvn clean package -DskipTests

# 启动（指定生产配置）
nohup java -jar target/wx-ai-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > app.log 2>&1 &

# 查看日志
tail -f app.log
```

**使用 systemd 管理后端服务（推荐）：**

```bash
cat > /etc/systemd/system/wx-ai.service << 'EOF'
[Unit]
Description=万象AI Backend
After=network.target mysqld.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/wx-ai
ExecStart=/usr/local/jdk-21/bin/java -jar /opt/wx-ai/target/wx-ai-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl start wx-ai
systemctl enable wx-ai
systemctl status wx-ai
```

### 六、前端构建

```bash
cd /opt/wx-ai/wx-ai-frontend

npm install
npm run build          # 构建产物输出到 dist/
```

### 七、Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;             # ← 改为你的域名或 IP

    # 前端静态文件
    location / {
        root /opt/wx-ai/wx-ai-frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;    # Vue Router history 模式
    }

    # 后端 API 代理（含 SSE 流式支持）
    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        # SSE 流式响应支持（关键配置）
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 180s;
        chunked_transfer_encoding on;
    }
}
```

```bash
# 重载 Nginx 配置
nginx -t               # 检查语法
nginx -s reload        # 热重载
```

### 八、防火墙配置

```bash
# 开放 HTTP 端口
firewall-cmd --permanent --add-service=http
firewall-cmd --permanent --add-service=https
firewall-cmd --reload
```

---

## Docker 容器化部署

> 适用于快速部署和环境一致性保障。

### 一、后端 Dockerfile

在项目根目录创建 `Dockerfile`：

```dockerfile
# 构建阶段
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

### 二、前端 Dockerfile

在 `wx-ai-frontend/` 目录创建 `Dockerfile`：

```dockerfile
# 构建阶段
FROM node:20-alpine AS builder
WORKDIR /build
COPY package.json package-lock.json ./
RUN npm install
COPY . .
RUN npm run build

# 运行阶段（Nginx 托管静态文件）
FROM nginx:alpine
COPY --from=builder /build/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

前端 `nginx.conf`（放在 `wx-ai-frontend/` 目录下）：

```nginx
server {
    listen 80;

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://wx-ai-backend:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 180s;
    }
}
```

### 三、Docker Compose 一键启动

在项目根目录创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: wx-ai-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: your_root_password
      MYSQL_DATABASE: wx_ai
      MYSQL_CHARACTER_SET_SERVER: utf8mb4
      MYSQL_COLLATION_SERVER: utf8mb4_unicode_ci
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./src/main/resources/db/schema.sql:/docker-entrypoint-initdb.d/schema.sql

  backend:
    build: .
    container_name: wx-ai-backend
    restart: always
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/wx_ai?useSSL=false&characterEncoding=utf-8
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: your_root_password
    depends_on:
      - mysql

  frontend:
    build: ./wx-ai-frontend
    container_name: wx-ai-frontend
    restart: always
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql-data:
```

**一键启动：**

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f

# 停止
docker-compose down

# 停止并清除数据卷（慎用）
docker-compose down -v
```

---

## Jenkins 自动化部署

> 适用于持续集成 / 持续部署（CI/CD），代码推送后自动构建部署。

### 一、Jenkins 安装（CentOS 7.5）

```bash
# 安装 Jenkins
wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key
yum install -y jenkins

# 启动
systemctl start jenkins
systemctl enable jenkins

# 获取初始管理员密码
cat /var/lib/jenkins/secrets/initialAdminPassword
```

浏览器访问 `http://你的服务器IP:8080`，输入初始密码完成初始化。

### 二、Jenkins 插件安装

在 Jenkins 管理 → 插件管理 中安装以下插件：

| 插件 | 用途 |
|------|------|
| Git Plugin | 拉取代码 |
| Pipeline | 流水线编排 |
| Docker Pipeline | Docker 构建 |
| Publish Over SSH | 远程部署到服务器 |
| Blue Ocean | 可视化流水线界面（可选） |

### 三、Jenkinsfile 流水线

在项目根目录创建 `Jenkinsfile`：

```groovy
pipeline {
    agent any

    environment {
        APP_NAME = 'wx-ai'
        DEPLOY_DIR = '/opt/wx-ai'
        JAR_NAME = 'wx-ai-0.0.1-SNAPSHOT.jar'
    }

    stages {
        stage('拉取代码') {
            steps {
                checkout scm
            }
        }

        stage('后端构建') {
            steps {
                sh 'mvn clean package -DskipTests -B'
            }
        }

        stage('前端构建') {
            steps {
                dir('wx-ai-frontend') {
                    sh '''
                        npm install
                        npm run build
                    '''
                }
            }
        }

        stage('部署到服务器') {
            steps {
                // 停止旧服务
                sshagent(['your-server-ssh-key']) {
                    sh """
                        ssh root@your-server-ip 'systemctl stop wx-ai || true'
                        scp target/${JAR_NAME} root@your-server-ip:${DEPLOY_DIR}/target/
                        scp -r wx-ai-frontend/dist/* root@your-server-ip:${DEPLOY_DIR}/wx-ai-frontend/dist/
                        ssh root@your-server-ip 'systemctl start wx-ai'
                        ssh root@your-server-ip 'nginx -s reload'
                    """
                }
            }
        }
    }

    post {
        success {
            echo '部署成功！'
        }
        failure {
            echo '部署失败，请检查日志！'
        }
    }
}
```

### 四、Jenkins 任务配置

1. Jenkins 首页 → 新建任务 → 流水线（Pipeline）
2. 配置 Git 仓库地址和凭证
3. Pipeline 定义选择 `Pipeline script from SCM`，指向项目中的 `Jenkinsfile`
4. 配置 Webhook（代码推送自动触发）：
   - 在 Gitee/GitHub 仓库设置中添加 Webhook，URL 为 `http://你的Jenkins地址/generic-webhook-trigger/invoke`

---

## API 接口一览

### 用户模块（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/register` | 注册（body: `{"username":"xx","password":"xxxxxx"}`） |
| POST | `/user/login` | 登录，返回 Token |

### 用户模块（需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/logout` | 退出登录（清除 Token） |
| GET | `/user/info` | 获取当前用户信息 |

### 会话模块（需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/session/create` | 创建新会话，返回 memoryId |
| GET | `/session/list` | 获取当前用户所有会话列表 |
| PUT | `/session/{memoryId}/title` | 重命名会话（body: `{"title":"新标题"}`） |
| GET | `/session/{memoryId}/messages?lastId=0&limit=20` | 分页获取历史消息 |
| DELETE | `/session/{memoryId}` | 删除会话及其所有消息 |

### AI 对话（需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/chat` | SSE 流式对话（form 参数: `memoryId` + `message`） |

> **认证方式**：请求头 `Authorization: Bearer <token>`，登录成功后返回

---

## 常见问题

**Q: 启动报数据库连接失败？**
检查 MySQL 是否启动，`application-local.yml` 中的 `username`/`password` 是否正确，数据库 `wx_ai` 是否已通过 `schema.sql` 创建。

**Q: 对话返回 401 未登录？**
确保已注册登录并拿到 Token，请求头中带上 `Authorization: Bearer <token>`。

**Q: MCP 联网搜索不工作？**
检查 `bigmodel.api-key` 是否正确配置，智谱 MCP Server 地址是否可达。

**Q: RAG 开启后启动很慢或报 Embedding 错误？**
RAG 开启后会在启动时对所有 `docs/*.md` 文件进行向量化，需要调用 Embedding API，检查 API Key 和余额。开发时可设置 `rag.enabled: false` 跳过。

**Q: CentOS 7.5 安装 MySQL 报 GPG key 错误？**
执行 `rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2023` 导入 GPG key 后重试。

**Q: Docker 启动后后端连不上 MySQL？**
确保 `docker-compose.yml` 中后端的 `SPRING_DATASOURCE_URL` 使用 `mysql` 作为主机名（Docker 内部 DNS），而非 `localhost`。

---

## 致谢

- [LangChain4j](https://github.com/langchain4j/langchain4j) — AI 应用开发框架
- [阿里云通义千问](https://dashscope.aliyun.com/) — 大语言模型服务
- [智谱 AI](https://open.bigmodel.cn/) — MCP 联网搜索服务
- [Spring Boot](https://spring.io/projects/spring-boot) — Java Web 框架
- [Vue.js](https://vuejs.org/) — 渐进式 JavaScript 框架
