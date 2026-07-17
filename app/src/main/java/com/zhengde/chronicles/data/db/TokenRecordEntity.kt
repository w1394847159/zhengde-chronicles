package com.zhengde.chronicles.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Token 消耗记录实体
 */
@Entity(tableName = "token_record")
data class TokenRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val turn: Int,
    val inputTokens: Int,
    val outputTokens: Int,
    val costYuan: Double,
    val timestamp: Long = System.currentTimeMillis()
)
