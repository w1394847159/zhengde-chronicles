package com.zhengde.chronicles.game.engine

import com.zhengde.chronicles.data.repository.MinisterData
import com.zhengde.chronicles.game.world.Minister
import com.zhengde.chronicles.game.world.MinisterState
import com.zhengde.chronicles.game.world.WorldState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 大臣管理器 — 管理所有大臣的状态、对话和关系
 */
@Singleton
class MinisterManager @Inject constructor() {

    /** 所有大臣的初始数据（静态档案） */
    private val ministerArchive: Map<String, Minister> =
        MinisterData.getAllMinisters().associateBy { it.id }

    /** 获取大臣档案 */
    fun getMinister(id: String): Minister? = ministerArchive[id]

    /** 获取所有大臣档案 */
    fun getAllMinisters(): List<Minister> = ministerArchive.values.toList()

    /** 获取派系所有大臣 */
    fun getMinistersByFaction(faction: String): List<Minister> =
        ministerArchive.values.filter { it.faction == faction }

    /** 获取在朝大臣 */
    fun getActiveMinisters(state: WorldState): List<Minister> =
        ministerArchive.values.filter { m ->
            state.ministerStates[m.id]?.isInCourt != false
        }

    /**
     * 获取某大臣当前的忠诚度
     */
    fun getLoyalty(state: WorldState, ministerId: String): Int {
        return state.ministerStates[ministerId]?.loyalty
            ?: ministerArchive[ministerId]?.loyalty ?: 50
    }

    /**
     * 更新大臣好感度
     */
    fun updateLoyalty(state: WorldState, ministerId: String, delta: Int): Map<String, MinisterState> {
        val states = state.ministerStates.toMutableMap()
        val current = states[ministerId] ?: MinisterState()
        states[ministerId] = current.copy(
            loyalty = (current.loyalty + delta).coerceIn(0, 100)
        )
        return states
    }

    /**
     * 构建大臣对话的 Prompt 上下文
     * 返回当前状态摘要 + 大臣近期言论
     */
    fun buildMinisterContext(state: WorldState): String {
        val sb = StringBuilder()
        sb.appendLine("【朝堂大臣概况】")

        // 按派系分组展示
        val factions = listOf("八虎", "内阁", "边军", "心学", "宗室", "宫廷")
        for (faction in factions) {
            val ministers = getMinistersByFaction(faction)
            if (ministers.isEmpty()) continue

            val activeInFaction = ministers.filter { m ->
                state.ministerStates[m.id]?.isInCourt != false
            }
            if (activeInFaction.isEmpty()) continue

            sb.appendLine("$faction：")
            for (m in activeInFaction) {
                val loyalty = getLoyalty(state, m.id)
                val loyaltyStr = when {
                    loyalty >= 80 -> "忠心耿耿"
                    loyalty >= 60 -> "尚可信任"
                    loyalty >= 40 -> "态度暧昧"
                    loyalty >= 20 -> "心怀不满"
                    else -> "怨气冲天"
                }
                val lastTalk = state.ministerStates[m.id]?.lastDialogueSummary
                sb.appendLine("  · ${m.name}（${m.title}）[${loyaltyStr}]")
                if (!lastTalk.isNullOrBlank()) {
                    sb.appendLine("    上次奏对：$lastTalk")
                }
            }
        }

        // 已不在朝的大臣
        val departed = ministerArchive.values.filter { m ->
            state.ministerStates[m.id]?.isInCourt == false
        }
        if (departed.isNotEmpty()) {
            sb.appendLine("\n不在朝中：${departed.joinToString("、") { it.name }}")
        }

        return sb.toString()
    }

    /**
     * 获取默认初始大臣状态
     */
    fun getDefaultMinisterStates(): Map<String, MinisterState> {
        val states = mutableMapOf<String, MinisterState>()
        for (m in ministerArchive.values) {
            states[m.id] = MinisterState(
                loyalty = m.loyalty,
                influence = m.influence,
                isInCourt = m.isInCourt,
                isAlive = m.isAlive
            )
        }
        return states
    }
}
