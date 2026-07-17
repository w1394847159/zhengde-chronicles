package com.zhengde.chronicles.game.features.incognito

import com.zhengde.chronicles.game.engine.GameFeature
import com.zhengde.chronicles.game.engine.FeatureResult
import com.zhengde.chronicles.game.world.WorldState
import com.zhengde.chronicles.game.world.ActiveEvent
import com.zhengde.chronicles.game.world.EventType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * 🎭 微服私访系统 — 朱厚照偷跑出宫
 *
 * 正德皇帝最出名的爱好之一——偷跑出去玩。
 * 消耗 1 回合，随机触发民间事件。
 *
 * 结果取决于：
 * - 运气（随机）
 * - 皇帝威望（威望高微服更安全）
 * - 玩心（玩心高微服更有趣但也更危险）
 */
@Singleton
class IncognitoFeature @Inject constructor() : GameFeature {

    override val name: String = "微服私访"

    /** 微服事件池 */
    private val events = listOf(
        IncognitoEvent(
            id = "market_encounter",
            title = "市井奇遇",
            desc = "陛下换上便服，混入闹市。茶馆里说书先生正讲着一桩奇案…………",
            prestigeChange = 0,
            playfulnessChange = 5,
            energyChange = -3,
            courtChange = -5,
            wisdomChange = 2,
            narrative = "陛下来到城南茶馆，点了一壶龙井。" +
                    "邻桌几个书生正在议论朝政，说刘瑾专权，民不聊生。" +
                    "陛下听了一下午，既生气又觉得新鲜——原来民间是这样看朕的朝堂的。",
            isSpecial = false
        ),
        IncognitoEvent(
            id = "talent_discovery",
            title = "举贤",
            desc = "有人在酒肆中纵论天下，谈吐不凡……",
            prestigeChange = 3,
            playfulnessChange = 3,
            energyChange = -3,
            courtChange = -3,
            wisdomChange = 5,
            narrative = "陛下在酒肆中遇到一个醉醺醺的书生。" +
                    "此人虽衣衫褴褛，却对边关形势了如指掌。" +
                    "陛下暗记下他的名字，准备回宫后查一查此人底细。" +
                    "\n\n正是：英雄不问出处。",
            isSpecial = false
        ),
        IncognitoEvent(
            id = "corruption_exposed",
            title = "撞破黑幕",
            desc = "无意间撞见一桩官商勾结的勾当……",
            prestigeChange = 5,
            playfulnessChange = -3,
            energyChange = -5,
            courtChange = 3,
            wisdomChange = 3,
            narrative = "陛下在城东暗访，撞见几个官员正在收受贿赂。" +
                    "他们大摇大摆地讨论如何虚报灾情、侵吞赈灾银两。" +
                    "陛下龙颜大怒，恨不得当场拿人。但转念一想——倒要看看，这水有多深。",
            isSpecial = true
        ),
        IncognitoEvent(
            id = "identity_exposed",
            title = "暴露身份",
            desc = "不妙，被人认出来了！",
            prestigeChange = -5,
            playfulnessChange = -2,
            energyChange = -5,
            courtChange = -8,
            wisdomChange = 0,
            narrative = "陛下正在街头吃小吃，突然有人大喊：「皇上？!」\n\n" +
                    "原来是一个曾在宫中当差的老太监认出了陛下。" +
                    "一时间街市轰动，百姓跪了一地。" +
                    "陛下只好匆匆回宫，第二天朝堂上又多了几本劝谏的奏折。",
            isSpecial = true
        ),
        IncognitoEvent(
            id = "assassination_attempt",
            title = "遇刺",
            desc = "暗处有人持刀扑来……！",
            prestigeChange = 3,
            playfulnessChange = -8,
            energyChange = -10,
            courtChange = -5,
            wisdomChange = 2,
            martialChange = 3,
            narrative = "陛下在一条僻静小巷中遭到伏击！\n\n" +
                    "两个蒙面人持刀杀出。陛下虽惊不乱，顺手抄起路边一根扁担格挡。" +
                    "幸得锦衣卫暗哨及时赶到，刺客见势不妙，服毒自尽。" +
                    "\n\n经此一役，陛下武艺见长，但回宫后也后怕不已。" +
                    "正是：千金之子坐不垂堂。",
            isSpecial = true,
            requiresHighPlayfulness = true
        )
    )

    override fun checkTrigger(state: WorldState): Boolean {
        return state.energy >= 15 && state.courtStability >= 15
    }

    override fun onActivate(state: WorldState): List<ActiveEvent> {
        return listOf(ActiveEvent(
            id = "incognito_${state.turn}",
            title = "🎭 微服私访",
            description = "陛下在宫中坐不住了，想出宫走走。换上一身便服，带上几个贴身侍卫……",
            type = EventType.NARRATIVE_DRIVEN,
            deadline = state.turn + 1,
            turnCreated = state.turn
        ))
    }

    override fun executeChoice(state: WorldState, choiceId: String): FeatureResult {
        // 随机触发一个事件
        val available = events.filter { event ->
            if (event.requiresHighPlayfulness) state.playfulness >= 60 else true
        }
        val event = available[Random.nextInt(available.size)]

        val changes = mutableMapOf(
            "playfulness" to event.playfulnessChange,
            "energy" to event.energyChange,
            "courtStability" to event.courtChange
        )
        if (event.prestigeChange != 0) changes["prestige"] = event.prestigeChange
        if (event.wisdomChange != 0) changes["politicalWisdom"] = event.wisdomChange
        if (event.martialChange != 0) changes["martialSkill"] = event.martialChange

        val flags = mutableListOf<String>()
        if (event.isSpecial) {
            flags.add("incognito_${event.id}")
            flags.add("milestone_incognito_${event.id}")
        }

        return FeatureResult(
            stateChanges = changes,
            setFlags = flags,
            narrative = event.narrative,
            newEvents = if (event.isSpecial) listOf(ActiveEvent(
                id = "incognito_aftermath_${state.turn}",
                title = "【微服后续】${event.title}",
                description = "陛下回宫后，回想起今日见闻……",
                type = EventType.NARRATIVE_DRIVEN,
                deadline = state.turn + 3,
                turnCreated = state.turn
            )) else emptyList()
        )
    }
}

data class IncognitoEvent(
    val id: String,
    val title: String,
    val desc: String,
    val prestigeChange: Int,
    val playfulnessChange: Int,
    val energyChange: Int,
    val courtChange: Int,
    val wisdomChange: Int,
    val martialChange: Int = 0,
    val narrative: String,
    val isSpecial: Boolean = false,
    val requiresHighPlayfulness: Boolean = false
)
