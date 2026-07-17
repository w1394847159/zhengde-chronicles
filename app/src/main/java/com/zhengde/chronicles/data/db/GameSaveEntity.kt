package com.zhengde.chronicles.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 存档元信息实体
 */
@Entity(tableName = "game_save")
data class GameSaveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val turn: Int,
    val year: Int,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val snapshotTurn: Int
)
