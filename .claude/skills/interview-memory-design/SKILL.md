---
name: interview-memory-design
description: Use when editing this project's frontend UI, brand copy, design tokens, navigation, responsive layout, or visual review. Keeps the "面试记忆助手" warm-paper / ink / cinnabar language consistent and prevents generic admin-dashboard drift.
---

# 面试记忆助手 · 设计 Skill

## 何时使用

- 新增或修改 `web/src/views/` 页面；
- 改 `web/src/styles/tokens.css`、`main.css`、`theme.css`；
- 改导航、品牌名、页面标题、按钮文案；
- 做响应式、深浅色、表格视图、空状态；
- 设计评审或 UI 回归。

## 品牌事实

- 产品名：**面试记忆助手**；
- 英文：Interview Memory Assistant；
- 标记：**忆**；
- Slogan：**把每一次面试，变成下一次的答案**；
- 描述：记录题目 · 间隔回想 · 面试复盘；
- 品牌配置唯一来源：`web/src/config/brand.js`；
- 导航唯一来源：`web/src/config/navigation.js`。

不要在其他组件里重复硬编码产品名；改品牌只改 `brand.js`。

## 视觉方向

- 关键词：暖纸、墨色、朱砂批注、手记、克制；
- 主色：朱砂红（`--primary`），用于行动、重点、危险；
- 背景：暖纸色；侧栏：墨色；卡片：纸面 + 细边框 + 硬阴影；
- 字体：衬线标题（`--font-display`）+ 无衬线正文 + 等宽小标签；
- 圆角克制（3–7px），阴影偏“纸张叠放”，不要大面积玻璃拟态；
- 动效 150–200ms，只用于反馈，不做无意义入场动画。

禁止：

- 改成蓝紫渐变的通用后台；
- 引入第二套颜色系统或 Tailwind；
- 用 emoji 代替图标（完成态 emoji 文案除外）；
- 为“好看”加无意义的 01/02/03 编号（真实步骤除外）；
- 在页面里写死 HEX，必须走 token。

## 页面结构

| 路由 | 页面 | 主职责 |
| --- | --- | --- |
| `/` | 今日 | 今日待回想、连续天数、薄弱分类、最近记录 |
| `/review` | 记忆训练 | 隐藏答案 → 主动回想 → 评分 |
| `/questions` | 题库 | 搜索、筛选、卡片 / 表格 |
| `/questions/:id` | 题目详情 | 答案、记忆状态、回想历史、关联面试 |
| `/interviews` | 面试档案 | 面试列表与复盘 |
| `/insights` | 成长洞察 | 趋势、分类记忆结构、难度 / 标签 |
| `/method` | 记忆方法 | 四步闭环、评分规则、日常节奏 |
| `/data` | 设置与数据 | 导入导出、分类标签、危险操作 |

页面统一使用 `.page`，区块用 `.card` / `.hero-card`，头部用 `.card-head`。新增页面先复用现有 class，再写 scoped 样式。

## 文案规则

- 用“回想 / 记忆训练 / 加固中 / 长期记忆 / 待开始”，不用“复习 / 已掌握 / 未掌握”（历史 API 字段除外）；
- 按钮说清楚结果：“开始记忆训练”“核对答案”“记一道题”，不用“提交”；
- 空状态给下一步，不卖萌、不道歉；
- 错误说清发生了什么、怎么修；
- 标题短，正文不超过 80 字符一行。

## 修改流程

1. 先读 `web/src/styles/tokens.css` 和目标页面；
2. 确认要用的 token 已存在，缺什么先补 token，不写死颜色；
3. 优先复用 `.card`、`.hero-card`、`.stat-grid`、`.empty` 等现有结构；
4. 新页面同时在 `web/src/router/index.js` 注册标题，必要时在 `navigation.js` 加入口；
5. 同时检查亮色 / 暗色、窄屏（375 / 768 / 1440）；
6. 跑 `cd web; npm run lint; npm run test; npm run build`。

## 验收清单

- [ ] 产品名、导航、标题来自 `config/`；
- [ ] 没有新增硬编码颜色；
- [ ] 键盘可达：按钮可 Tab，焦点可见；
- [ ] 对比度 ≥ 4.5:1（正文）；
- [ ] 375px 下不横向滚动；
- [ ] 暗色主题下不出现低对比文字；
- [ ] 空状态 / 加载态 / 错误态都有；
- [ ] 不破坏已有路由与书签。
