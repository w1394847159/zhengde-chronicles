package com.zhengde.chronicles.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 数据访问对象
 */
@Dao
interface GameDao {

    // ========== 世界状态快照 ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: WorldStateEntity)

    @Query("SELECT * FROM world_state WHERE turn = :turn LIMIT 1")
    suspend fun getSnapshot(turn: Int): WorldStateEntity?

    @Query("SELECT * FROM world_state WHERE isCheckpoint = 1 ORDER BY turn DESC LIMIT 50")
    fun getCheckpoints(): Flow<List<WorldStateEntity>>

    @Query("DELETE FROM world_state WHERE turn < :maxTurn AND isCheckpoint = 0")
    suspend fun cleanNonCheckpoints(maxTurn: Int)

    @Query("DELETE FROM world_state")
    suspend fun deleteAllSnapshots()

    // ========== 诏书记录 ==========

    @Insert
    suspend fun insertEdictLog(log: EdictLogEntity)

    @Query("SELECT * FROM edict_log ORDER BY turn DESC LIMIT 100")
    suspend fun getRecentEdicts(): List<EdictLogEntity>

    @Query("DELETE FROM edict_log")
    suspend fun deleteAllEdicts()

    // ========== 存档 ==========

    @Insert
    suspend fun createSave(save: GameSaveEntity)

    @Query("SELECT * FROM game_save ORDER BY createdAt DESC")
    fun getAllSaves(): Flow<List<GameSaveEntity>>

    @Delete
    suspend fun deleteSave(save: GameSaveEntity)

    @Query("DELETE FROM game_save")
    suspend fun deleteAllSaves()

    // ========== Token 记录 ==========

    @Insert
    suspend fun insertTokenRecord(record: TokenRecordEntity)

    @Query("SELECT SUM(costYuan) FROM token_record")
    suspend fun getTotalTokenCost(): Double?

    @Query("SELECT SUM(inputTokens + outputTokens) FROM token_record")
    suspend fun getTotalTokens(): Int?

    @Query("SELECT COUNT(*) FROM token_record")
    suspend fun getTotalCalls(): Int?

    @Query("DELETE FROM token_record")
    suspend fun deleteAllTokenRecords()
}
