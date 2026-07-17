package com.zhengde.chronicles.game.engine

/**
 * 回合快照接口 — 所有可序列化的回合状态需实现此接口
 */
interface TurnSnapshot {
    val turn: Int
    val year: Int
    val month: Int
    val day: Int
    val timestamp: Long
}
