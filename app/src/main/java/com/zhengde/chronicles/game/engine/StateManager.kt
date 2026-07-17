package com.zhengde.chronicles.game.engine

import com.zhengde.chronicles.game.world.WorldState
import com.zhengde.chronicles.game.world.ChangeEntry
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 步进状态机 — 核心设计
 *
 * 与《历史模拟器：崇祯》架构一致：
 * 每个回合=一个不可变状态快照，增量更新产生下一个快照。
 *
 * 设计要点：
 * - 全量快照每 10 回合保存一次（Room 存储）
 * - 增量变更日志保留最近 100 条
 * - 支持回滚到指定回合（保留最近 50 个检查点）
 */
@Singleton
class StateManager @Inject constructor() {

    /** 当前状态 */
    private var _currentState: WorldState = WorldState.createDefault()
    val currentState: WorldState get() = _currentState

    /** 历史快照索引 (turn -> WorldState) */
    private val snapshots = mutableMapOf<Int, WorldState>()

    /** 增量变更日志 */
    private val changeLog = mutableListOf<ChangeEntry>()

    // ========== 核心方法 ==========

    /**
     * 初始化世界（开局）
     */
    fun initialize(startYear: Int): WorldState {
        val state = if (startYear == 1505) {
            WorldState.createDefault()
        } else {
            WorldState.createDefault().copy(year = startYear)
        }
        _currentState = state
        snapshots[0] = state
        changeLog.clear()
        return state
    }

    /**
     * 应用 LLM 推演结果，生成新状态快照
     *
     * @param effect 推演效果
     * @return 新状态
     */
    fun applyEffect(effect: EdictEffect): WorldState {
        val prev = _currentState

        // ===== 国势维度更新 =====
        val newTreasury = clamp(prev.treasury + effect.treasuryDelta, 0, WorldState.MAX_TREASURY)
        val newGranary = clamp(prev.granary + effect.granaryDelta, 0, WorldState.MAX_GRANARY)
        val newMilitary = clamp(prev.military + effect.militaryDelta, 0, 999)
        val newBorderThreat = clamp(prev.borderThreat + effect.borderThreatDelta, 0, 100)
        val newCourtStability = clamp(prev.courtStability + effect.courtStabilityDelta, 0, 100)
        val newCorruption = clamp(prev.corruption + effect.corruptionDelta, 0, 100)

        // ===== 皇帝属性更新 =====
        val newPrestige = clamp(prev.prestige + effect.prestigeDelta, 0, 100)
        val newEnergy = clamp(prev.energy + effect.energyDelta, 0, 100)
        val newPlayfulness = clamp(prev.playfulness + effect.playfulnessDelta, 0, 100)
        val newMartialSkill = clamp(prev.martialSkill + effect.martialSkillDelta, 0, 100)
        val newPoliticalWisdom = clamp(prev.politicalWisdom + effect.politicalWisdomDelta, 0, 100)

        // ===== 省份更新 =====
        val newProvinces = prev.provinces.toMutableMap()
        for ((province, delta) in effect.popularSupportDelta) {
            newProvinces[province] = newProvinces[province]?.let {
                it.copy(popularSupport = clamp(it.popularSupport + delta, 0, 100))
            } ?: continue
        }

        // ===== 派系更新 =====
        val newFactions = prev.factionRelations.toMutableMap()
        for ((faction, delta) in effect.factionRelationDelta) {
            newFactions[faction] = clamp((newFactions[faction] ?: 50) + delta, -100, 100)
        }

        // ===== 时间推进 =====
        val (newYear, newMonth, newDay) = advanceTime(prev.year, prev.month, prev.day)

        // ===== 剧情标记更新 =====
        val newFlags = prev.flags.toMutableMap()
        effect.setFlags.forEach { newFlags[it] = true }
        effect.clearFlags.forEach { newFlags.remove(it) }

        // ===== 事件管理 =====
        val newActiveEvents = prev.activeEvents.toMutableList()
        // 移除过期事件
        newActiveEvents.removeAll { it.status == EventStatus.EXPIRED }
        // 添加新事件
        newActiveEvents.addAll(effect.newEvents)

        // ===== 变更日志 =====
        val changes = mutableMapOf<String, Pair<Int, Int>>()
        if (effect.treasuryDelta != 0) changes["国库"] = prev.treasury to newTreasury
        if (effect.militaryDelta != 0) changes["军力"] = prev.military to newMilitary
        if (effect.prestigeDelta != 0) changes["威望"] = prev.prestige to newPrestige
        // ... 其余变更

        val entry = ChangeEntry(
            turn = prev.turn + 1,
            edictSummary = effect.summary,
            changes = changes,
            causation = effect.causation
        )
        changeLog.add(entry)

        // ===== 构建新状态 =====
        val newState = prev.copy(
            turn = prev.turn + 1,
            year = newYear,
            month = newMonth,
            day = newDay,
            treasury = newTreasury,
            granary = newGranary,
            military = newMilitary,
            borderThreat = newBorderThreat,
            courtStability = newCourtStability,
            corruption = newCorruption,
            prestige = newPrestige,
            energy = newEnergy,
            playfulness = newPlayfulness,
            martialSkill = newMartialSkill,
            politicalWisdom = newPoliticalWisdom,
            provinces = newProvinces,
            factionRelations = newFactions,
            flags = newFlags,
            activeEvents = newActiveEvents,
            changeLog = changeLog.takeLast(100),
            timestamp = System.currentTimeMillis(),
            snapshotId = "turn_${prev.turn + 1}_${System.currentTimeMillis()}"
        )

        _currentState = newState
        snapshots[newState.turn] = newState

        // 清理旧快照 — 只保留每 10 回合的检查点
        cleanupSnapshots()

        return newState
    }

    /**
     * 回滚到指定回合
     */
    fun rollbackTo(turn: Int): WorldState? {
        val snapshot = snapshots[turn] ?: return null
        _currentState = snapshot
        return snapshot
    }

    /**
     * 获取最近的变更摘要（给 LLM 记忆用）
     */
    fun getRecentChanges(count: Int = 3): List<ChangeEntry> {
        return changeLog.takeLast(count)
    }

    // ========== 内部方法 ==========

    private fun cleanupSnapshots() {
        val toRemove = snapshots.keys.filter { turn ->
            turn > 0 && turn % 10 != 0 &&
                    turn < _currentState.turn - 50
        }
        toRemove.forEach { snapshots.remove(it) }
    }

    companion object {
        /** 时间推进（简化：每月 3 回合，即每 3 回合前进一个月） */
        private fun advanceTime(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
            var newDay = day + 10
            var newMonth = month
            var newYear = year
            if (newDay > 30) {
                newDay -= 30
                newMonth++
            }
            if (newMonth > 12) {
                newMonth -= 12
                newYear++
            }
            return Triple(newYear, newMonth, newDay)
        }

        fun clamp(value: Int, min: Int, max: Int): Int =
            when {
                value < min -> min
                value > max -> max
                else -> value
            }
    }
}
