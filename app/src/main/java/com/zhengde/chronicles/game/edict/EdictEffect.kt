package com.zhengde.chronicles.game.edict

import com.zhengde.chronicles.game.world.ActiveEvent

/**
 * LLM 推演结果 — 结构化数值变更
 *
 * 对应 LLM Prompt 中要求的 JSON Schema 输出。
 * Prompt 中强制要求 LLM 必须按此结构输出，EffectSystem 解析并校验。
 */
data class EdictEffect(
    // ===== 叙事摘要（给玩家看） =====
    val summary: String = "",

    // ===== 国势变化 =====
    val treasuryDelta: Int = 0,
    val granaryDelta: Int = 0,
    val militaryDelta: Int = 0,
    val borderThreatDelta: Int = 0,
    val courtStabilityDelta: Int = 0,
    val corruptionDelta: Int = 0,

    // ===== 皇帝属性变化 =====
    val prestigeDelta: Int = 0,
    val energyDelta: Int = 0,
    val playfulnessDelta: Int = 0,
    val martialSkillDelta: Int = 0,
    val politicalWisdomDelta: Int = 0,

    // ===== 省份民心变化 =====
    val popularSupportDelta: Map<String, Int> = emptyMap(),

    // ===== 派系关系变化 =====
    val factionRelationDelta: Map<String, Int> = emptyMap(),

    // ===== 剧情标记 =====
    val setFlags: List<String> = emptyList(),
    val clearFlags: List<String> = emptyList(),

    // ===== 新事件 =====
    val newEvents: List<ActiveEvent> = emptyList(),

    // ===== 因果说明（给 MemorySystem 用的摘要） =====
    val causation: String = "",

    // ===== 意外连锁反应描述（纯粹叙事用） =====
    val unexpectedConsequences: String = ""
)
