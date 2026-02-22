package com.whitestone.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StoneDao {
    @Query("SELECT * FROM stones ORDER BY timestamp ASC")
    fun getAllStones(): Flow<List<Stone>>

    @Query("SELECT * FROM stones WHERE dayKey = :dayKey ORDER BY timestamp ASC")
    fun getStonesForDay(dayKey: String): Flow<List<Stone>>

    @Query("SELECT * FROM stones WHERE id = :id")
    suspend fun getStoneById(id: Long): Stone?

    @Insert
    suspend fun insert(stone: Stone): Long

    @Update
    suspend fun update(stone: Stone)

    @Delete
    suspend fun delete(stone: Stone)
}
