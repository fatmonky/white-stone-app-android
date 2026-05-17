package com.whitestone.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ReflectionDaoTest {
    private lateinit var database: StoneDatabase
    private lateinit var dao: ReflectionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, StoneDatabase::class.java).build()
        dao = database.reflectionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertUpdateAndDeleteAreKeyedByDayKey() = runBlocking {
        dao.insert(
            Reflection(
                dayKey = "2026-05-17",
                questionIndex = 7,
                responseText = "first",
                createdAt = 100,
                updatedAt = 100
            )
        )

        val existing = dao.getByDayKeyOnce("2026-05-17")!!
        dao.update(existing.copy(responseText = "updated", updatedAt = 200))

        assertEquals("updated", dao.getByDayKey("2026-05-17").first()?.responseText)

        dao.deleteByDayKey("2026-05-17")

        assertNull(dao.getByDayKey("2026-05-17").first())
    }

    @Test
    fun reflectionsForQuestionAreChronologicalWithinSameQuestionIndex() = runBlocking {
        dao.insert(reflection(dayKey = "2026-05-19", questionIndex = 2))
        dao.insert(reflection(dayKey = "2026-05-17", questionIndex = 2))
        dao.insert(reflection(dayKey = "2026-05-18", questionIndex = 3))

        val reflections = dao.getAllForQuestion(2).first()

        assertEquals(listOf("2026-05-17", "2026-05-19"), reflections.map { it.dayKey })
    }

    private fun reflection(dayKey: String, questionIndex: Int): Reflection =
        Reflection(
            dayKey = dayKey,
            questionIndex = questionIndex,
            responseText = dayKey,
            createdAt = 100,
            updatedAt = 100
        )
}
