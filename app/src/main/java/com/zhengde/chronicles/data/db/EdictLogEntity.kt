package com.zhengde.chronicles.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 诏书记录实体
 */
@Entity(tableName = "edict_log")
data class EdictLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val turn: Int,
    val playerEdict: String,
    val effectJson: String,       // EdictEffect JSON
    val tokenCost: Double,
    val timestamp: Long = System.currentTimeMillis()
)
