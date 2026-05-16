package com.whitestone.app.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        StoneDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migration1To2PreservesRowsAndAddsNullableTagColumns() {
        val dbName = "migration-test"
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                """
                INSERT INTO stones (id, type, timestamp, note, dayKey)
                VALUES (1, 'WHITE', 100, 'Existing', '2026-05-17')
                """.trimIndent()
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)
        val cursor = db.query(
            "SELECT type, timestamp, note, dayKey, rootTagsCsv, rootDescriptor, intensity FROM stones WHERE id = 1"
        )

        cursor.use {
            it.moveToFirst()
            assertEquals("WHITE", it.getString(0))
            assertEquals(100L, it.getLong(1))
            assertEquals("Existing", it.getString(2))
            assertEquals("2026-05-17", it.getString(3))
            assertNull(it.getString(4))
            assertNull(it.getString(5))
            assertNull(it.getString(6))
        }
    }
}
