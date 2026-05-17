package com.whitestone.app.data

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Stone::class, Reflection::class], version = 3, exportSchema = true)
@TypeConverters(Converters::class)
abstract class StoneDatabase : RoomDatabase() {
    abstract fun stoneDao(): StoneDao
    abstract fun reflectionDao(): ReflectionDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE stones ADD COLUMN rootTagsCsv TEXT")
        db.execSQL("ALTER TABLE stones ADD COLUMN rootDescriptor TEXT")
        db.execSQL("ALTER TABLE stones ADD COLUMN intensity TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS reflections (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                dayKey TEXT NOT NULL,
                questionIndex INTEGER NOT NULL,
                responseText TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_reflections_dayKey ON reflections(dayKey)"
        )
    }
}

class Converters {
    @TypeConverter
    fun fromStoneType(type: StoneType): String = type.name

    @TypeConverter
    fun toStoneType(value: String): StoneType = StoneType.valueOf(value)
}
