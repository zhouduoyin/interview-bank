# 面试题库 · 面试手记

一个给自己用的面试题记录 + 间隔复习 + 面试复盘工具。前后端分离，数据全部保存在本机，不依赖任何第三方服务。

> 记录每一道被问到的题，写下自己的答案，在忘记之前复习它。

界面采用「暖纸 · 墨色 · 朱砂」的**面试手记**视觉体系：像一本摊在桌上的工作笔记，而不是又一个蓝紫渐变后台。完整设计规范见 [docs/design-system.md](docs/design-system.md)，启动后也可以访问 `/styleguide` 查看活文档。

![工作台](docs/screenshots/dashboard.png)

## 功能

- **题库管理**：新建 / 编辑 / 删除题目，支持 Markdown 书写题目描述与参考答案（代码高亮、表格、引用），支持分类、难度、标签、来源公司 / 岗位、收藏。
- **快速记题**：任意页面 `Ctrl + N` 呼出快速记录框，先记下题目，之后再补答案。
- **间隔复习**：内置 SM-2 简化版算法，根据「忘记 / 有点难 / 记得 / 很简单」自动安排下次复习时间；今日复习页提供翻卡式自测，支持键盘操作。
- **题目详情**：参考答案可折叠，复习历史时间线，掌握程度快捷调整，展示这道题出现在哪些面试里。
- **面试记录**：按场次记录公司、岗位、轮次、日期、渠道、薪资、结果、自评与复盘，可关联题库中的题目，写下面试官追问和自己的回答。
- **数据洞察**：复习热力图、30 天趋势、分类掌握度、难度分布、高频标签、薄弱分类提醒、连续复习天数。
- **数据管理**：JSON 一键导出 / 导入（按标题去重合并）、Markdown 导出、分类与标签管理、清除示例数据、清空全部数据。
- **体验细节**：亮 / 暗色主题、侧栏折叠、全局搜索（`Ctrl + K`）、列表卡片 / 表格两种视图、批量操作、分页、空状态引导、响应式布局。

## 设计：把面试准备写成一本手记

这套界面不是套模板，而是先定了概念，再落成一套可复用的设计系统。

- **纸先于屏**：暖纸底色、1px 细线、3px 硬阴影，容器先是「纸片」再是「卡片」。
- **墨分五色，只用一个强调色**：正文用墨，真正的重点用朱砂；苔绿 / 赭石 / 石青只做分类与状态。
- **衬线负责记忆，无衬线负责效率**：标题、数字用衬线；正文、表单用无衬线；日期、编号、标签用等宽。
- **动效是纸的惯性**：只动 `transform / opacity`，120 / 180 / 260ms，尊重 `prefers-reduced-motion`。

亮暗双主题共用一套 CSS 变量，Element Plus 也映射到同一套 token；分类色通过 `color-mix` 随主题自适应。设计规范页包含色板、字号、间距、组件、动效与可访问性清单。

![设计规范](docs/screenshots/styleguide.png)

- 活文档：<http://127.0.0.1:8090/styleguide>
- 完整规范：[docs/design-system.md](docs/design-system.md)

## 界面预览

### 题库

卡片 / 表格双视图，支持关键词、分类、难度、掌握度、标签、收藏筛选。

![题库](docs/screenshots/questions.png)

### 题目详情

Markdown 渲染的题目与答案、复习状态、历史记录、关联面试。

![题目详情](docs/screenshots/question-detail.png)

### 今日复习

先自己想，再翻答案，然后用四个按钮决定下次什么时候再见。

![今日复习](docs/screenshots/review.png)

### 快速记题

`Ctrl + N` 随时记录，先把题留住，答案可以稍后补。

![快速记题](docs/screenshots/quick-add.png)

### 面试记录

一场面试一条记录，关联被问到的题目，复盘时能串起来看。

![面试记录](docs/screenshots/interviews.png)

### 数据洞察

复习趋势、分类掌握度、难度分布、高频标签和需要加强的方向。

![数据洞察](docs/screenshots/insights.png)

### 暗色主题

![暗色主题](docs/screenshots/dashboard-dark.png)

## 快速开始

### 环境要求

- JDK 17 及以上（开发时使用 JDK 21 运行）
- 只有重新构建前端时才需要 Node.js 18+ 与 Maven

### 直接运行（拿到的是已构建目录）

本地这份目录已经包含构建好的 `server/target/interview-bank.jar`，**无需联网安装依赖**即可运行。

双击 `start.bat`（推荐），或在项目根目录执行：

```powershell
java -jar server\target\interview-bank.jar
```

浏览器访问 <http://127.0.0.1:8090>。

### 从 GitHub 克隆（只有源码）

仓库按常规忽略 `node_modules/`、`web/dist/` 与 `server/target/`，因此第一次需要自行构建：

```powershell
cd web
npm install
cd ..
.\build.ps1
java -jar server\target\interview-bank.jar
```

构建需要 JDK 17+、Maven 与 Node.js 18+，并保持网络可用以下载 Maven / npm 依赖。构建完成后同样访问 <http://127.0.0.1:8090>。

首次启动会自动建库并写入 20 道示例题、10 个分类、2 条面试记录，方便直接体验；这些示例数据可以在「数据管理」里一键清除，不会影响你自己记录的内容。

停止服务：

```powershell
.\stop.ps1
```

或直接关闭运行 `java -jar` 的窗口。

### 端口

默认使用 `8090`。如果端口被占用，可以换一个：

```powershell
$env:SERVER_PORT=8091; java -jar server\target\interview-bank.jar
```

## 开发模式

后端（Spring Boot，端口 8090，热重启需手动）：

```powershell
cd server
mvn spring-boot:run
```

前端（Vite，端口 5173，`/api` 自动代理到 8090）：

```powershell
cd web
npm run dev
```

打开 <http://127.0.0.1:5173>。

### 重新构建

```powershell
.\build.ps1
```

脚本会依次执行：前端 `npm run build` → 把 `web/dist` 复制进后端 `src/main/resources/static` → `mvn -DskipTests package`，最终产物为 `server/target/interview-bank.jar`（前端已打进 jar，单文件即可运行）。

## 快捷键

| 快捷键 | 位置 | 作用 |
| --- | --- | --- |
| `Ctrl / ⌘ + K` | 全局 | 聚焦顶栏搜索 |
| `Ctrl / ⌘ + N` | 全局 | 快速记一道题 |
| `Ctrl / ⌘ + S` | 题目 / 面试编辑页 | 保存 |
| `空格` / `Enter` | 今日复习 | 显示答案 |
| `1` `2` `3` `4` | 今日复习 | 忘记 / 有点难 / 记得 / 很简单 |

## 复习算法

采用 SM-2 的简化版本，只保留对个人复习最有用的部分：

| 评价 | 下次间隔 | 难度系数 ease |
| --- | --- | --- |
| 忘记了 | +1 天 | −0.25（最低 1.3） |
| 有点难 | 上次间隔 × 1.2 | 不变 |
| 记得 | 上次间隔 × ease | 不变 |
| 很简单 | 上次间隔 × ease × 1.3 | +0.1（最高 3.2） |

间隔限制在 1–365 天。复习次数达到 3 次或下次间隔 ≥ 7 天时，题目自动标记为「已掌握」。

## 数据说明

- 数据保存在运行目录下的 `data/` 文件夹（H2 文件数据库，`interview-bank.mv.db`），单机单用户，无登录。
- 备份：直接复制 `data/` 文件夹，或在「数据管理」里导出 JSON。
- 恢复：把 JSON 导入即可；导入按标题去重，不会覆盖已有题目。
- 重置：停止服务后删除 `data/` 文件夹，下次启动会重新生成示例数据。
- 清空全部：在「数据管理」里输入「清空」确认，会删除题目、分类、标签、面试记录与复习历史。

## 技术栈

- 后端：Spring Boot 3.5、Spring Web、Spring Data JPA、Bean Validation、H2、Lombok
- 前端：Vue 3、Vue Router 4、Element Plus、Vite、marked + DOMPurify + highlight.js、dayjs
- 样式：设计令牌 + 原生 CSS；亮 / 暗双主题；Element Plus 变量映射；无第三方 UI 模板依赖

## 目录结构

```
interview-bank/
├─ server/                       Spring Boot 后端
│  ├─ src/main/java/com/interviewbank/
│  │  ├─ domain/                 实体与枚举
│  │  ├─ repo/                   JPA Repository
│  │  ├─ service/                业务逻辑、示例数据、统计
│  │  ├─ web/                    REST Controller 与 SPA 转发
│  │  └─ common/                 统一返回、异常处理
│  ├─ src/main/resources/
│  │  ├─ application.yml
│  │  └─ static/                 构建后的前端静态资源
│  └─ target/interview-bank.jar  可直接运行的产物
├─ web/                          Vue 3 前端
│  ├─ src/
│  │  ├─ views/                  工作台、题库、复习、面试、洞察、数据、设计规范
│  │  ├─ components/             Markdown 编辑器 / 阅读器、热力图等
│  │  ├─ layout/                 侧栏与顶栏
│  │  ├─ store/                  轻量全局状态
│  │  └─ styles/
│  │     ├─ tokens.css           设计 token（颜色、字体、间距、动效、EP 变量）
│  │     ├─ main.css             结构、布局与组件骨架
│  │     └─ theme.css            视觉层（纸感、墨色侧栏、组件外观、响应式）
│  └─ dist/                      构建产物
├─ docs/
│  ├─ design-system.md           完整设计规范
│  └─ screenshots/               界面截图
├─ start.bat / start.ps1         一键启动
├─ stop.ps1                      停止服务
├─ build.ps1                     一键构建
└─ data/                         运行后生成的本地数据库（不提交）
```

## 主要接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/stats/dashboard` | 工作台统计 |
| GET | `/api/stats/insights` | 数据洞察 |
| GET / POST | `/api/questions` | 题目列表（关键词、分类、难度、掌握度、标签、收藏、排序、分页）/ 新建 |
| GET / PUT / DELETE | `/api/questions/{id}` | 题目详情 / 更新 / 删除 |
| PATCH | `/api/questions/{id}/star` | 切换收藏 |
| PATCH | `/api/questions/{id}/mastery?value=` | 修改掌握程度 |
| POST | `/api/questions/{id}/review` | 提交一次复习评价 |
| GET | `/api/questions/{id}/reviews` | 复习历史 |
| GET | `/api/review/queue?limit=` | 今日复习队列 |
| GET / POST | `/api/interviews` | 面试列表 / 新建 |
| GET / PUT / DELETE | `/api/interviews/{id}` | 面试详情 / 更新 / 删除 |
| GET | `/api/data/export` | 导出 JSON |
| GET | `/api/data/export/markdown` | 导出 Markdown |
| POST | `/api/data/import` | 导入 JSON |
| DELETE | `/api/data/demo` | 清除示例数据 |
| DELETE | `/api/data/all` | 清空全部数据 |

## 常见问题

**启动后浏览器提示无法连接？**  
首次启动需要初始化数据库，约 20 秒；`start.ps1` 会等接口就绪后再打开浏览器。若手动启动，请看到 `Started InterviewBankApplication` 后再访问。

**8090 端口被占用？**  
用 `stop.ps1` 检查并停止旧实例，或通过 `SERVER_PORT` 换端口。

**想把自己的数据带到另一台电脑？**  
在「数据管理」导出 JSON，把文件拷过去后导入即可；也可以直接复制 `data/` 文件夹。

**示例数据会一直存在吗？**  
只在第一次启动时写入一次。点击「清除示例数据」后不会再自动生成，除非删掉整个 `data/` 目录重新开始。
