# MySQL 部署与三端分离

项目本身就是三个可以独立运行的部分：

```text
浏览器
  │
  ├─ Vue 3 SPA（Nginx / Vite 开发服务器）  :80 / :5173
  │        │  /api
  │        ▼
  ├─ Spring Boot REST API                    :8090
  │        │  JDBC
  │        ▼
  └─ MySQL 8.4                               :3306
```

前端只通过 `/api` 访问后端，不包含任何数据库逻辑；后端只返回 JSON，不再负责托管前端页面；MySQL 保存全部业务数据。单 jar 内嵌前端仍然保留，作为「一个人本地随手用」的简化模式，但不是这套分离部署的必要条件。

## 1. 准备数据库

### 使用本机 MySQL

先确认 MySQL 服务已启动，然后以管理员身份执行一次 [sql/init.sql](../sql/init.sql)：

```powershell
mysql -u root -p -e "source sql/init.sql"
```

脚本会创建：

- 数据库 `interview_bank`，字符集 `utf8mb4`，排序规则 `utf8mb4_0900_ai_ci`
- 用户 `interview`，密码 `interview_pass`（仅用于本地开发，生产环境请修改）
- 该用户对 `interview_bank.*` 的权限

不想用默认账号时，可以手动执行等价 SQL，或者只创建空库，再用环境变量传入自己的账号：

```sql
CREATE DATABASE interview_bank
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

### 使用 Docker Compose

根目录的 [docker-compose.yml](../docker-compose.yml) 已经编排了 MySQL、Spring Boot 和 Nginx 三个服务，无需本机安装 MySQL：

```powershell
docker compose up -d --build
```

启动后：

| 服务 | 地址 |
| --- | --- |
| 前端（Nginx） | <http://127.0.0.1:18090> |
| 后端 API | <http://127.0.0.1:18089/api/stats/dashboard> |
| MySQL | `127.0.0.1:13307`，库 `interview_bank`，用户 `interview / interview_pass` |

端口或密码被占用时，复制 `.env.example` 为 `.env` 后覆盖：

```dotenv
WEB_PORT=18090
API_PORT=18089
MYSQL_PORT=13307
MYSQL_ROOT_PASSWORD=root_pass
DB_USERNAME=interview
DB_PASSWORD=interview_pass
```

停止服务：

```powershell
docker compose down
```

连数据一起删除：

```powershell
docker compose down -v
```

## 2. 分别启动后端与前端

### 后端（Spring Boot + MySQL）

先构建一次：

```powershell
cd server
mvn -DskipTests package
cd ..
```

然后使用 MySQL profile 启动：

```powershell
$env:SPRING_PROFILES_ACTIVE='mysql'
$env:DB_URL='jdbc:mysql://127.0.0.1:3306/interview_bank?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:DB_USERNAME='interview'
$env:DB_PASSWORD='interview_pass'
java -jar server\target\interview-bank.jar
```

也可以直接双击 `start-mysql.bat`，它会带上同样的默认值。

后端启动后只提供 `/api/**`，默认地址 <http://127.0.0.1:8090/api>。

### 前端（Vue 3）

```powershell
cd web
npm ci
npm run dev
```

打开 <http://127.0.0.1:5173>。Vite 会把 `/api` 代理到 `http://127.0.0.1:8090`，可在 `web/.env.local` 中修改：

```dotenv
VITE_API_BASE_URL=/api
VITE_PROXY_TARGET=http://127.0.0.1:8090
```

工程化命令：`npm run lint`、`npm run test`、`npm run format:check`、`npm run build`；CI 配置在 `.github/workflows/web-ci.yml`。

如果前端要单独部署到另一个域名，把 `VITE_API_BASE_URL` 改成完整地址，例如：

```dotenv
VITE_API_BASE_URL=https://api.example.com/api
```

后端已开启 CORS，允许任意来源访问 `/api/**`；正式环境建议把 `WebConfig` 中的 `allowedOriginPatterns("*")` 收紧到自己的前端域名。

### 生产构建（不用 Docker）

```powershell
cd web
npm ci
npm run build
```

把 `web/dist/` 交给 Nginx 托管，并把 `/api/` 反向代理到 Spring Boot。可以直接参考 [web/nginx.conf](../web/nginx.conf)。

后端可以单独打成 jar：

```powershell
cd server
mvn -DskipTests package
java -jar target/interview-bank.jar --spring.profiles.active=mysql
```

## 3. 配置项

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `h2` | 设为 `mysql` 使用 MySQL |
| `SERVER_PORT` | `8090` | 后端端口 |
| `DB_URL` | `jdbc:mysql://127.0.0.1:3306/interview_bank?...` | JDBC 地址 |
| `DB_USERNAME` | `interview` | 数据库用户 |
| `DB_PASSWORD` | `interview_pass` | 数据库密码 |

MySQL profile 的配置文件在 [application-mysql.yml](../server/src/main/resources/application-mysql.yml)。H2 仍保留在 `application-h2.yml`，用于零配置演示。

首次连接空库时，Hibernate 会自动建表，`SeedRunner` 会写入 20 道示例题、10 个分类和 2 条面试记录。初始化标记保存在数据库的 `app_meta` 表中，因此 H2 与 MySQL、不同数据库之间不会互相影响；删库重建后会重新写入示例数据。

表结构：

| 表 | 说明 |
| --- | --- |
| `category` | 分类 |
| `question` | 题目、答案、笔记与复习状态 |
| `question_tags` | 题目标签（ElementCollection） |
| `review_log` | 每次复习记录 |
| `interview_record` | 面试场次 |
| `interview_question` | 面试与题目的关联 |
| `app_meta` | 应用元数据（示例数据初始化标记） |

## 4. 从 H2 迁移已有数据

当前 `data/` 里的 H2 数据不会被 MySQL 自动读取。迁移方法：

1. 用 H2 模式启动旧版本，在「数据管理」导出 JSON。
2. 用 MySQL 模式启动新版本，在「数据管理」导入刚才的 JSON。
3. 导入按标题去重，不会覆盖 MySQL 中已有的题目。

如果只是体验示例数据，直接启动 MySQL 空库即可，会自动重新写入示例题。

## 5. 注意事项

- `ddl-auto: update` 适合个人项目和开发阶段；正式环境建议改用 Flyway / Liquibase 管理表结构。
- MySQL 连接串默认 `serverTimezone=UTC`，与 JPA 的 `hibernate.jdbc.time_zone=UTC` 保持一致。
- 题目正文使用 `MEDIUMTEXT`（Hibernate 根据长度自动选择），单题答案可以放很长的 Markdown。
- MySQL 8.4 已实测；MySQL 8.0 也可以使用。5.7 未验证。
