# AGENTS.md · 面试记忆助手

给在本仓库工作的 AI agent 的入口说明。先读本文件，再按任务加载对应文档或项目 skill。

## 项目是什么

**面试记忆助手**：记录面试题 → 主动回想 → 四档评分 → 间隔安排 → 面试复盘 → 洞察。

- 单用户、本地优先；H2 默认，MySQL 可选；
- 前端 Vue 3 + Element Plus + Vite；
- 后端 Spring Boot 3 + JPA；
- 代码内部仍使用历史代号 `interview-bank`（包名、jar、H2 文件名），**不要为了改名动数据文件或构建产物**。

## 常用命令

```powershell
# 前端
cd web
npm ci
npm run dev        # 5173
npm run verify     # lint + format check + test + build
npm run test       # Vitest

# 后端
cd server
mvn spring-boot:run
mvn -DskipTests package

# 单 jar 构建 / 运行
.\stop.ps1
.\build.ps1
java -jar server\target\interview-bank.jar   # http://127.0.0.1:8090
```

构建前必须先 `.\stop.ps1`，否则 Windows 会锁住 jar。

## 目录

```text
web/src/views/        页面：今日、训练、题库、面试、洞察、方法、数据
web/src/components/   复用组件
web/src/config/       brand.js / navigation.js（品牌与 IA 单一来源）
web/src/styles/       tokens.css → main.css → theme.css
web/src/api/          唯一请求入口
server/src/main/java/com/interviewbank/
  web/ service/ repo/ domain/ request/ common/
docs/                 PRODUCT / ARCHITECTURE / AGENT-SKILLS / design-system / mysql
.claude/skills/       项目本地 skills
```

## 硬约束

1. **不要删除或重命名 `data/interview-bank.mv.db`**；
2. 不要执行 `DELETE /api/data/all`，除非用户明确要求且已备份；
3. 不提交 `data/`、`.env`、`server/target/`、`web/dist/`；
4. 产品名统一走 `web/src/config/brand.js`，导航统一走 `web/src/config/navigation.js`；
5. 路由路径保持稳定，改信息架构只改展示名；
6. 颜色走 `tokens.css` 变量，不写死 HEX；
7. 文案使用“回想 / 记忆训练 / 加固中 / 长期记忆 / 待开始”，不用“复习 / 已掌握”；
8. 改前端后跑 `npm run verify`；改后端后至少 `mvn -DskipTests package`；
9. 完成前做真实页面检查，不以“构建成功”代替功能验证。

## 文档

- [产品定义](docs/PRODUCT.md)
- [系统架构](docs/ARCHITECTURE.md)
- [Agent Skills](docs/AGENT-SKILLS.md)
- [设计系统](docs/design-system.md)
- [MySQL 部署](docs/mysql.md)

## 本地 Skills

- `.claude/skills/interview-memory-design/SKILL.md`：改 UI / 品牌 / 布局时读；
- `.claude/skills/interview-memory-release/SKILL.md`：构建 / 重启 / 提交时读；
- `.claude/skills/interview-memory-data/SKILL.md`：改实体 / 导入导出 / 迁移时读。

## 提交

Conventional Commits：`feat:` / `fix:` / `style:` / `docs:` / `chore:`。
不要 force push；不要在未验证时宣布完成。
