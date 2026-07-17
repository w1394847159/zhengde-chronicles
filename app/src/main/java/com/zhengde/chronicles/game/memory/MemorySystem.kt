package com.zhengde.chronicles.game.memory

import com.zhengde.chronicles.game.world.WorldState
import com.zhengde.chronicles.game.world.ChangeEntry
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 分层记忆系统
 *
 * 设计思路（与崇祯模拟器一致）：
 * - RAG 效果不好，会让模型只关注玩家提到的关键词而忽略全局
 * - 改用"步进式增量 + 分层记忆压缩"
 *
 * 分层结构：
 * Level 0: 当前全量状态快照 → 必送
 * Level 1: 最近 3 回合变更摘要 → 必送
 * Level 2: 关键历史节点 → 由 EventSystem 标记的里程碑
 * Level 3: 语义关联记忆 → 轻量 RAG 辅助
 */
@Singleton
class MemorySystem @Inject constructor() {

    /** 关键历史事件（Level 2） */
    private val milestones = mutableListOf<Milestone>()

    /** 回合摘要缓存（Level 1） */
    private val recentSummaries = ArrayDeque<String>(5)

    /** 最大里程碑数量 */
    private val maxMilestones = 50

    /**
     * 每回合结束后调用，压缩本轮信息到记忆中
     */
    fun compress(
        turn: Int,
        edictSummary: String,
        effectCausation: String,
        newState: WorldState,
        triggeredEvents: List<String>
    ) {
        // Level 1: 更新近期摘要
        val summary = buildTurnSummary(turn, edictSummary, effectCausation)
        recentSummaries.addLast(summary)
        if (recentSummaries.size > 5) {
            recentSummaries.removeFirst()
        }

        // Level 2: 检测是否里程碑事件
        if (triggeredEvents.isNotEmpty() || isMilestoneEvent(newState)) {
            milestones.add(Milestone(
                turn = turn,
                year = newState.year,
                month = newState.month,
                description = edictSummary,
                triggeredEvents = triggeredEvents
            ))
            // 裁剪超出的里程碑
            if (milestones.size > maxMilestones) {
                val removeCount = milestones.size - maxMilestones
                repeat(removeCount) { milestones.removeFirstOrNull() }
            }
        }
    }

    /**
     * 构建 LLM Prompt 的记忆注入部分
     */
    fun buildMemoryPrompt(currentState: WorldState): String {
        val sb = StringBuilder()

        // Level 0: 当前状态（由 PromptBuilder 处理，这里不重复）

        // Level 1: 最近变更
        sb.appendLine("【近期朝政回顾】")
        if (recentSummaries.isEmpty()) {
            sb.appendLine("  陛下初登大宝，尚无重大举措。")
        } else {
            recentSummaries.forEach { sb.appendLine("  · $it") }
        }

        // Level 2: 关键历史节点
        if (milestones.isNotEmpty()) {
            sb.appendLine("\n【重要历史节点】")
            val recent = milestones.takeLast(10)
            recent.forEach { m ->
                sb.appendLine("  · ${m.year}年${m.month}月：${m.description}")
            }
        }

        // Level 3: 当前活跃事件
        if (currentState.activeEvents.isNotEmpty()) {
            sb.appendLine("\n【亟待处理的事务】")
            currentState.activeEvents.forEach { event ->
                sb.appendLine("  · [${event.status.name}] ${event.title}：${event.description.take(40)}...")
            }
        }

        return sb.toString()
    }

    /**
     * 重置（开新档时调用）
     */
    fun reset() {
        milestones.clear()
        recentSummaries.clear()
    }

    // ========== 内部 ==========

    private fun buildTurnSummary(turn: Int, edict: String, causation: String): String {
        val brief = edict.take(30)
        return "第${turn}回合：\"${brief}...\" → $causation"
    }

    /**
     * 判断是否为里程碑事件
     * 当发生重大数值变化或关键剧情触发时判定为里程碑
     */
    private fun isMilestoneEvent(state: WorldState): Boolean {
        // 简单判断：首次触发的特殊标记
        return state.flags.any { (key, value) ->
            value && key.startsWith("milestone_")
        }
    }
}

data class Milestone(
    val turn: Int,
    val year: Int,
    val month: Int,
    val description: String,
    val triggeredEvents: List<String>
)
