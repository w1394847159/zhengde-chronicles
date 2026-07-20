package com.zhengde.chronicles.game.world

/**
 * 大臣动态状态 — 随游戏进程变化
 */
data class MinisterState(
    val loyalty: Int = 50,         // 好感度 0-100
    val influence: Int = 50,       // 当前势力 0-100
    val isInCourt: Boolean = true,
    val isAlive: Boolean = true,
    val lastDialogueSummary: String = "",    // 上次对话摘要
    val dialogueCount: Int = 0,             // 对话次数
    val favorHistory: List<String> = emptyList()  // 恩宠记录
)
