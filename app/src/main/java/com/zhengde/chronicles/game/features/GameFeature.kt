package com.zhengde.chronicles.game.features

import com.zhengde.chronicles.game.world.WorldState
import com.zhengde.chronicles.game.world.ActiveEvent

/**
 * 特色系统接口
 *
 * 每个特色系统（豹房/亲征/微服/八虎/王阳明）实现此接口，
 * 在 WorldEngine 中注册，由 EventSystem 触发。
 */
interface GameFeature {

    /** 系统名称 */
    val name: String

    /**
     * 检测是否满足触发条件
     */
    fun checkTrigger(state: WorldState): Boolean

    /**
     * 激活时生成的事件
     */
    fun onActivate(state: WorldState): List<ActiveEvent>

    /**
     * 玩家做出选择后执行影响
     */
    fun executeChoice(state: WorldState, choiceId: String): FeatureResult
}

/**
 * 特色系统执行结果
 */
data class FeatureResult(
    val stateChanges: Map<String, Int> = emptyMap(),  // 数值变化
    val setFlags: List<String> = emptyList(),
    val narrative: String = "",                        // 叙事描述
    val newEvents: List<ActiveEvent> = emptyList()
)
