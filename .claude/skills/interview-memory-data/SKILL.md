---
name: interview-memory-data
description: Use when changing this project's database entities, imports/exports, migrations, demo seeding, or any operation that could delete questions, reviews, interviews, categories, or tags. Protects the local H2 file and enforces backup-before-migration.
---

# 面试记忆助手 · 数据安全 Skill

## 数据在哪里

| 模式 | 位置 | 说明 |
| --- | --- | --- |
| H2（默认） | `data/interview-bank.mv.db` | 本地文件，单用户；不要改名，脚本引用此路径 |
| MySQL | `interview_bank` 库 | 由 `DB_URL / DB_USERNAME / DB_PASSWORD` 控制 |
| 备份 | `/api/data/export` | JSON 全量；`/api/data/export/markdown` 为 Markdown |

当前基线数据：**95 道题 / 58 条回想记录**（以实际库为准，改动前先导出确认）。

## 红线

- 不删除、不重命名 `data/interview-bank.mv.db`；
- 不重命名 `server/target/interview-bank.jar`；
- 不执行 `DELETE /api/data/all`；
- 不在没有备份的情况下改实体字段或跑迁移；
- 不把真实数据提交进 Git；
- 不直接编辑 H2 文件。

## 改数据模型前

1. 先导出 JSON 备份，记录当前题数 / 回想数 / 面试数；
2. 确认 H2 与 MySQL 两套 schema 都能建表；
3. 新增字段必须可空或有默认值，避免旧库启动失败；
4. 枚举新增值要考虑旧数据反序列化；
5. 如果改了 `Question` 字段，检查：
   - `QuestionRequest` 校验；
   - `QuestionService.create/update`；
   - 导入导出 DTO；
   - 前端表单与详情页；
6. 启动后检查 `/api/stats/dashboard` 与 `/api/questions?size=1`。

## 导入规则

- 按**题目标题**去重；已存在题目跳过，不覆盖手写答案；
- 分类不存在时自动创建；
- 导入结果返回 `questions / interviews / skipped`；
- 导入前提示用户先导出当前数据。

## 清空规则

| 操作 | 范围 | 是否可恢复 |
| --- | --- | --- |
| 清除示例数据 | 仅 `demo=true` 的题目 / 分类 / 面试 | 否，但只影响示例 |
| 清空全部 | 题目、分类、标签、面试、回想历史 | 否，必须先备份 |

前端危险操作必须二次确认；后端 `DELETE /api/data/all` 必须保留确认参数 / 明确语义，不允许 GET 触发。

## 迁移检查清单

- [ ] 已导出 JSON 备份；
- [ ] 新旧实体字段兼容（可空 / 默认值）；
- [ ] H2 启动成功；
- [ ] MySQL profile 启动成功（如使用）；
- [ ] 题目数、回想数、面试数与备份一致；
- [ ] 导入同一份 JSON 不产生重复；
- [ ] 前端列表、详情、统计能正常读取新字段；
- [ ] 回滚方案已写明。

## 恢复

1. 停止服务；
2. 用备份 JSON 通过 `/api/data/import` 导入；
3. 若需完全重置：先复制一份 `data/` 到项目外，再删除 `data/` 让应用重建；
4. 恢复后核对题数、分类数、最近回想时间。
