package com.zhengde.chronicles.game.features

import com.zhengde.chronicles.game.features.baofang.BaoFangFeature
import com.zhengde.chronicles.game.features.expedition.ExpeditionFeature
import com.zhengde.chronicles.game.features.incognito.IncognitoFeature
import com.zhengde.chronicles.game.features.eighttigers.EightTigersFeature
import com.zhengde.chronicles.game.features.wangyangming.WangYangMingFeature
import com.zhengde.chronicles.game.engine.GameFeature
import com.zhengde.chronicles.game.engine.FeatureResult
import com.zhengde.chronicles.game.world.WorldState
import com.zhengde.chronicles.game.world.ActiveEvent

/**
 * 特色系统管理器 — 统一调度所有子系统
 *
 * 职责：
 * 1. 管理所有 GameFeature 实例
 * 2. 检测触发条件
 * 3. 路由玩家选择到对应子系统
 */
class FeatureManager constructor(
    val baoFang: BaoFangFeature,
    val expedition: ExpeditionFeature,
    val incognito: IncognitoFeature,
    val eightTigers: EightTigersFeature,
    val wangYangMing: WangYangMingFeature
) {
    /** 所有注册的特色系统 */
    val features: List<GameFeature> = listOf(
        baoFang, expedition, incognito, eightTigers, wangYangMing
    )

    /**
     * 检测所有特色系统的触发条件
     * 返回当前满足触发条件的事件列表
     */
    fun checkAllTriggers(state: WorldState): List<ActiveEvent> {
        val events = mutableListOf<ActiveEvent>()

        // 王阳明事件链特有检测：宁王造反
        if (wangYangMing.checkNingRebellion(state)) {
            // 标记宁王造反已激活
            events.add(ActiveEvent(
                id = "ning_rebellion_${state.turn}",
                title = "【⚔️】宁王造反！",
                description = "宁王朱宸濠在江西举兵反叛！连下九江、南康，顺江而下，直逼南京！",
                type = com.zhengde.chronicles.game.world.EventType.HISTORICAL_CALLBACK,
                deadline = state.turn + 2,
                turnCreated = state.turn
            ))
        }

        // 常规触发检测
        for (feature in features) {
            if (feature.checkTrigger(state)) {
                events.addAll(feature.onActivate(state))
            }
        }

        return events
    }

    /**
     * 执行特色系统选择
     */
    fun executeFeatureChoice(featureName: String, state: WorldState, choiceId: String): FeatureResult {
        val feature = features.find { it.name == featureName }
            ?: return FeatureResult(narrative = "未知指令。")
        return feature.executeChoice(state, choiceId)
    }
}
