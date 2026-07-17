# 《正德风云录》开发进度

> 项目状态：🟢 进行中 | 最后更新：2026-07-17

---

## 阶段总览

| 阶段 | 状态 | 完成日期 | 说明 |
|------|------|---------|------|
| P0 项目骨架搭建 | 🔴 未开始 | — | Gradle 配置、项目结构、GitHub Actions |
| P1 核心引擎 | 🔴 未开始 | — | WorldEngine, StateManager, EffectSystem |
| P2 LLM 集成 | 🔴 未开始 | — | LlmClient, PromptBuilder, TokenTracker |
| P3 数据模型与存储 | 🔴 未开始 | — | Room 数据库、数据结构定义 |
| P4 基础 UI | 🔴 未开始 | — | Compose 界面、诏书输入、数值面板 |
| P5 特色系统 | 🔴 未开始 | — | 豹房、亲征、微服、八虎、王阳明 |
| P6 数据填充与打磨 | 🔴 未开始 | — | 初始数据、Prompt 调优、数值平衡 |
| P7 发布 | 🔴 未开始 | — | Release APK、README 完善 |

---

## 详细进度

### P0：项目骨架搭建

- [ ] 创建 GitHub 仓库 ✅
- [ ] Gradle 配置文件
- [ ] 项目目录结构
- [ ] AndroidManifest.xml
- [ ] GitHub Actions workflow
- [ ] .gitignore
- [ ] 首次提交与 CI 验证

### P1：核心引擎

- [ ] TimeSystem（年/月/日/回合）
- [ ] WorldState 数据模型
- [ ] StateManager（步进状态机）
- [ ] EffectSystem（效果解析与校验）
- [ ] EventSystem（事件检测与生成）
- [ ] MemorySystem（分层记忆）
- [ ] NarrativeSystem（叙事渲染）

### P2：LLM 集成

- [ ] LlmClient（OkHttp + API 调用）
- [ ] PromptBuilder（Prompt 工程）
- [ ] TokenTracker（消耗追踪）
- [ ] TokenEstimator（预估）
- [ ] 错误重试与容错机制

### P3：数据模型与存储

- [ ] Room 数据库
- [ ] GameDao
- [ ] 实体定义
- [ ] GameRepository
- [ ] DefaultData（初始数据）
- [ ] 存档管理

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
