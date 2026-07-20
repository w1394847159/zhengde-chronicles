package com.zhengde.chronicles.game.world

/**
 * 大臣数据模型
 */
data class Minister(
    val id: String,
    val name: String,
    val title: String,           // 官职
    val faction: String,         // 派系
    val politics: Int,           // 政治 0-100
    val military: Int,           // 军事 0-100
    val intelligence: Int,       // 智力 0-100
    val loyalty: Int,            // 忠诚 0-100
    val influence: Int,          // 势力 0-100
    val personality: String,     // 性格描述
    val stance: String,          // 政治立场描述
    val isAlive: Boolean = true,
    val isInCourt: Boolean = true,
    val description: String      // 人物简介
)
