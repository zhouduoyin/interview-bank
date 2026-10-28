---
name: interview-memory-release
description: Use when building, rebuilding, restarting, smoke-testing, or committing this project. Covers the frontend verify gate, stopping the running jar before packaging, starting the single jar, browser checks, and safe git commits without touching data/.
---

# 面试记忆助手 · 发布 Skill

## 前置检查

- 工作目录：项目根目录（包含 `build.ps1`、`stop.ps1`、`web/`、`server/`）；
- 运行中的服务：`http://127.0.0.1:8090`，jar 为 `server/target/interview-bank.jar`；
- 数据目录：`data/`，文件 `interview-bank.mv.db`；
- **任何情况下都不要删除 `data/`**，除非用户明确要求重置并已备份。

## 标准发布流程

### 1. 前端门禁

```powershell
cd web
npm run verify
```

`verify` = ESLint → Prettier check → Vitest → Vite build。任何一步失败都先修，不带病构建。

### 2. 停掉正在运行的 jar

```powershell
cd ..
.\stop.ps1
```

原因：Windows 下 jar 被占用时，Maven 无法重命名旧产物，会报 `Unable to rename ...jar to ...jar.original`。

### 3. 构建单 jar

```powershell
.\build.ps1
```

脚本顺序：前端 build → 复制 `web/dist` 到后端 `static` → `mvn -DskipTests package` → `server/target/interview-bank.jar`。

### 4. 启动

```powershell
java -jar server\target\interview-bank.jar
```

作为后台任务运行；确认端口 8090 已监听后再做浏览器检查。

### 5. 冒烟检查

至少覆盖：

- `/` 今日页：标题、待回想数、优先加固入口；
- `/review`：开始训练 → 核对答案 → 评分 → 完成页；
- `/questions`：搜索、分类筛选、卡片 / 表格切换；
- `/questions/{id}`：答案、记忆状态、回想历史；
- `/interviews`、`/insights`、`/method`、`/data`：能打开且无控制台报错；
- 刷新页面后路由不 404（SPA fallback）；
- 暗色主题与 375px 宽度无横向滚动。

没有 Playwright 时，用现有 CDP / `Invoke-WebRequest` 检查关键文本与 DOM，不要只凭“构建成功”宣布完成。

### 6. 提交

```powershell
git status --short
git add -A
git commit -m "feat: 重构为面试记忆助手 v2"
git push
```

提交信息用 Conventional Commits：

- `feat:` 新功能 / 新页面；
- `fix:` 修复；
- `style:` 纯样式；
- `docs:` 文档；
- `chore:` 构建 / 依赖。

## 绝对不要

- 不 `git add data/`、不提交 `.env`、不提交 `server/target/`；
- 不 `git push --force`；
- 不执行 `DELETE /api/data/all` 或 `rm -rf data/`；
- 不在 jar 运行时强行构建；
- 不跳过 `npm run verify`；
- 不为了“看起来完成”而删测试或跳过失败用例。

## 回滚

```powershell
git log --oneline -5
git revert <commit>
.\stop.ps1
.\build.ps1
java -jar server\target\interview-bank.jar
```

数据回滚：优先用 `/api/data/export` 的 JSON 备份恢复，不要直接改 H2 文件。
