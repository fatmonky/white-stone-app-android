package com.whitestone.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class QuestionCount(
    val questionIndex: Int,
    val count: Int,
)

@Dao
interface ReflectionDao {
    @Query("SELECT * FROM reflections WHERE dayKey = :dayKey LIMIT 1")
    fun getByDayKey(dayKey: String): Flow<Reflection?>

    @Query("SELECT * FROM reflections WHERE dayKey = :dayKey LIMIT 1")
    suspend fun getByDayKeyOnce(dayKey: String): Reflection?

    @Query("SELECT * FROM reflections WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<Reflection?>

    @Query("SELECT * FROM reflections WHERE questionIndex = :index ORDER BY dayKey ASC")
    fun getAllForQuestion(index: Int): Flow<List<Reflection>>

    @Query("SELECT * FROM reflections ORDER BY dayKey DESC")
    fun getAllNewestFirst(): Flow<List<Reflection>>

    @Query("SELECT questionIndex, COUNT(*) AS count FROM reflections GROUP BY questionIndex")
    fun countsByQuestion(): Flow<List<QuestionCount>>

    @Query("SELECT dayKey FROM reflections")
    fun getAllDayKeys(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(reflection: Reflection): Long

    @Update
    suspend fun update(reflection: Reflection)

    @Query("DELETE FROM reflections WHERE dayKey = :dayKey")
    suspend fun deleteByDayKey(dayKey: String)

    @Query("DELETE FROM reflections WHERE id = :id")
    suspend fun deleteById(id: Long)
}
