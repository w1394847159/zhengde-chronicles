package com.zhengde.chronicles.game.world

import com.zhengde.chronicles.game.engine.TurnSnapshot

/**
 * 世界状态 — 游戏的全量状态快照
 *
 * 每个回合产生一个不可变的 WorldState 实例，
 * 由 StateManager 通过增量更新创建新实例。
 */
data class WorldState(
    // ========== 回合信息 ==========
    override val turn: Int = 0,
    override val year: Int = 1505,
    override val month: Int = 5,       // 朱厚照五月登基
    override val day: Int = 1,

    // ========== 国势维度 ==========
    val treasury: Int = 3000,         // 国库（万两）
    val granary: Int = 2000,         // 粮储（万石）
    val military: Int = 500,         // 军力指数 0-999
    val borderThreat: Int = 40,      // 边患 0-100
    val courtStability: Int = 60,    // 朝纲 0-100
    val corruption: Int = 70,        // 贪腐 0-100（正德朝开局贪腐严重）

    // ========== 皇帝个人维度 ==========
    val prestige: Int = 50,          // 威望 0-100
    val energy: Int = 80,            // 精力 0-100
    val playfulness: Int = 60,       // 玩心 0-100 🔥 核心特色
    val martialSkill: Int = 40,      // 武艺 0-100
    val politicalWisdom: Int = 30,   // 政治智慧 0-100

    // ========== 省份状态 ==========
    val provinces: Map<String, ProvinceState> = defaultProvinces(),

    // ========== 派系关系 ==========
    val factionRelations: Map<String, Int> = defaultFactionRelations(),

    // ========== 大臣状态 ==========
    val ministerStates: Map<String, MinisterState> = emptyMap(),

    // ========== 剧情标记 ==========
    val flags: Map<String, Boolean> = emptyMap(),
    val activeEvents: List<ActiveEvent> = emptyList(),
    val completedEvents: List<String> = emptyList(),

    // ========== 变更日志 ==========
    val changeLog: List<ChangeEntry> = emptyList(),

    // ========== 快照元信息 ==========
    override val timestamp: Long = System.currentTimeMillis(),
    val snapshotId: String = ""
) : TurnSnapshot {

    companion object {
        const val MAX_TREASURY = 9999
        const val MAX_GRANARY = 5000
        const val MAX_STAT = 100
        const val MIN_STAT = 0

        /** 正德元年五月开局默认状态 */
        fun createDefault(): WorldState = WorldState()
    }

    // ========== 便捷查询方法 ==========

    /** 获取某省民心 */
    fun popularSupport(province: String): Int =
        provinces[province]?.popularSupport ?: 50

    /** 获取某省驻军 */
    fun garrison(province: String): Int =
        provinces[province]?.garrison ?: 30

    /** 获取派系关系值 */
    fun factionRelation(faction: String): Int =
        factionRelations[faction] ?: 50

    /** 判断某个剧情标记是否激活 */
    fun hasFlag(flag: String): Boolean = flags[flag] == true
}

// ========== 省份状态 ==========

data class ProvinceState(
    val name: String,
    val popularSupport: Int = 60,    // 民心 0-100
    val garrison: Int = 30,          // 驻军 0-100
    val taxRevenue: Int = 50,        // 税收贡献 0-100
    val grainOutput: Int = 50,       // 粮产 0-100
    val stability: Int = 60,         // 治安 0-100
    val disasterRisk: Int = 10,      // 灾害风险 0-100
    val specialResources: List<String> = emptyList()
)

// ========== 派系关系 ==========

data class FactionRelation(
    val name: String,
    val relation: Int = 50,          // -100 ~ 100
    val influence: Int = 50          // 势力 0-100
)

// ========== 活跃事件 ==========

data class ActiveEvent(
    val id: String,
    val title: String,
    val description: String,
    val type: EventType,
    val deadline: Int,               // 必须在几回合内处理
    val turnCreated: Int,
    val status: EventStatus = EventStatus.PENDING
)

enum class EventType {
    THRESHOLD_TRIGGERED,    // 阈值触发（民心低→民变）
    NARRATIVE_DRIVEN,       // LLM 推演生成的剧情事件
    HISTORICAL_CALLBACK,    // 历史事件（宁王造反等）
    FACTION_CRISIS,         // 派系危机
    NATURAL_DISASTER        // 天灾
}

enum class EventStatus {
    PENDING, RESOLVED, EXPIRED
}

// ========== 变更日志 ==========

data class ChangeEntry(
    val turn: Int,
    val edictSummary: String,
    val changes: Map<String, Pair<Int, Int>>,  // key -> (旧值, 新值)
    val causation: String
)

// ========== 默认数据 ==========

fun defaultProvinces(): Map<String, ProvinceState> = mapOf(
    "北直隶" to ProvinceState("北直隶", popularSupport = 55, garrison = 50, taxRevenue = 60),
    "南直隶" to ProvinceState("南直隶", popularSupport = 60, garrison = 30, taxRevenue = 80),
    "陕西" to ProvinceState("陕西", popularSupport = 45, garrison = 35, taxRevenue = 30, disasterRisk = 35),
    "山西" to ProvinceState("山西", popularSupport = 50, garrison = 40, taxRevenue = 35, disasterRisk = 20),
    "山东" to ProvinceState("山东", popularSupport = 55, garrison = 25, taxRevenue = 50),
    "河南" to ProvinceState("河南", popularSupport = 55, garrison = 20, taxRevenue = 45, disasterRisk = 25),
    "湖广" to ProvinceState("湖广", popularSupport = 60, garrison = 20, taxRevenue = 55),
    "江西" to ProvinceState("江西", popularSupport = 58, garrison = 15, taxRevenue = 45),
    "浙江" to ProvinceState("浙江", popularSupport = 65, garrison = 15, taxRevenue = 65),
    "福建" to ProvinceState("福建", popularSupport = 55, garrison = 10, taxRevenue = 30),
    "广东" to ProvinceState("广东", popularSupport = 60, garrison = 20, taxRevenue = 40),
    "四川" to ProvinceState("四川", popularSupport = 55, garrison = 25, taxRevenue = 35),
    "云南" to ProvinceState("云南", popularSupport = 50, garrison = 20, taxRevenue = 20, specialResources = listOf("铜矿"))
)

fun defaultFactionRelations(): Map<String, Int> = mapOf(
    "八虎" to 80,
    "内阁" to 40,
    "边军" to 50,
    "豹房近幸" to 70,
    "宗室" to 45,
    "心学" to 10
)
