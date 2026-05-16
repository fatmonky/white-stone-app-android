package com.whitestone.app.data

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Stone::class], version = 2, exportSchema = true)
@TypeConverters(Converters::class)
abstract class StoneDatabase : RoomDatabase() {
    abstract fun stoneDao(): StoneDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE stones ADD COLUMN rootTagsCsv TEXT")
        db.execSQL("ALTER TABLE stones ADD COLUMN rootDescriptor TEXT")
        db.execSQL("ALTER TABLE stones ADD COLUMN intensity TEXT")
    }
}

class Converters {
    @TypeConverter
    fun fromStoneType(type: StoneType): String = type.name

    @TypeConverter
    fun toStoneType(value: String): StoneType = StoneType.valueOf(value)
}
