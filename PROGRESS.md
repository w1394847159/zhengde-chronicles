# 《正德风云录》开发进度

> 项目状态：🟢 进行中 | 最后更新：2026-07-17

---

## 阶段总览

| 阶段 | 状态 | 完成日期 | 说明 |
|------|------|---------|------|
| P0 项目骨架搭建 | ✅ 已完成 | 2026-07-17 | Gradle 配置、项目结构、GitHub Actions、首次 CI 通过 |
| P1 核心引擎 | ✅ 已完成 | 2026-07-17 | WorldEngine, StateManager, EffectSystem, EventSystem, MemorySystem, NarrativeSystem |
| P2 LLM 集成 | ✅ 已完成 | 2026-07-17 | LlmClient, PromptBuilder, TokenTracker |
| P3 数据持久化 | 🟡 进行中 | — | Room 数据库、DAO、Repository、存档管理 |
| P4 基础 UI | 🟡 进行中 | — | MainGameScreen 已与 ViewModel 集成 |
| P5 特色系统 | 🔴 未开始 | — | 豹房、亲征、微服、八虎、王阳明 |
| P6 数据填充与打磨 | 🔴 未开始 | — | 初始数据、Prompt 调优、数值平衡 |
| P7 发布 | 🔴 未开始 | — | Release APK、README 完善 |

---

## 详细进度

### P0：项目骨架搭建

- [x] 创建 GitHub 仓库 ✅
- [x] Gradle 配置文件
- [x] 项目目录结构
- [x] AndroidManifest.xml
- [x] GitHub Actions workflow
- [x] .gitignore
- [x] 首次提交与 CI 验证 🟡 等待 CI 结果

### P1：核心引擎

- [x] WorldState 数据模型（国势/皇帝/省份/派系 4 大类 20+ 维度）
- [x] StateManager（步进状态机，增量更新，快照管理，回滚）
- [x] EffectSystem（LLM 输出解析，JSON 校验，clamp 机制）
- [x] EventSystem（阈值检测，剧烈变化检测，派系危机检测）
- [x] MemorySystem（三层分层记忆，Level 0-3）
- [x] NarrativeSystem（明清白话叙事渲染）
- [x] WorldEngine（主循环，串联全流程）

### P2：LLM 集成

- [x] LlmClient（OkHttp + OpenAI 兼容 API）
- [x] PromptBuilder（系统指令 + 状态注入 + 记忆分层 + 输出约束）
- [x] TokenTracker（Token 估算，费用追踪）
- [ ] Prompt 实测调优

### P3：数据持久化

- [x] Room 数据库配置
- [x] 类型转换器（Converters）
- [x] WorldStateEntity（全量快照 JSON 存储）
- [x] EdictLogEntity（诏书记录）
- [x] GameSaveEntity（存档元信息）
- [x] TokenRecordEntity（Token 消耗记录）
- [x] GameDao（全部 CRUD 操作）
- [x] AppDatabase（Room 数据库单例）
- [x] GameRepository（数据仓库层）
- [x] Application 初始化数据库连接

### P4：基础 UI

- [ ] 主题（暗金/朱红/墨黑）
- [ ] 数值面板组件
- [ ] 诏书输入界面
- [ ] 推演结果展示
- [ ] 事件流/奏折列表
- [ ] 设置界面（API Key 配置）
- [ ] 存档管理界面
- [ ] Token 统计界面

### P5：特色系统

- [ ] 豹房系统
- [ ] 亲征系统
- [ ] 微服私访系统
- [ ] 八虎博弈系统
- [ ] 王阳明事件链

### P6：数据填充与打磨

- [ ] 初始大臣数据
- [ ] 派系初始关系
- [ ] 省份数据
- [ ] 事件阈值配置
- [ ] Prompt 调优
- [ ] 数值平衡测试

### P7：发布

- [ ] README.md
- [ ] Release APK
- [ ] 使用文档

---

## 当前正在做的事

> **P0：项目骨架搭建** — 创建 Gradle 配置文件、项目结构、GitHub Actions
