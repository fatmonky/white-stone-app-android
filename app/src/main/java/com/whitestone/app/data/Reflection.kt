package com.whitestone.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reflections",
    indices = [Index(value = ["dayKey"], unique = true)]
)
data class Reflection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayKey: String,
    val questionIndex: Int,
    val responseText: String,
    val createdAt: Long,
    val updatedAt: Long,
)
