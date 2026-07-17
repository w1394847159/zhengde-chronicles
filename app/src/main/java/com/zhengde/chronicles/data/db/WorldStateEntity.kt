package com.zhengde.chronicles.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zhengde.chronicles.game.world.WorldState

/**
 * 世界状态快照实体 — 用于 Room 持久化
 *
 * 将复杂的 WorldState 序列化为 JSON 字符串存储。
 * 加载时反序列化回 WorldState 对象。
 */
@Entity(tableName = "world_state")
data class WorldStateEntity(
    @PrimaryKey
    val turn: Int,
    val year: Int,
    val month: Int,
    val day: Int,
    val stateJson: String,        // WorldState 序列化
    val snapshotType: String,     // "FULL" / "CHECKPOINT"
    val isCheckpoint: Boolean,
    val timestamp: Long,
    val saveName: String = ""
) {
    companion object {
        fun fromWorldState(state: WorldState, snapshotType: String = "FULL"): WorldStateEntity {
            val gson = com.google.gson.Gson()
            return WorldStateEntity(
                turn = state.turn,
                year = state.year,
                month = state.month,
                day = state.day,
                stateJson = gson.toJson(state),
                snapshotType = snapshotType,
                isCheckpoint = state.turn % 10 == 0,
                timestamp = System.currentTimeMillis()
            )
        }

        fun toWorldState(entity: WorldStateEntity): WorldState {
            val gson = com.google.gson.Gson()
            return gson.fromJson(entity.stateJson, WorldState::class.java)
        }
    }
}
