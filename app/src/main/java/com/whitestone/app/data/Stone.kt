package com.whitestone.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stones")
data class Stone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: StoneType,
    val timestamp: Long,
    val note: String = "",
    val dayKey: String,
    val rootTagsCsv: String? = null,
    val rootDescriptor: String? = null,
    val intensity: String? = null
)
