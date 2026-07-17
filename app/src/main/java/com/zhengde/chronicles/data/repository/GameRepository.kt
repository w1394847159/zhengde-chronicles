package com.zhengde.chronicles.data.repository

import com.zhengde.chronicles.data.db.*
import com.zhengde.chronicles.game.world.WorldState
import com.zhengde.chronicles.game.engine.TurnResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据仓库 — 连接 Room 数据库与游戏引擎
 */
@Singleton
class GameRepository @Inject constructor() {

    private var database: AppDatabase? = null

    /** 初始化数据库（在 Application 中调用） */
    fun init(context: android.content.Context) {
        database = AppDatabase.getInstance(context)
    }

    private val dao: GameDao? get() = database?.gameDao()

    // ========== 世界状态快照 ==========

    suspend fun saveSnapshot(state: WorldState) {
        val entity = WorldStateEntity.fromWorldState(state)
        dao?.insertSnapshot(entity)
    }

    suspend fun loadSnapshot(turn: Int): WorldState? {
        val entity = dao?.getSnapshot(turn) ?: return null
        return WorldStateEntity.toWorldState(entity)
    }

    fun getCheckpoints(): Flow<List<WorldStateEntity>>? = dao?.getCheckpoints()

    suspend fun saveTurnResult(result: TurnResult) {
        if (!result.success || result.newState == null) return

        // 保存状态快照
        val entity = WorldStateEntity.fromWorldState(
            result.newState,
            if (result.newState.turn % 10 == 0) "CHECKPOINT" else "FULL"
        )
        dao?.insertSnapshot(entity)

        // 记录诏书
        // （诏书内容需要在调用处传入，或从 result 中提取）
    }

    // ========== 存档 ==========

    suspend fun createSave(name: String, state: WorldState) {
        val save = GameSaveEntity(
            name = name,
            turn = state.turn,
            year = state.year,
            description = "第${state.turn}回合 · 正德${state.year - 1505 + 1}年",
            snapshotTurn = state.turn
        )
        dao?.createSave(save)

        // 确保存档对应的快照是 checkpoint
        val entity = WorldStateEntity.fromWorldState(state, "CHECKPOINT")
        dao?.insertSnapshot(entity)
    }

    fun getAllSaves(): Flow<List<GameSaveEntity>>? = dao?.getAllSaves()

    suspend fun deleteSave(id: Long) {
        val save = GameSaveEntity(id = id, name = "", turn = 0, year = 0, snapshotTurn = 0)
        dao?.deleteSave(save)
    }

    // ========== Token 统计 ==========

    suspend fun getTotalCost(): Double = dao?.getTotalTokenCost() ?: 0.0
    suspend fun getTotalTokens(): Int = dao?.getTotalTokens() ?: 0
    suspend fun getTotalCalls(): Int = dao?.getTotalCalls() ?: 0

    // ========== 清理 ==========

    suspend fun clearAllData() {
        dao?.deleteAllSnapshots()
        dao?.deleteAllEdicts()
        dao?.deleteAllTokenRecords()
    }
}
