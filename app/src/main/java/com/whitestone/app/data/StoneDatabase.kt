package com.whitestone.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(entities = [Stone::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class StoneDatabase : RoomDatabase() {
    abstract fun stoneDao(): StoneDao
}

class Converters {
    @TypeConverter
    fun fromStoneType(type: StoneType): String = type.name

    @TypeConverter
    fun toStoneType(value: String): StoneType = StoneType.valueOf(value)
}
