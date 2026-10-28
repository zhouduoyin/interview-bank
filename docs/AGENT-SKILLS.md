# Agent Skills 选型与落地

> 这篇文档回答三件事：**哪些 GitHub Skills 值得用、为什么、怎么装**，以及**这个项目自己沉淀了哪些 skill**。
> Skills 是“给 agent 的操作手册 + 脚本 + 资源”，不是普通依赖；安装前要像 review 代码一样 review 它。

## 1. 什么是 Agent Skill

一个 skill 通常是一个目录：

```text
skill-name/
├─ SKILL.md        # YAML frontmatter（name / description）+ 操作说明
├─ scripts/        # 可选：确定性脚本
├─ references/     # 可选：按需加载的参考资料
└─ assets/         # 可选：模板、字体、图标
```

agent 先读 `description` 判断是否触发，再按需加载正文与资源。规范见 [agentskills.io](https://agentskills.io)。

对本项目的意义：

- 把“怎么改这个项目”写成可复用的操作手册，而不是每次都靠长 prompt；
- 把重复流程（设计检查、发版、数据安全）固化成 skill；
- 通过 `description` 控制触发，避免每次对话都塞满规则。

## 2. 精选 Skills

### 2.1 `frontend-design`（首选）

- 仓库：<https://github.com/anthropics/skills>
- 路径：<https://github.com/anthropics/skills/tree/main/skills/frontend-design>
- License：Apache-2.0（见该目录 `LICENSE.txt`）
- 作用：在新建或重塑 UI 时，给出非模板化的视觉方向、字体、版式和文案判断。
- 为什么适合本项目：当前“暖纸 / 墨色 / 朱砂”的视觉语言需要持续保持一致性，而不是退回通用后台模板。
- 使用方式：让 agent 先读 `SKILL.md`，再基于现有 `tokens.css` 做增量设计，不要整站换皮。
- 注意：它强调“避免无意义的 01/02/03 编号”；本项目“记忆方法”的四步是真实顺序，使用编号是合理的。

### 2.2 `webapp-testing`

- 仓库：<https://github.com/anthropics/skills>
- 路径：<https://github.com/anthropics/skills/tree/main/skills/webapp-testing>
- License：Apache-2.0
- 作用：用 Python Playwright 对本地 Web 应用做真实浏览器验证：截图、DOM 检查、控制台日志、交互回归。
- 为什么适合本项目：前端有训练流程、表格视图、主题切换，单靠单元测试覆盖不到真实交互。
- 使用方式：

  ```bash
  python scripts/with_server.py --server "npm run dev" --port 5173 -- python your_check.py
  ```

- 注意：需要 Python 3 与 Playwright；agent 不应擅自安装，先询问。没有 Playwright 时，项目现有的 CDP 检查脚本也可以完成 DOM 验证。

### 2.3 `skill-creator`

- 仓库：<https://github.com/anthropics/skills>
- 路径：<https://github.com/anthropics/skills/tree/main/skills/skill-creator>
- License：随仓库（多数 Apache-2.0；文档类技能为 source-available，使用前看目录内声明）
- 作用：创建 / 修改 / 评估 skill，包含测试用例、评分和描述触发优化。
- 为什么适合本项目：把“设计检查、发版、数据安全”写成项目本地 skill 时，用它来评估触发是否准确。
- 注意：它面向 Claude 的 eval 流程；在 DSH / Codex 中可借用方法，不必强依赖其脚本。

### 2.4 `ui-ux-pro-max`（设计检索，可选）

- 仓库：<https://github.com/nextlevelbuilder/ui-ux-pro-max-skill>
- License：MIT
- 作用：按产品类型检索 UI 风格、配色、字体搭配、图表类型和 UX 规则；带一个本地 Python 检索脚本。
- 为什么适合本项目：做新页面或设计评审时，可以快速对照“教育 / 效率工具 / 日记类”产品的常见模式。
- 使用方式：

  ```bash
  npm install -g ui-ux-pro-max-cli
  uipro init --ai claude
  ```

- 注意：**不要把它的推荐当成新设计系统直接覆盖现有 token**。本项目已经有一套成熟视觉语言，正确用法是“检索 → 对照 → 局部修正”，不是整站换风格。

### 2.5 `superpowers`（工程方法论，可选）

- 仓库：<https://github.com/obra/superpowers>
- License：MIT
- 作用：把“先澄清需求 → 写计划 → TDD → 代码审查 → 完成分支”固化成可触发 skill，强调 RED-GREEN-REFACTOR 和验证后再宣布完成。
- 为什么适合本项目：本次重构涉及前端、后端、文档、构建，多步骤且容易漏验证；TDD 与“验证后再完成”能降低回归。
- 安装（Claude Code）：

  ```text
  /plugin install superpowers@claude-plugins-official
  ```

- 注意：它会改变 agent 的工作方式，适合中大型改动；小修小补不必启用全部流程。

### 2.6 `archify`（架构图，可选）

- 仓库：<https://github.com/tt-a1i/archify>
- License：Apache-2.0（历史 MIT 授权仍有效）
- 作用：把系统描述或仓库转成可交互的架构 / 时序 / 数据流 HTML 图，自带校验。
- 为什么适合本项目：`docs/ARCHITECTURE.md` 目前用 Mermaid，足够 GitHub 阅读；如果要做分享页或面试讲解，可以再用 archify 生成独立 HTML。
- 安装：

  ```bash
  npx skills add tt-a1i/archify -g
  ```

- DSH 集成（社区维护，非官方）：

  ```bash
  dsh plugin --profile web add @tt-a1i/archify-dsh@1.0.0
  ```

- 注意：它会读取仓库并生成文件；先确认输出目录，不要把生成物混进 `src/`。本项目当前**不默认安装**，避免增加不必要的运行时。

### 2.7 采用状态

| Skill | 状态 | 在本项目里的落点 |
| --- | --- | --- |
| `frontend-design` | 采用原则 | 暖纸 / 墨色 / 朱砂的视觉方向、`docs/design-system.md`、本地 `interview-memory-design` |
| `webapp-testing` | 采用方法 | 用 CDP 做真实浏览器回归与截图；没有引入 Python / Playwright 运行时 |
| `skill-creator` | 采用结构 | 本地 skills 统一使用 `SKILL.md` + 明确触发描述；按真实任务检查触发 |
| `ui-ux-pro-max` | 仅调研 | 作为设计检索参考，不覆盖现有 token，不作为依赖 |
| `superpowers` | 仅流程参考 | 借鉴计划 / TDD / 完成后验证，不改变项目运行时 |
| `archify` | 可选 | 需要独立架构分享页时再生成，不默认安装 |

判断标准很简单：**能落到项目约束、能被验证、不会增加不必要的运行时**，才进入“采用”；否则只保留在调研清单里。

## 3. 本项目怎么用这些 Skills

| 场景 | 使用 skill | 具体做法 |
| --- | --- | --- |
| 新增页面 / 改造视觉 | `frontend-design` + 项目 `tokens.css` | 先读现有 token 与页面结构，再给一个明确的视觉方向；不引入第二套主题系统 |
| 训练流程回归 | `webapp-testing` 或现有 CDP 脚本 | 验证“未到期 → 训练 → 评分 → 完成 → 到期数变化”闭环 |
| 写项目本地 skill | `skill-creator` | 用真实任务做测试 prompt，检查 `description` 是否能稳定触发 |
| 设计评审 | `ui-ux-pro-max` | 只取检索结果中的检查项，不覆盖现有品牌 |
| 中大型重构 | `superpowers` | 先计划，再按任务实现，每个任务补测试，最后统一 review |
| 架构分享 | `archify` | 从 `docs/ARCHITECTURE.md` 的 Mermaid 出发，生成独立 HTML，不替代源码文档 |

## 4. 项目本地 Skills

位置：`.claude/skills/`。这些是**项目专属**的，不依赖外部市场，跟着仓库走。

### 4.1 `interview-memory-design`

- 文件：`.claude/skills/interview-memory-design/SKILL.md`
- 触发：改前端页面、设计 token、品牌文案、响应式布局时。
- 内容：品牌事实、颜色 / 字体约束、页面结构、禁止事项、验证清单。

### 4.2 `interview-memory-release`

- 文件：`.claude/skills/interview-memory-release/SKILL.md`
- 触发：准备构建、重启、发版、提交前。
- 内容：`npm run verify` → 停服务 → 构建 → 启动 → 冒烟检查 → 提交信息规范；明确**不要删 `data/`**。

### 4.3 `interview-memory-data`

- 文件：`.claude/skills/interview-memory-data/SKILL.md`
- 触发：导入导出、数据库迁移、清空数据、修改实体字段时。
- 内容：数据文件位置、备份优先、去重规则、危险操作红线、迁移检查。

## 5. 安装与安全

### 5.1 安装前检查

1. 打开 `SKILL.md`，确认 `name`、`description`、触发条件；
2. 看 `scripts/` 是否有网络请求、文件删除、`sudo`、包安装；
3. 看 `LICENSE` / `LICENSE.txt`，确认是否允许你的使用场景；
4. 固定到某个 commit 或 release，不要永远拉 `main`；
5. 先在临时目录或只读模式跑一次。

### 5.2 安全红线

- 不把 API key、数据库密码、`.env` 内容放进 skill；
- 不允许 skill 执行 `rm -rf data/`、`DELETE /api/data/all`、`git push --force`；
- 不盲目安装“热门但无人 review”的 skill；
- 不让 skill 自动安装全局软件；需要时先问用户；
- 生成的文件要落在明确目录，不污染 `src/`、`server/src/`。

### 5.3 License 备注

| Skill | License | 备注 |
| --- | --- | --- |
| `frontend-design` | Apache-2.0 | 该目录有独立 `LICENSE.txt` |
| `webapp-testing` | Apache-2.0 | 需要 Playwright |
| `skill-creator` | 随仓库 | 使用前看目录声明 |
| `ui-ux-pro-max` | MIT | 免费版与付费版功能不同 |
| `superpowers` | MIT | 会改变 agent 工作流 |
| `archify` | Apache-2.0 | 历史 MIT 授权仍有效 |

> 以上信息以各仓库当前 `LICENSE` 为准；商用或再分发前请自行复核。

## 6. 评估一个 Skill 是否值得留下

| 维度 | 问题 |
| --- | --- |
| 触发准确 | 该触发时触发，不该触发时不打扰？ |
| 可验证 | 有测试 / 检查清单 / 可复现命令？ |
| 可回滚 | 失败时能否撤销，是否只读优先？ |
| 维护成本 | 是否引入新运行时、全局依赖、频繁变更？ |
| 与项目一致 | 是否尊重现有 token、路由、数据模型？ |
| 安全 | 是否读敏感文件、发网络请求、执行删除？ |

一个 skill 如果只是“看起来很强”，但不能被验证、不能回滚、和现有架构冲突，就不应该进入项目。

## 7. 参考链接

- Agent Skills 规范：<https://agentskills.io>
- Anthropic Skills：<https://github.com/anthropics/skills>
- UI UX Pro Max：<https://github.com/nextlevelbuilder/ui-ux-pro-max-skill>
- Superpowers：<https://github.com/obra/superpowers>
- Archify：<https://github.com/tt-a1i/archify>
- Archify DSH 集成：<https://github.com/tt-a1i/archify/tree/main/integrations/deepseek-harness>
